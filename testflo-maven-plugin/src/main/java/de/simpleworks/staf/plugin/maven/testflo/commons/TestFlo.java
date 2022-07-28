package de.simpleworks.staf.plugin.maven.testflo.commons;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Subtask;
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;

import de.simpleworks.staf.commons.elements.TestCase;
import de.simpleworks.staf.commons.elements.TestPlan;
import de.simpleworks.staf.commons.enums.Result;
import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.report.TestcaseReport; 
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.module.jira.util.JiraProperties;
import de.simpleworks.staf.plugin.maven.testflo.commons.enums.TestCaseGeneral;
import de.simpleworks.staf.plugin.maven.testflo.commons.enums.TestCaseStatus;
import de.simpleworks.staf.plugin.maven.testflo.commons.enums.TestCaseTransition;
import de.simpleworks.staf.plugin.maven.testflo.commons.enums.TestPlanStatus;
import de.simpleworks.staf.plugin.maven.testflo.commons.enums.TestPlanTransition;
import de.simpleworks.staf.plugin.maven.testflo.commons.pojo.FixVersion;
import de.simpleworks.staf.plugin.maven.testflo.utils.TestFLOProperties;
import okhttp3.OkHttpClient;

public class TestFlo {
	private static final Logger logger = LogManager.getLogger(TestFlo.class);
	private final IssueRestClient jira;

	private final TestFloTms tms;
	private final TestFloFixVersion testFloFixVersion;
	private final TestFloLabel testFloLabel;
	private final TestFloFields testFloFields;

	public TestFlo(final IssueRestClient jira, final OkHttpClient client, final URL urlTms) {
		if (jira == null) {
			throw new IllegalArgumentException("jira can't be null.");
		}
		this.jira = jira;
		tms = new TestFloTms(client, urlTms, TestFLOProperties.getInstance());
		testFloFixVersion = new TestFloFixVersion(client, jira, JiraProperties.getInstance());
		testFloLabel = new TestFloLabel(jira);
		testFloFields = new TestFloFields(jira);
	}

	private void logTransitions(final Issue issue) {
		TestFlo.logger.error(String.format("get transitions for issue with key: '%s'.", issue.getKey()));
		try {
			final Iterable<Transition> transitions = jira.getTransitions(issue).claim();
			for (final Transition transition : transitions) {
				TestFlo.logger.error(String.format("transition: name: '%s', id: %d.", transition.getName(),
						Integer.valueOf(transition.getId())));
			}
		} catch (final Exception ex) {
			final String message = String.format("issue '%s': can't get transitions.", issue.getKey());
			TestFlo.logger.error(message, ex);
		}
	}

	private void transition(final Issue issue, final String expectedStatus, final int transtionId)
			throws SystemException {
		Assert.assertNotNull("issue can't be null.", issue);
		if (Convert.isEmpty(expectedStatus)) {
			throw new IllegalArgumentException("expectedStatus can't be null or empty string.");
		}
		TestFloUtils.checkStatus(issue, expectedStatus);
		if (TestFlo.logger.isDebugEnabled()) {
			TestFlo.logger.debug(String.format("issue '%s': process transition: '%d'.", issue.getKey(),
					Integer.valueOf(transtionId)));
		}
		try {
			jira.transition(issue, new TransitionInput(transtionId)).claim();
		} catch (final Exception ex) {
			final String message = String.format("issue '%s': can't process transition: '%d'.", issue.getKey(),
					Integer.valueOf(transtionId));
			TestFlo.logger.error(message, ex);
			logTransitions(issue);
			throw new SystemException(message);
		}
	}

	private void transition(final Issue issue, final TestPlanStatus expectedStatus, final TestPlanTransition transition)
			throws SystemException {
		Assert.assertNotNull("expectedStatus can't be null.", expectedStatus);
		Assert.assertNotNull("transition can't be null.", transition);
		transition(issue, expectedStatus.getTestFloName(), 61);
	}

	public void transition(final TestPlan testPlan, final TestPlanStatus expectedStatus,
			final TestPlanTransition transition) throws SystemException {
		Assert.assertNotNull("testPlan can't be null.", testPlan);
		Assert.assertNotNull("expectedStatus can't be null.", expectedStatus);
		Assert.assertNotNull("transition can't be null.", transition);
		final Issue issue = jira.getIssue(testPlan.getId()).claim();
		Assert.assertNotNull(String.format("can't get issue for: '%s'.", testPlan.getId()), issue);
		transition(issue, expectedStatus, transition);
	}

