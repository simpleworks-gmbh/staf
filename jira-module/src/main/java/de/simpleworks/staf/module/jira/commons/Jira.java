package de.simpleworks.staf.module.jira.commons;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Attachment;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.input.AttachmentInput;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.LinkIssuesInput;
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;
import com.atlassian.jira.rest.client.internal.async.AsynchronousIssueRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;

import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.module.jira.elements.SubTask;
import de.simpleworks.staf.module.jira.elements.Task;
import de.simpleworks.staf.module.jira.util.JiraProperties;
import de.simpleworks.staf.module.jira.util.JiraRateLimitingEffect;
import de.simpleworks.staf.module.jira.util.enums.Status;
import de.simpleworks.staf.module.jira.util.linkedissues.LinkedIssueType;

public class Jira {
	private JiraRestClient jiraClient;
	private final JiraProperties configuration;
	private static final Logger logger = LogManager.getLogger(Jira.class);

	public Jira(final JiraProperties configuration) {
		if (configuration == null) {
			throw new IllegalArgumentException("configuration can't be null.");
		}

		this.configuration = configuration;
		try {
			this.jiraClient = new AsynchronousJiraRestClientFactory().createWithBasicHttpAuthentication(getJiraUri(),
					configuration.getUsername(), configuration.getPassword());
		} catch (final Exception ex) {
			final String message = "can't configure jira client.";
			Jira.logger.error(message, ex);
			throw new RuntimeException(message);
		}
	}

	private IssueRestClient getIssueRestClient() throws SystemException {
		if (jiraClient == null) {
			throw new SystemException("jiraClient can't be null.");
		}

		final IssueRestClient result = jiraClient.getIssueClient();

		if (result == null) {
			throw new SystemException("can't get issue client.");
		}

		return result;
	}

	private URI getJiraUri() throws SystemException {
		if (this.configuration == null) {
			throw new IllegalArgumentException("configuration can't be null.");
		}

		final URL url = this.configuration.getUrl();
		if (url == null) {
			throw new IllegalArgumentException("url can't be null.");
		}

		URI uri;

		try {
			uri = url.toURI();
		} catch (final URISyntaxException ex) {
			final String message = String.format("can't get URI from URL: '%s'.", url);
			Jira.logger.error(message, ex);
			throw new SystemException(message);
		}

		return uri;
	}

	public String createIssue(final Task task) throws SystemException {
		if (task == null) {
			throw new IllegalArgumentException("task can't be null or empty string.");
		}

		task.validate();

		BasicIssue basicIssue;

		try {
			final IssueRestClient issueClient = getIssueRestClient();

			final IssueInputBuilder issueBuilder = new IssueInputBuilder(task.getProjectKey(),
					Long.valueOf(task.getIssueType()));
			issueBuilder.setFieldValue("labels",
					task.getLabels().stream().map(label -> label.getValue()).collect(Collectors.toList()));
			issueBuilder.setSummary(task.getSummary());
			issueBuilder.setDescription(task.getDescription());

			final IssueInput newIssue = issueBuilder.build();
			if (newIssue == null) {
				throw new SystemException("can't build issue.");
			}

			final Promise<BasicIssue> promise = issueClient.createIssue(newIssue);
			basicIssue = promise.fail(new JiraRateLimitingEffect()).claim();
		} catch (final Exception ex) {
			final String message = "can't create issue.";
			Jira.logger.error(message, ex);
			throw new SystemException(message);
		}

		if (basicIssue == null) {
			throw new SystemException("newIssue can't be null or empty string.");
		}

		return basicIssue.getKey();
	}

	public void linkIssues(final String parentIssueKey, final Task task, final LinkedIssueType link)
			throws SystemException {
		if (Convert.isEmpty(parentIssueKey)) {
			throw new IllegalArgumentException("parentIssueKey can't be null or empty string.");
		}

		if (task == null) {
			throw new IllegalArgumentException("task can't be null or empty string.");
		}

		if (link == null) {
			throw new IllegalArgumentException("link can't be null or empty string.");
		}

		try {
			final IssueRestClient issueClient = getIssueRestClient();

			// normal task
			final IssueInputBuilder issueBuilder = new IssueInputBuilder();

			issueBuilder.setProjectKey(task.getProjectKey());
			issueBuilder.setSummary(task.getSummary());
			issueBuilder.setDescription(task.getDescription());

			final IssueInput newIssue = issueBuilder.build();
			if (newIssue == null) {
				throw new SystemException("can't build issue.");
			}

			final Promise<Void> promise = issueClient
					.linkIssue(new LinkIssuesInput(parentIssueKey, task.getKey(), link.getName()));
			promise.fail(new JiraRateLimitingEffect()).claim();
		} catch (final Exception ex) {
			final String message = "can't link issue.";
			Jira.logger.error(message, ex);
			throw new SystemException(message);
		}
	}

