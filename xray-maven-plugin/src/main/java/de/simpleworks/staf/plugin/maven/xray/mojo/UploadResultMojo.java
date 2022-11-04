package de.simpleworks.staf.plugin.maven.xray.mojo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.util.concurrent.Promise;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.simpleworks.staf.commons.elements.TestPlan;
import de.simpleworks.staf.commons.enums.ArtefactEnum;
import de.simpleworks.staf.commons.enums.Result;
import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.mapper.elements.MapperTestplan;
import de.simpleworks.staf.commons.mapper.report.MapperTestcaseReport;
import de.simpleworks.staf.commons.report.StepReport;
import de.simpleworks.staf.commons.report.TestcaseReport;
import de.simpleworks.staf.commons.report.artefact.HarFile;
import de.simpleworks.staf.commons.report.artefact.Screenshot;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.module.jira.util.JiraFailCallback;
import de.simpleworks.staf.plugin.maven.xray.commons.Xray;
import de.simpleworks.staf.plugin.maven.xray.consts.XrayConsts;
import de.simpleworks.staf.plugin.maven.xray.enums.StatusEnum;

@Mojo(name = "uploadTestResult", defaultPhase = LifecyclePhase.INITIALIZE)
public class UploadResultMojo extends XrayMojo {

	private static final Logger logger = LogManager.getLogger(UploadResultMojo.class);

	private final MapperTestcaseReport mapperTestcaseReport = new MapperTestcaseReport();
	private final MapperTestplan mapperTestplan = new MapperTestplan();

	@Parameter(property = "result", required = true)
	private String result;

	@Parameter(property = "testplan", required = true)
	private String testplan;

	@Inject
	@Named(XrayConsts.XRAY_CLIENT)
	private Xray xrayClient;

	@Inject
	private IssueRestClient clientJira;

	private List<TestcaseReport> testcaseReports;
	private JsonObject testrun;

	public UploadResultMojo() throws Exception {
		super();
	}

	private void init() throws Exception {

		final File resultFile = new File(result);

		if (!resultFile.exists()) {
			throw new IllegalArgumentException(String.format("resultFile does not exist at \"%s\".", result));
		}

		final File testplanFile = new File(testplan);

		if (!testplanFile.exists()) {
			throw new IllegalArgumentException(String.format("testplanFile does not exist at \"%s\".", testplan));
		}

		try {
			testcaseReports = mapperTestcaseReport.readAll(resultFile);
		} catch (final SystemException ex) {
			final String msg = String.format("can't read testcase reports from \"%s\".", result);
			UploadResultMojo.logger.error(msg, ex);
			throw new Exception(msg);
		}

		if (Convert.isEmpty(testcaseReports)) {
			throw new Exception("testcase can't be null or empty.");
		}

		List<TestPlan> testplans;

		try {
			testplans = mapperTestplan.readAll(testplanFile);
		} catch (final SystemException ex) {
			final String msg = String.format("can't read testplans from \"%s\".", testplan);
			UploadResultMojo.logger.error(msg, ex);
			throw new Exception(msg);
		}

		// Fetch test plan
		final String key = testplans.get(0).getId();
		testrun = getTestRun(key);

		if (testrun == null) {
			throw new IllegalArgumentException(String.format("testrun not found with key \"%s\".", key));
		}
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		try {
			// init variables
			init();

			updateTestSteps(testcaseReports, testrun);

		} catch (final Exception ex) {
			final String msg = "can't upload testresults.";
			UploadResultMojo.logger.error(msg, ex);
			throw new MojoExecutionException(msg);
		}
	}

