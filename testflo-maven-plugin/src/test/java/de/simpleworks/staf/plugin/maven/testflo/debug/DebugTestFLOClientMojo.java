package de.simpleworks.staf.plugin.maven.testflo.debug;

import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.junit.Assert;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.domain.Attachment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.api.domain.Subtask;
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.beust.jcommander.JCommander;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.module.jira.module.JiraModule;
import de.simpleworks.staf.module.jira.util.consts.ClientConsts;
import de.simpleworks.staf.plugin.maven.testflo.commons.TestFlo;
import de.simpleworks.staf.plugin.maven.testflo.consts.Consts;
import de.simpleworks.staf.plugin.maven.testflo.mojo.TestfloMojo;
import okhttp3.OkHttpClient;

public class DebugTestFLOClientMojo extends TestfloMojo {

	private static final Logger logger = LogManager.getLogger(DebugTestFLOClientMojo.class);

	@Inject
	private IssueRestClient client;

	@Inject
	@Named(Consts.BASIC_AUTHENTICATED_CLIENT)
	private OkHttpClient clientHttp;

	@Parameter(property = "testPlanId", required = true)
	protected final String testPlanId;

	@Parameter(property = "file", required = true)
	protected String fileName;

	@Inject
	@Named(Consts.JIRA_REST_TMS)
	private final URL urlTms;

	@Inject
	@Named(ClientConsts.URL)
	private URL jiraUrl;

	protected final TestFlo clientNG;

	protected DebugTestFLOClientMojo(final String testPlanId, final String fileName, final URL urlTms,
			final URL jiraUrl) {
		super(new JiraModule());

		if (Convert.isEmpty(testPlanId)) {
			throw new IllegalArgumentException("testPlanId can't be null or empty string.");
		}

		if (Convert.isEmpty(fileName)) {
			throw new IllegalArgumentException("fileName can't be null or empty string.");
		}

		if (urlTms == null) {
			throw new IllegalArgumentException("urlTms can't be null.");
		}

		this.testPlanId = testPlanId;
		this.fileName = fileName;
		this.urlTms = urlTms;
		this.jiraUrl = jiraUrl;

		clientNG = new TestFlo(client, clientHttp, urlTms, jiraUrl);
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		throw new MojoFailureException("not implemented -> debug class.");
	}

	public static void dumpFields(final String title, final Iterable<IssueField> fields) {
		DebugTestFLOClientMojo.logger.debug(title);
		for (final IssueField field : fields) {
			DebugTestFLOClientMojo.logger.debug(String.format("field: id: '%s', name: '%s', value: '%s'.",
					field.getId(), field.getName(), field.getValue()));
		}
	}

	public void dumpIssue() {
		final Issue issue = client.getIssue(testPlanId).claim();
		DebugTestFLOClientMojo.logger.debug(String.format("key: '%s', issue: id: %d.", issue.getKey(), issue.getId()));

		final IssueType type = issue.getIssueType();
		DebugTestFLOClientMojo.logger
				.debug(String.format("issue type: id: %d, name: '%s'.", type.getId(), type.getName()));

		final Status status = issue.getStatus();
		DebugTestFLOClientMojo.logger
				.debug(String.format("status: id: %d, name: '%s'.", status.getId(), status.getName()));

		DebugTestFLOClientMojo.dumpFields("fields for issue.", issue.getFields());

		DebugTestFLOClientMojo.logger.debug("attachments:");
		for (final Attachment attachment : issue.getAttachments()) {
			DebugTestFLOClientMojo.logger.debug(String.format("attachment: '%s'.", attachment));
		}

		final IssueField field = issue.getField("Test Plan Iteration");
		Assert.assertNotNull("field \"Test Plan Iteration\" can't be null.", field);
		final Object obj = field.getValue();
		DebugTestFLOClientMojo.logger
				.debug(String.format("class for field: \"Iteration\" is: '%s'.", obj.getClass().getName()));

		{
			DebugTestFLOClientMojo.logger.debug("transition:");
			final Iterable<Transition> transitions = client.getTransitions(issue).claim();
			for (final Transition transition : transitions) {
				DebugTestFLOClientMojo.logger.debug(String.format("transition: name: '%s', id: %d.",
						transition.getName(), Integer.valueOf(transition.getId())));
			}
		}

		for (final Subtask subtask : issue.getSubtasks()) {
			final Issue i = client.getIssue(subtask.getIssueKey()).claim();

			final IssueField steps = i.getFieldByName("Steps");
			DebugTestFLOClientMojo.logger.debug(String.format("steps: '%s'.", steps.getValue()));

			final IssueField template = i.getFieldByName("TC Template");
			DebugTestFLOClientMojo.logger.debug(String.format("template: '%s'.", template.getValue()));

			final Iterable<Transition> transitions = client.getTransitions(i).claim();
			for (final Transition transition : transitions) {
				DebugTestFLOClientMojo.logger.debug(String.format("subtask: '%s', transition: name: '%s', id: %d.",
						subtask.getIssueKey(), transition.getName(), Integer.valueOf(transition.getId())));
			}
		}
	}

	public static void main(final String[] args) throws Exception {
		DebugTestFLOClientMojo.logger.info("start..");

		final DebugArgsFetch arguments = new DebugArgsFetch();
		JCommander.newBuilder().addObject(arguments).build().parse(args);

		final DebugTestFLOClientMojo client = new DebugTestFLOClientMojo(arguments.id, arguments.file, arguments.urlTms,
				arguments.jiraUrl);
		client.dumpIssue();

		DebugTestFLOClientMojo.logger.info("DONE.");
	}

}
