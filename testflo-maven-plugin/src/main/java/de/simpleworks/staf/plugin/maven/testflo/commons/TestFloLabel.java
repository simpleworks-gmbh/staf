package de.simpleworks.staf.plugin.maven.testflo.commons;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueFieldId;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.util.concurrent.Promise;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.module.jira.util.JiraFailCallback;

public class TestFloLabel {

	private static final Logger logger = LogManager.getLogger(TestFloLabel.class);
	private final IssueRestClient jira;
	private final boolean keepJiraLabel;

	public TestFloLabel(final IssueRestClient jira, final boolean keepJiraLabel) {
		if (jira == null) {
			throw new IllegalArgumentException("jira can't be null.");
		}
		this.jira = jira;
		this.keepJiraLabel = keepJiraLabel;
	}

	public void addLabels(String issueKey, List<String> labels) {

		if (Convert.isEmpty(issueKey)) {
			throw new IllegalArgumentException("issueKey can't be null or empty string.");
		}

		if (Convert.isEmpty(labels)) {
			throw new IllegalArgumentException("labels can't be null or empty.");
		}

		List<String> issueLabels = new ArrayList<String>();

		if (!issueLabels.addAll(labels)) {
			TestFloLabel.logger.error(String.format("can't add labels."));
			return;
		}

		try {

			if (this.keepJiraLabel) {

				if (TestFloLabel.logger.isDebugEnabled()) {
					TestFloLabel.logger.debug("Keep Jira Labels, while fetching.");
				}

				Promise<Issue> promiseIssue = jira.getIssue(issueKey);

				Issue jiraIssue = promiseIssue.claim();

				if (!issueLabels.addAll(UtilsCollection.toList(jiraIssue.getLabels()))) {
					TestFloLabel.logger.error(String.format("can't add labels."));
					return;
				}
			}

		} catch (Exception ex) {
			TestFloLabel.logger.error(String.format("can't fetch issue \"%s\".", issueKey), ex);
			return;
		}

		try {

			final IssueInputBuilder issueBuilder = new IssueInputBuilder();

			issueBuilder.setFieldValue(IssueFieldId.LABELS_FIELD.id, issueLabels);

			final IssueInput newIssue = issueBuilder.build();

			if (newIssue == null) {
				throw new IllegalArgumentException("newIssue can't be null.");
			}

			final Promise<Void> promise = jira.updateIssue(issueKey, newIssue);
			promise.fail(new JiraFailCallback()).claim();

		} catch (Exception ex) {
			TestFloLabel.logger.error(String.format("can't add labels [\"%s\"].", String.join(",", issueLabels)), ex);
		}
	}

}