	@SuppressWarnings("hiding")
	private void updateTestSteps(final List<TestcaseReport> reports, final JsonObject testrun) {
		final JsonArray tests = testrun.get("data").getAsJsonObject().get("getTestRuns").getAsJsonObject()
				.get("results").getAsJsonArray();

		for (final TestcaseReport report : reports) {

			final String keyTestcase = report.getId();
			final long id = getId(keyTestcase).longValue();

			if (UploadResultMojo.logger.isInfoEnabled()) {
				UploadResultMojo.logger.info(
						String.format("Updating test \"%s\" with Xray id \"%d\".", keyTestcase, Long.valueOf(id)));
			}

			JsonObject testObject = null;

			for (final JsonElement test : tests) {
				final String issueId = ((JsonObject) test).get("test").getAsJsonObject().get("issueId").getAsString();

				if (id == Long.valueOf(issueId).longValue()) {
					testObject = (JsonObject) test;
					break;
				}
			}

			if (testObject == null) {
				final String message = String.format("test with key \"%s\" not found in testrun.", keyTestcase);
				UploadResultMojo.logger.error(message);
				throw new IllegalArgumentException(message);
			}

			final JsonArray steps = testObject.get("steps").getAsJsonArray();

			final String testRunId = testObject.get("id").getAsString();

			String errorMessage = Convert.EMPTY_STRING;

			for (int i = 0; i < report.getSteps().size(); i++) {
				boolean failed = false;
				boolean unknown = false;
				final StepReport stepReport = report.getSteps().get(i);

				final JsonObject stepObject = steps.get(i).getAsJsonObject();
				final String stepId = stepObject.get("id").getAsString();

				if (stepReport.getError() != null) {
					String error = stepReport.getError().getMessage().toString().replaceAll("\\\"",
							Convert.EMPTY_STRING);
					error = error.replace("\\", "\\\\");
					errorMessage = error.substring(0, Math.min(500, error.length() - 1));
					errorMessage = errorMessage.replaceAll("\n", Convert.EMPTY_STRING);
				}

				String query = Convert.EMPTY_STRING;

				switch (stepReport.getResult()) {
				case SUCCESSFULL:
					if (stepReport.getArtefact() != null) {
						if (stepReport.getArtefact().getType() == ArtefactEnum.SCREENSHOT) {
							final Screenshot screenshot = (Screenshot) stepReport.getArtefact();
							final Calendar cal = Calendar.getInstance();
							final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
							final String timestamp = sdf.format(cal.getTime());
							final String filename = String.format("evidence_%s_%s.png", stepId, timestamp);
							query = String.format(
									"mutation { updateTestRunStep( testRunId: \"%s\", stepId: \"%s\", updateData: { status: \"%s\", evidence: { add:[ { filename: \"%s\", mimeType: \"image/png\", data: \"%s\"} ] } } ) { addedEvidence warnings } }",
									testRunId, stepId, StatusEnum.PASSED.getValue(), filename,
									screenshot.getArtefact());
						} else if (stepReport.getArtefact().getType() == ArtefactEnum.HARFILE) {
							final HarFile harFile = (HarFile) stepReport.getArtefact();
							final Calendar cal = Calendar.getInstance();
							final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
							final String timestamp = sdf.format(cal.getTime());
							final String filename = String.format("evidence_%s_%s.haf", stepId, timestamp);
							query = String.format(
									"mutation { updateTestRunStep( testRunId: \"%s\", stepId: \"%s\", updateData: { status: \"%s\", evidence: { add:[ { filename: \"%s\", mimeType: \"application/json\", data: \"%s\"} ] } } ) { addedEvidence warnings } }",
									testRunId, stepId, StatusEnum.PASSED.getValue(), filename,
									UploadResultMojo.convertToBase64(harFile.getArtefact()));
						}
					} else {
						query = String.format(
								"mutation { updateTestRunStep( testRunId: \"%s\", stepId: \"%s\", updateData: { status: \"%s\" } ) { warnings } }",
								testRunId, stepId, StatusEnum.PASSED.getValue());
					}
					query = String.format(
							"mutation { updateTestRunStep( testRunId: \"%s\", stepId: \"%s\", updateData: { status: \"%s\" } ) { addedDefects warnings }}",
							testRunId, stepId, StatusEnum.PASSED.getValue());
					break;
				case FAILURE:

					if (stepReport.getArtefact() != null) {

						if (stepReport.getArtefact().getType() == ArtefactEnum.SCREENSHOT) {
							final Screenshot screenshot = (Screenshot) stepReport.getArtefact();
							final Calendar cal = Calendar.getInstance();
							final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
							final String timestamp = sdf.format(cal.getTime());
							final String filename = String.format("evidence_%s_%s.png", stepId, timestamp);

							query = String.format(
									"mutation { updateTestRunStep( testRunId: \"%s\", stepId: \"%s\", updateData: { actualResult: \"%s\", status: \"%s\", evidence: { add:[ { filename: \"%s\", mimeType: \"image/png\", data: \"%s\"} ] } } ) { addedEvidence warnings } }",
									testRunId, stepId, errorMessage, StatusEnum.FAILED.getValue(), filename,
									screenshot.getArtefact());
						} else if (stepReport.getArtefact().getType() == ArtefactEnum.HARFILE) {
							final HarFile harFile = (HarFile) stepReport.getArtefact();
							final Calendar cal = Calendar.getInstance();
							final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
							final String timestamp = sdf.format(cal.getTime());
							final String filename = String.format("evidence_%s_%s.haf", stepId, timestamp);

							query = String.format(
									"mutation { updateTestRunStep( testRunId: \"%s\", stepId: \"%s\", updateData: { actualResult: \"%s\", status: \"%s\", evidence: { add:[ { filename: \"%s\", mimeType: \"application/json\", data: \"%s\"} ] } } ) { addedEvidence warnings } }",
									testRunId, stepId, errorMessage, StatusEnum.FAILED.getValue(), filename,
									UploadResultMojo.convertToBase64(harFile.getArtefact()));
						}
					} else {

						query = String.format(
								"mutation { updateTestRunStep( testRunId: \"%s\", stepId: \"%s\", updateData: { actualResult: \"%s\", status: \"%s\" } ) { warnings } }",
								testRunId, stepId, errorMessage, StatusEnum.FAILED.getValue());
					}
					failed = true;
					break;
				case UNKNOWN:
					query = String.format(
							"mutation { updateTestRunStep( testRunId: \"%s\", stepId: \"%s\", updateData: { comment: \"%s\", status: \"%s\" } ) { warnings } }",
							testRunId, stepId, errorMessage, StatusEnum.TODO.getValue());
					unknown = true;
					break;
				default:
					throw new IllegalArgumentException(
							String.format("Result \"%s\" not supported.", stepReport.getResult().getValue()));
				}

				JsonObject payload = new JsonObject();
				payload.addProperty("query", query);

				JsonObject response = null;
				try {
					response = xrayClient.fetchResponseFromXray(payload);
				} catch (final IOException ex) {
					UploadResultMojo.logger.error("can't update test step.", ex);
				}

				if (Result.FAILURE.equals(stepReport.getResult())) {
					payload = new JsonObject();
					query = String.format("mutation {updateTestRunComment(id: \"%s\", comment: \"%s\")}", testRunId,
							errorMessage);

					payload.addProperty("query", query);

					response = null;
					try {
						response = xrayClient.fetchResponseFromXray(payload);
					} catch (final IOException ex) {
						UploadResultMojo.logger.error("can't update test run comment.", ex);
					}
				}

				if (response == null) {
					throw new IllegalArgumentException(String.format("Couldn't update step \"%s\" from test \"%s\".",
							stepReport.getDescription(), keyTestcase));
				}

				if (failed || unknown) {
					break;
				}
			}

			if (UploadResultMojo.logger.isInfoEnabled()) {
				UploadResultMojo.logger.info(String.format("Update of test \"%s\" done.", keyTestcase));
			}
		}
	}