	public String createSubTask(final String parentIssueKey, final Task task) throws SystemException {
		if (Convert.isEmpty(parentIssueKey)) {
			throw new IllegalArgumentException("parentIssueKey can't be null or empty string.");
		}

		if (task == null) {
			throw new IllegalArgumentException("task can't be null or empty string.");
		}

		BasicIssue basicIssue;

		try {
			final IssueRestClient issueClient = getIssueRestClient();

			// normal task
			final IssueInputBuilder issueBuilder = new IssueInputBuilder();

			issueBuilder.setProjectKey(task.getProjectKey());
			issueBuilder.setSummary(task.getSummary());

			issueBuilder.setDescription(task.getDescription());
			final Long issueTypeId = Long.valueOf((new SubTask() {
				private static final long serialVersionUID = -7333103708919940665L;
			}).getIssueType());
			issueBuilder.setIssueTypeId(issueTypeId);

			issueBuilder.setFieldValue("parent", ComplexIssueInputFieldValue.with("key", parentIssueKey));
			final IssueInput newIssue = issueBuilder.build();
			if (newIssue == null) {
				throw new IllegalArgumentException("newIssue can't be null or empty string.");
			}

			final Promise<BasicIssue> promise = issueClient.createIssue(newIssue);
			basicIssue = promise.fail(new JiraRateLimitingEffect()).claim();
		} catch (final Exception ex) {
			final String message = "can't create sub task.";
			Jira.logger.error(message, ex);
			throw new SystemException(message);
		}

		if (basicIssue == null) {
			throw new SystemException("newIssue can't be null or empty string.");
		}

		return basicIssue.getKey();
	}

	public void updateIssue(final Task task) throws SystemException {
		if (task == null) {
			throw new IllegalArgumentException("task can't be null or empty string.");
		}

		task.validate();

		try {
			final IssueRestClient issueClient = getIssueRestClient();

			final IssueInputBuilder issueBuilder = new IssueInputBuilder();
			issueBuilder.setFieldValue("labels",
					task.getLabels().stream().map(label -> label.getValue()).collect(Collectors.toList()));
			issueBuilder.setFieldValue("issuetype",
					ComplexIssueInputFieldValue.with("id", Long.valueOf(task.getIssueType())));
			issueBuilder.setProjectKey(task.getProjectKey());
			issueBuilder.setDescription(task.getDescription());

			final IssueInput newIssue = issueBuilder.build();
			if (newIssue == null) {
				throw new IllegalArgumentException("newIssue can't be null or empty string.");
			}

			final Promise<Void> promise = issueClient.updateIssue(task.getKey(), newIssue);
			promise.fail(new JiraRateLimitingEffect()).claim();
		} catch (final Exception ex) {
			final String message = "can't update issue.";
			Jira.logger.error(message, ex);
			throw new SystemException(message);
		}
	}

	// TODO: make all methods protected
	public Issue getIssue(final String issueKey) throws SystemException {
		if (Convert.isEmpty(issueKey)) {
			throw new IllegalArgumentException("issueKey can't be null or empty string.");
		}

		Issue issue;

		try {
			final IssueRestClient issueClient = getIssueRestClient();

			final Promise<Issue> promise = issueClient.getIssue(issueKey);
			issue = promise.fail(new JiraRateLimitingEffect()).claim();
		} catch (final Exception ex) {
			final String message = "can't get issue.";
			Jira.logger.error(message, ex);
			throw new SystemException(message);
		}

		if (issue == null) {
			throw new SystemException("issue can't be null.");
		}

		return issue;
	}

	public void addComment(final String issueKey, final String commentBody) throws SystemException {
		if (Convert.isEmpty(issueKey)) {
			throw new IllegalArgumentException("issueKey can't be null or empty string.");
		}

		if (Convert.isEmpty(commentBody)) {
			throw new IllegalArgumentException("commentBody can't be null or empty string.");
		}

		try {
			final IssueRestClient issueClient = getIssueRestClient();

			final Promise<Issue> promise = issueClient.getIssue(issueKey);
			final Issue issue = promise.fail(new JiraRateLimitingEffect()).claim();

			issueClient.addComment(issue.getCommentsUri(), Comment.valueOf(commentBody));
		} catch (final Exception ex) {
			final String message = "can't add comment.";
			Jira.logger.error(message, ex);
			throw new SystemException(message);
		}
	}