	private void transition(final Issue issue, final TestCaseStatus expectedStatus, final TestCaseTransition transition)
			throws SystemException {
		Assert.assertNotNull("expectedStatus can't be null.", expectedStatus);
		Assert.assertNotNull("transition can't be null.", transition);
		transition(issue, expectedStatus.getTestFloName(), transition.getTestFloId());
	}

	private void transition(final Subtask subtask, final TestCaseStatus expectedStatus,
			final TestCaseTransition transition) throws SystemException {
		Assert.assertNotNull("subtask can't be null.", subtask);
		Assert.assertNotNull("expectedStatus can't be null.", expectedStatus);
		Assert.assertNotNull("transition can't be null.", transition);
		final Issue issue = jira.getIssue(subtask.getIssueKey()).claim();
		Assert.assertNotNull(String.format("can't get issue for: '%s'.", subtask.getIssueKey()), issue);
		transition(issue, expectedStatus.getTestFloName(), transition.getTestFloId());
	}

	private Issue readTestCase(final Subtask subtask, final TestPlan testPlan) throws SystemException {
		if (TestFlo.logger.isDebugEnabled()) {
			TestFlo.logger.debug(String.format("read test case data for key: '%s'.", subtask.getIssueKey()));
		}
		final Issue result = jira.getIssue(subtask.getIssueKey()).claim();
		Assert.assertNotNull(String.format("can't get issue for: '%s'.", subtask.getIssueKey()), result);
		final TestCase testCase = new TestCase();
		testCase.setId(result.getKey());
		final String templateId = TestFloUtils.getField(result, TestCaseGeneral.TEMPLATE.getTestFloName());
		Assert.assertFalse(String.format("test case '%s': can't get templateId (field '%s' has empty value).",
				result.getKey(), TestCaseGeneral.TEMPLATE), Convert.isEmpty(templateId));
		testCase.setTemplateId(templateId);
		if (TestFlo.logger.isDebugEnabled()) {
			TestFlo.logger.debug(String.format("test case '%s' based on template: '%s'.", result.getKey(), templateId));
		}
		TestFloUtils.readSteps(result, testCase);
		if (TestFlo.logger.isDebugEnabled()) {
			TestFlo.logger.debug(String.format("add test case id: '%s', templateId: '%s' to test plan '%s'.",
					testCase.getId(), testCase.getTemplateId(), testPlan.getId()));
		}
		testPlan.add(testCase);
		return result;
	}

	public void moveTestPlanToNextIteration(final String testPlanId) throws SystemException {
		if (Convert.isEmpty(testPlanId)) {
			throw new IllegalArgumentException("testPlanId can't be null or empty string.");
		}
		if (TestFlo.logger.isInfoEnabled()) {
			TestFlo.logger.info(String.format("create next iteration for test plan '%s'.", testPlanId));
		}
		final Issue issue = jira.getIssue(testPlanId).claim();
		Assert.assertNotNull(String.format("can't get issue for: '%s'.", testPlanId), issue);
		TestFloUtils.checkTestPlanStatus(issue, TestPlanStatus.Open, null);
		tms.moveTestPlanToNextIteration(issue);
	}

	public TestCase readTestCase(final String testCaseId) throws SystemException {
		if (Convert.isEmpty(testCaseId)) {
			throw new IllegalArgumentException("testCaseId can't be null or empty string.");
		}
		if (TestFlo.logger.isInfoEnabled()) {
			TestFlo.logger.info(String.format("read test case '%s'.", testCaseId));
		}
		final TestCase testcase = new TestCase();
		testcase.setId(testCaseId);
		if (TestFlo.logger.isDebugEnabled()) {
			TestFlo.logger.debug(String.format("get issue for testCaseId: '%s'.", testCaseId));
		}
		final Issue issue = jira.getIssue(testCaseId).claim();
		Assert.assertNotNull(String.format("can't get issue for: '%s'.", testCaseId), issue);
		TestFloUtils.readSteps(issue, testcase);
		return testcase;
	}

