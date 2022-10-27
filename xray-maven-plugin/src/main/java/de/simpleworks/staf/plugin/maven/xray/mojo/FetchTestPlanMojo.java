package de.simpleworks.staf.plugin.maven.xray.mojo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.jayway.jsonpath.JsonPath;

import de.simpleworks.staf.commons.elements.TestCase;
import de.simpleworks.staf.commons.elements.TestPlan;
import de.simpleworks.staf.commons.elements.TestStep;
import de.simpleworks.staf.commons.mapper.elements.MapperTestplan;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.module.jira.util.JiraFailCallback;
import de.simpleworks.staf.plugin.maven.xray.commons.Xray;
import de.simpleworks.staf.plugin.maven.xray.consts.XrayConsts;
import de.simpleworks.staf.plugin.maven.xray.elements.Test;
import de.simpleworks.staf.plugin.maven.xray.enums.StatusEnum;
import edu.emory.mathcs.backport.java.util.Arrays;

@Mojo(name = "fetchTestPlan", defaultPhase = LifecyclePhase.INITIALIZE)
public class FetchTestPlanMojo extends XrayMojo {

	private static final Logger logger = LogManager.getLogger(FetchTestPlanMojo.class);

	@Parameter(property = "ids", required = true)
	private List<String> ids;

	@Parameter(property = "file", required = true)
	private String file;

	@Parameter(property = "environment", required = true)
	private String environment;

	@Inject
	@Named(XrayConsts.XRAY_CLIENT)
	private Xray xrayClient;

	@Inject
	private IssueRestClient clientJira;

	private final List<File> files;

	public FetchTestPlanMojo() throws Exception {
		super();
		files = new ArrayList<>();
	}

	private void init() {
		if (Convert.isEmpty(ids)) {
			throw new IllegalArgumentException("ids can't be null or empty.");
		}
		final File directory = new File(file);

		if (directory.isDirectory()) {
			for (final String id : ids) {
				files.add(Paths.get(file, String.format("Testplan-%s.json", id)).toFile());
			}
		}
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		try {
			// init variables
			init();

			fetchTestplans();

		} catch (final Exception ex) {
			final String msg = "can't upload testresults.";
			FetchTestPlanMojo.logger.error(msg, ex);
			throw new MojoExecutionException(msg);
		}
	}

	private void fetchTestplans() throws Exception {

		for (final String id : ids) {
			final TestPlan result = new TestPlan();

			result.setId(id);

			// Fetch test plan
			final JsonObject testplan = getTestplan(id);

			if (testplan == null) {
				throw new IllegalArgumentException(String.format("testplan not found with key \"%s\".", id));
			}

			// Get test cases from fetched test plan
			final List<TestCase> tests = getTests(testplan);

			for (final TestCase testCase : tests) {
				// sort test cases
				testCase.getTestSteps();

				result.add(testCase);
			}

			// Start a new test execution for the given test plan
			final String testExecutionKey = startTestExecution(result, testplan);
			result.setId(testExecutionKey);

			File testplanFile = null;

			final Optional<File> opFile = files.stream().filter(f -> f.getName().contains(id)).findFirst();

			// find the designated testplan file, that matches the testplane
			if (opFile.isPresent()) {
				testplanFile = opFile.get();
			} else {
				// set testplanfile from the parameter
				testplanFile = new File(file);
			}
			// Write resulted test plan to file
			FetchTestPlanMojo.writeTestplan(result, testplanFile);
		}
	}