	public void addAttachement(final Task task, final File attachement) throws SystemException {
		if (task == null) {
			throw new IllegalArgumentException("task can't be null.");
		}

		if (attachement == null) {
			throw new IllegalArgumentException("attachement can't be null.");
		}

		if (!attachement.exists()) {
			throw new IllegalArgumentException(
					String.format("The file at %s does not exist.", attachement.getAbsolutePath()));
		}

		try {
			final IssueRestClient issueClient = getIssueRestClient();

			final Promise<Issue> promise = issueClient.getIssue(task.getKey());
			final Issue issue = promise.fail(new JiraRateLimitingEffect()).claim();

			if (Jira.logger.isDebugEnabled()) {
				Jira.logger.debug(String.format("using AttachmentsUri %s .", issue.getAttachmentsUri()));
			}

			final Promise<Void> upload = issueClient.addAttachments(issue.getAttachmentsUri(), attachement);
			upload.fail(new JiraRateLimitingEffect()).claim();

		} catch (final Exception ex) {
			final String message = String.format("can't add attachment from file: '%s'.", attachement);
			Jira.logger.error(message, ex);
			throw new SystemException(message);
		}
	}

	public void changeStatus(final Task task, final Status status) throws SystemException {
		if (task == null) {
			throw new IllegalArgumentException("task can't be null or empty string.");
		}

		task.validate();

		if (status == null) {
			throw new IllegalArgumentException("status can't be null.");
		}

		try {
			final IssueRestClient issueClient = getIssueRestClient();

			final Promise<Issue> promise = issueClient.getIssue(task.getKey());

			final Issue issue = promise.fail(new JiraRateLimitingEffect()).claim();

			if (Jira.logger.isDebugEnabled()) {
				Jira.logger.debug(String.format("using AttachmentsUri %s .", issue.getAttachmentsUri()));
			}

			final Promise<Void> changeStatus = issueClient.transition(issue,
					new TransitionInput(Integer.parseInt(status.getValue())));
			changeStatus.fail(new JiraRateLimitingEffect()).claim();

		} catch (final Exception ex) {
			final String message = "can't change status.";
			Jira.logger.error(message, ex);
			throw new SystemException(message);
		}
	}

	public List<Attachment> fetchAttachements(final Task task) throws SystemException {
		if (task == null) {
			throw new IllegalArgumentException("task can't be null.");
		}

		final List<Attachment> attachements = new ArrayList<>();

		try {
			final IssueRestClient issueClient = getIssueRestClient();

			final Promise<Issue> promise = issueClient.getIssue(task.getKey());
			final Issue issue = promise.fail(new JiraRateLimitingEffect()).claim();
			issue.getAttachments().forEach(attachement -> attachements.add(attachement));
		} catch (final Exception ex) {
			final String message = "can't fetch attachements.";
			Jira.logger.error(message, ex);
			throw new SystemException(message);
		}

		if (Jira.logger.isDebugEnabled()) {
			if (Convert.isEmpty(attachements)) {
				Jira.logger.debug(
						String.format("No attachements were fetched from Task, identified by '%s'.", task.getKey()));
			}
		}

		return attachements;
	}

	public boolean uploadAttachements(final String subTaskKey, final List<Attachment> attachements)
			throws SystemException {
		if (Convert.isEmpty(subTaskKey)) {
			throw new IllegalArgumentException("subTaskKey can't be null.");
		}

		if (Convert.isEmpty(attachements)) {
			throw new IllegalArgumentException("attachements can't be null.");
		}

		try {
			final IssueRestClient issueClient = getIssueRestClient();

			for (final Attachment attachement : attachements) {
				final Promise<Issue> promise = issueClient.getIssue(subTaskKey);
				final Issue newIssue = promise.fail(new JiraRateLimitingEffect()).claim();

				final AsynchronousIssueRestClient asynchronousIssueRestClient = (AsynchronousIssueRestClient) issueClient;
				final Promise<InputStream> attachment = asynchronousIssueRestClient
						.getAttachment(attachement.getContentUri());

				try (final InputStream inputStream = attachment.get();) {
					final Promise<Void> upload = issueClient.addAttachments(newIssue.getAttachmentsUri(),
							new AttachmentInput(attachement.getFilename(), inputStream));
					upload.fail(new JiraRateLimitingEffect()).claim();
				}
			}
		} catch (final Exception ex) {
			final String message = "can't upload attachements.";
			Jira.logger.error(message, ex);
			throw new SystemException(message);
		}

		return true;
	}
}