	public TestPlan readTestPlan(final String testPlanId) throws SystemException {
		if (Convert.isEmpty(testPlanId)) {
			throw new IllegalArgumentException("testPlanId can't be null or empty string.");
		}
		if (TestFlo.logger.isInfoEnabled()) {
			TestFlo.logger.info(String.format("read test plan '%s'.", testPlanId));
		}
		final TestPlan result = new TestPlan();
		result.setId(testPlanId);
		if (TestFlo.logger.isDebugEnabled()) {
			TestFlo.logger.debug(String.format("get issue for testPlanId: '%s'.", testPlanId));
		}
		final Issue issue = jira.getIssue(testPlanId).claim();
		Assert.assertNotNull(String.format("can't get issue for: '%s'.", testPlanId), issue);
		TestFloUtils.checkTestPlanStatus(issue, TestPlanStatus.Open, TestCaseStatus.Open);
		for (final Subtask subtask : issue.getSubtasks()) {
			Assert.assertNotNull("subtask can't be null.", subtask);
			readTestCase(subtask, result);
		}
		return result;
	}

	public void startTestPlan(final TestPlan testPlan) throws SystemException {
		if (testPlan == null) {
			throw new IllegalArgumentException("testPlan can't be null or empty string.");
		}
		if (TestFlo.logger.isInfoEnabled()) {
			TestFlo.logger.info(String.format("start test plan '%s'.", testPlan.getId()));
		}
		final Issue issue = jira.getIssue(testPlan.getId()).claim();
		Assert.assertNotNull(String.format("can't get issue for: '%s'.", testPlan.getId()), issue);
		TestFloUtils.checkTestPlanStatus(issue, TestPlanStatus.Open, TestCaseStatus.Open);
		final StringBuilder builder = new StringBuilder();
		try {
			transition(issue, TestPlanStatus.Open, TestPlanTransition.Start);
		} catch (final SystemException ex) {
			TestFlo.logger.error("error on transition for test plan.", ex);
			builder.append(", '").append(ex.getMessage()).append("'");
		}
		for (final Subtask subtask : issue.getSubtasks()) {
			try {
				transition(subtask, TestCaseStatus.Open, TestCaseTransition.Test);
			} catch (final SystemException ex) {
				TestFlo.logger.error("error on transition for test case.", ex);
				builder.append(", '").append(ex.getMessage()).append("'");
			}
		}
		final String errors = builder.toString();
		if (!Convert.isEmpty(errors)) {
			throw new SystemException(
					String.format("can't start test plan: '%s'. Reason(s): %s.", issue.getKey(), errors.substring(2)));
		}
	}

	public void reOpenTestplan(final String testPlanId) {
		
		if (Convert.isEmpty(testPlanId)) {
			throw new IllegalArgumentException("testPlanId can't be null or empty string.");
		}

		final Issue issue = jira.getIssue(testPlanId).claim();
		Assert.assertNotNull(String.format("can't get issue for: '%s'.", testPlanId), issue);
		try {
			transition(issue, TestPlanStatus.Closed, TestPlanTransition.Close);
		} catch (final SystemException ex) {
			TestFlo.logger.info(String.format("ignore error '%s'.", ex.getMessage()));
		}
		for (final Subtask subtask : issue.getSubtasks()) {
			
			//FIXME: determine state of the teststep beforehand
			try {
				transition(subtask, TestCaseStatus.Pass, TestCaseTransition.Retest);
			} catch (final SystemException ex) {
				// ignore error	
			}
			
			try {
				transition(subtask, TestCaseStatus.Fail, TestCaseTransition.Retest);
			} catch (final SystemException ex) {
				// ignore error
			}
			
			final Issue subtaskIssue = jira.getIssue(subtask.getIssueKey()).claim();
			Assert.assertNotNull(String.format("can't get subtaskIssue for: '%s'.", subtask.getIssueKey()), subtaskIssue);
			
			tms.resetTeststep(subtaskIssue);	
		}
	}

	private Issue updateStepResults(final TestCase testCase, final List<StepResult> stepResults)
			throws SystemException {
		final Issue issue = jira.getIssue(testCase.getId()).claim();
		Assert.assertNotNull(String.format("can't get issue for: '%s'.", testCase.getId()), issue);
		for (final StepResult stepResult : stepResults) {
			tms.updateTestStep(issue, stepResult);
		}
		return issue;
	}

	private void updateTestCaseStatus(final Issue issue, final Result result) throws SystemException {
		TestCaseTransition transition;
		switch (result) {
		case SUCCESSFULL:
			transition = TestCaseTransition.Pass;
			break;
		case FAILURE:
			transition = TestCaseTransition.Fail;
			break;
		default:
			throw new SystemException(String.format("unsuported result: '%s'", result));
		}
		transition(issue, TestCaseStatus.InProgress, transition);
	}