	/**
	 * Fetch test plan via GraphQL API for X-Ray
	 *
	 * @param key JIRA key for the test plan to fest (e.g. SXX-92)
	 * @return JSON representation of the test plan with its containing tests and
	 *         their steps.
	 */
	private JsonObject getTestplan(final String key) {
		final String jql = String.format("project = '%s' AND issuetype = 'Test Plan' AND key = '%s'", key.split("-")[0],
				key);
		final String graphQl = String.format(
				"query{getTestPlans(jql: \"%s\", limit: 1) { total start limit results { issueId tests(limit: 100) { total start limit results { issueId testType { name } steps { id action data result attachments { id filename storedInJira downloadLink } } } } jira(fields: [\"issueNum\", \"assignee\", \"reporter\"]) } } }",
				jql);
		final JsonObject payload = new JsonObject();
		payload.addProperty("query", graphQl);

		try {
			return xrayClient.fetchResponseFromXray(payload);
		} catch (final IOException e) {
			final String message = String.format("Couldn't fetch testplan with key \"%s\".", key);
			FetchTestPlanMojo.logger.error(message, e);
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * Get the test cases that are part of the given test plan
	 *
	 * @param JSON representation of the test plan with its containing tests and
	 *             their steps.
	 * @return list of test cases that are part of the test plan
	 */
	private List<TestCase> getTests(final JsonObject testplan) {
		final List<TestCase> result = new ArrayList<>();
		final String testPlanString = testplan.toString();
		final List<String> issueIds = JsonPath.read(testPlanString, "$..['tests']['results'][*]['issueId']");

		for (int i = 0; i < issueIds.size(); i++) {
			final TestCase testCase = new TestCase();

			final String issueId = issueIds.get(i);

			final String jsonPath = String.format("$['data']['getTestPlans']['results'][0]['tests']['results'][%d]",
					Integer.valueOf(i));
			final String testObjectValue = JsonPath.read(testPlanString, jsonPath).toString();
			final JsonObject testObject = new JsonParser().parse(testObjectValue).getAsJsonObject();

			try {
				final Promise<Issue> promise = clientJira.getIssue(issueId);
				final Issue issue = promise.fail(new JiraFailCallback()).claim();

				final String key = issue.getKey();

				testCase.setId(key);
				testCase.setTemplateId(key);

				final Test test = new Test();
				test.setTestKey(key);
				test.setIssueId(issueId);
				test.setStatus(StatusEnum.TODO);

				final String jsonPathSteps = "$['steps']";
				final String stepsArrayValue = JsonPath.read(testObject.toString(), jsonPathSteps).toString();
				final JsonArray stepsArray = new JsonParser().parse(stepsArrayValue).getAsJsonArray();
				for (int j = 0; j < stepsArray.size(); j++) {

					final String summary = JsonPath.read(stepsArrayValue,
							String.format("$[%d].['action']", Integer.valueOf(j)));

					final TestStep testStep = new TestStep(j + 1, summary);
					testCase.add(testStep);
				}

				result.add(testCase);
			} catch (final Exception ex) {
				FetchTestPlanMojo.logger.error(String.format("can't fetch key for Testcase \"%s\".", issueId), ex);
			}
		}

		return result;
	}

	/**
	 * After fetching the necessary information, start a new test execution
	 *
	 * @param testplan test plan POJO needed for executing the tests
	 * @param json     JSON representation of the test plan with its containing
	 *                 tests and their step (hold information about the issueIds for
	 *                 use in the API)
	 * @return JIRA key for the resulting test execution (e.g. SXX-148)
	 */
	private String startTestExecution(final TestPlan testplan, final JsonObject json) {

		if (testplan == null) {
			throw new IllegalArgumentException("testplan can't be null.");
		}

		if (json == null) {
			throw new IllegalArgumentException("json can't be null.");
		}

		final String testplanid = String.valueOf(getId(testplan.getId()));

		final String jsonString = json.toString();
		final List<String> issueIds = JsonPath.read(jsonString, "$..['tests']['results'][*]['issueId']");
		final String xrayIdTestplan = JsonPath.read(jsonString, "$['data']['getTestPlans']['results'][0]['issueId']");

		final String issueIdsAsString = String.join("\", \"", issueIds);
		final String graphQl = String.format(
				"mutation{createTestExecution(testIssueIds: [\"%s\"], testEnvironments: [\"%s\"], jira: { fields: { summary: \"Test Execution for %s\", project: {key: \"%s\"} } }) { testExecution { issueId jira(fields: [\"key\"]) } warnings createdTestEnvironments } }",
				issueIdsAsString, environment, testplan.getId(), testplan.getId().split("-")[0]);
		final JsonObject payload = new JsonObject();
		payload.addProperty("query", graphQl);

		JsonObject resultCreatingTestExecutionJson = null;
		try {
			resultCreatingTestExecutionJson = xrayClient.fetchResponseFromXray(payload);
		} catch (final IOException ex) {
			FetchTestPlanMojo.logger.error("", ex);
		}

		if (resultCreatingTestExecutionJson == null) {
			final String errorMessage = String.format("Couldn't create Testexecution for testplan \"%s\".", testplanid);
			FetchTestPlanMojo.logger.error(errorMessage);
			throw new IllegalArgumentException(errorMessage);
		}

		final String resultJsonAsString = resultCreatingTestExecutionJson.toString();
		final String testExectutionKey = JsonPath.read(resultJsonAsString,
				"$['data']['createTestExecution']['testExecution']['jira']['key']");
		final String testExectutionId = JsonPath.read(resultJsonAsString,
				"$['data']['createTestExecution']['testExecution']['issueId']");

		final String graphQl2 = String.format(
				"mutation{addTestExecutionsToTestPlan(issueId: \"%s\", testExecIssueIds: [\"%s\"]) { addedTestExecutions warning } }",
				xrayIdTestplan, testExectutionId);
		final JsonObject payload2 = new JsonObject();
		payload2.addProperty("query", graphQl2);

		JsonObject resultAddingTestExecution = null;

		try {
			resultAddingTestExecution = xrayClient.fetchResponseFromXray(payload2);
		} catch (final IOException ex) {
			FetchTestPlanMojo.logger.error("", ex);
		}

		if (resultAddingTestExecution == null) {
			final String errorMessage = String.format("Couldn't add Testexecution to testplan \"%s\".", testplanid);
			FetchTestPlanMojo.logger.error(errorMessage);
			throw new IllegalArgumentException(errorMessage);
		}

		if (testExectutionKey == null) {
			throw new IllegalArgumentException("Couldn't fetch new key for testexection.");
		}

		return testExectutionKey;
	}

	private Long getId(final String key) {
		try {
			final Promise<Issue> promise = clientJira.getIssue(key);
			final Issue issue = promise.fail(new JiraFailCallback()).claim();

			return issue.getId();
		} catch (final Exception ex) {
			final String msg = String.format("can't fetch id for TestPlan \"%s\".", key);
			FetchTestPlanMojo.logger.error(msg, ex);
			throw new IllegalArgumentException(msg);
		}
	}

	private static void writeTestplan(final TestPlan testplan, final File file) throws Exception {

		if (testplan == null) {
			throw new IllegalArgumentException("testplan can't be null.");
		}

		if (file == null) {
			throw new IllegalArgumentException("file can't be null.");
		}

		final MapperTestplan mapper = new MapperTestplan();
		try {
			mapper.write(file, Arrays.asList(new TestPlan[] { testplan }));
		} catch (final Exception ex) {
			FetchTestPlanMojo.logger.error(String.format("Can't write result to file \"%s\".", file));
			throw ex;
		}
	}
}