	private static String convertToBase64(final String harFileJson) {
		return Base64.getEncoder().encodeToString(harFileJson.getBytes(StandardCharsets.UTF_8));
	}

	private JsonObject getTestRun(final String key) {
		final Long id = getId(key);

		final String graphQl = String.format(
				"query{ getTestRuns( testExecIssueIds: [\"%s\"], limit: 100 ) { total limit start results { id status { name color description } steps { id action data result attachments { id filename } status { name color } } test { issueId } testExecution { issueId } } } }",
				id);
		final JsonObject payload = new JsonObject();
		payload.addProperty("query", graphQl);

		try {
			return xrayClient.fetchResponseFromXray(payload);
		} catch (final IOException ex) {
			final String message = String.format("Couldn't fetch testplan with key \"%s\".", key);
			UploadResultMojo.logger.error(message, ex);
			throw new IllegalArgumentException(message);
		}
	}

	private Long getId(final String key) {
		try {
			final Promise<Issue> promise = clientJira.getIssue(key);
			final Issue issue = promise.fail(new JiraFailCallback()).claim();

			return issue.getId();
		} catch (final Exception ex) {
			final String msg = String.format("can't fetch id for TestPlan \"%s\".", key);
			UploadResultMojo.logger.error(msg, ex);
			throw new IllegalArgumentException(msg);
		}
	}
}