	public void updateTestCase(final TestCase testCase, final TestcaseReport report) throws SystemException {
		if (testCase == null) {
			throw new IllegalArgumentException("testCase can't be null.");
		}
		if (report == null) {
			throw new IllegalArgumentException("report can't be null.");
		}
		if (TestFlo.logger.isDebugEnabled()) {
			TestFlo.logger.debug(String.format("update test case: '%s' by report: '%s'.", testCase, report.getId()));
		}
		final List<StepResult> stepResults = TestFloUtils.prepareStepResult(testCase, report);
		final Issue issue = updateStepResults(testCase, stepResults);
		updateTestCaseStatus(issue, report.getResult());
	}

	public void addFixVersions(final List<String> fixVersions, TestPlan testPlan) {
		if (Convert.isEmpty(fixVersions)) {
			throw new IllegalArgumentException("fixVersions can't be null or empty.");
		}
		if (testPlan == null) {
			throw new IllegalArgumentException("testPlan can't be null.");
		}
		final String testplanId = testPlan.getId();
		if (Convert.isEmpty(testplanId)) {
			throw new IllegalArgumentException("testplanId can't be nur or empty string.");
		}
		List<FixVersion> versions = new ArrayList<FixVersion>();
		for (final String fixVersion : fixVersions) {
			if (TestFlo.logger.isDebugEnabled()) {
				TestFlo.logger.debug(String.format("fetch fix Version '%s'.", fixVersion));
			}
			try {
				final FixVersion version = testFloFixVersion.fetchFixVersion(fixVersion, testplanId);
				if (!versions.add(version)) {
					TestFlo.logger.error(String.format("can't  add fix Version '%s'.", fixVersion));
				}
			} catch (Exception ex) {
				final String msg = String.format("can't fetch Fix Version '%s'.", fixVersion);
				TestFlo.logger.error(msg, ex);
			}
		}
		for (TestCase testcase : testPlan.getTestCases()) {
			if (TestFlo.logger.isDebugEnabled()) {
				TestFlo.logger.debug(String.format("add fix versions '%s' to issue '%s'.",
						String.join(", ",
								versions.stream().map(version -> version.toString()).collect(Collectors.toList())),
						testcase.getId()));
			}
			try {
				testFloFixVersion.addFixVersions(versions, testcase.getId());
			} catch (Exception ex) {
				final String msg = String.format("can't add Fix Version, for testcase '%s'.", testcase.getId());
				TestFlo.logger.error(msg, ex);
			}
		}
	}

	public void addLabels(List<String> labels, TestPlan testPlan) {
		if (Convert.isEmpty(labels)) {
			throw new IllegalArgumentException("labels can't be null or empty.");
		}
		if (testPlan == null) {
			throw new IllegalArgumentException("testPlan can't be null.");
		}

		for (TestCase testcase : testPlan.getTestCases()) {

			if (TestFlo.logger.isDebugEnabled()) {
				TestFlo.logger.debug(
						String.format("add labels '%s' to issue '%s'.", String.join(", ", labels), testcase.getId()));
			}
			try {
				testFloLabel.addLabels(testcase.getId(), labels);
			} catch (Exception ex) {
				final String msg = String.format("can't add Labels, for testcase '%s'.", testcase.getId());
				TestFlo.logger.error(msg, ex);
			}
		}
	}

	public void addFields(List<String> customFields, TestPlan testPlan) {
		if (Convert.isEmpty(customFields)) {
			throw new IllegalArgumentException("customFields can't be null or empty.");
		}
		if (testPlan == null) {
			throw new IllegalArgumentException("testPlan can't be null.");
		}

		for (TestCase testcase : testPlan.getTestCases()) {

			if (TestFlo.logger.isDebugEnabled()) {
				TestFlo.logger.debug(String.format("add customFields '%s' to issue '%s'.",
						String.join(", ", customFields), testcase.getId()));
			}
			try {

				testFloFields.addFields(testcase.getId(), customFields);
			} catch (Exception ex) {
				final String msg = String.format("can't add CustomFields, for testcase '%s'.", testcase.getId());
				TestFlo.logger.error(msg, ex);
			}
		}
	}
}