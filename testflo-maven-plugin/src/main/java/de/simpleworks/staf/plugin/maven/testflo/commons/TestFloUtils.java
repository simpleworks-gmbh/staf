package de.simpleworks.staf.plugin.maven.testflo.commons;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.api.domain.Subtask;
import com.jayway.jsonpath.JsonPath;

import de.simpleworks.staf.commons.elements.TestCase;
import de.simpleworks.staf.commons.elements.TestStep;
import de.simpleworks.staf.commons.enums.ArtefactEnum;
import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.report.StepReport;
import de.simpleworks.staf.commons.report.TestcaseReport;
import de.simpleworks.staf.commons.report.artefact.Artefact;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsArtefact;
import de.simpleworks.staf.plugin.maven.testflo.commons.enums.TestCaseStatus;
import de.simpleworks.staf.plugin.maven.testflo.commons.enums.TestFloTypes;
import de.simpleworks.staf.plugin.maven.testflo.commons.enums.TestPlanStatus;
import de.simpleworks.staf.plugin.maven.testflo.commons.enums.TestStepStatus;
import net.minidev.json.JSONArray;

public class TestFloUtils {

	private static final Logger logger = LogManager.getLogger(TestFloUtils.class);

	private static final String PATH_STEPSROWS = "$.stepsRows";
	private static final String PATH_STEPSCOLUMNS = "$.stepsColumns";

	private static final String STEPS = "Steps";
	private static final String ACTION = "Action";
	private static final String NAME = "name";
	private static final String CELLS = "cells";
	private static final String ACTUAL_RESULT = "Actual result";

	public static void readSteps(final Issue testCaseIssue, final TestCase testCase) throws SystemException {
		if (TestFloUtils.logger.isDebugEnabled()) {
			TestFloUtils.logger.debug(String.format("read steps for test case: '%s'.", testCase.getId()));
		}

		final String stepsContent = TestFloUtils.getField(testCaseIssue, TestFloUtils.STEPS);
		if (TestFloUtils.logger.isDebugEnabled()) {
			TestFloUtils.logger.debug(String.format("test case '%s' has steps: '%s'.", testCase.getId(), stepsContent));
		}

		try {
			final int actionIndex = TestFloUtils.getActionIndex(stepsContent);
			if (TestFloUtils.logger.isDebugEnabled()) {
				TestFloUtils.logger.debug(String.format("actionIndex: %d.", Integer.valueOf(actionIndex)));
			}

			final JSONArray array = JsonPath.read(stepsContent, TestFloUtils.PATH_STEPSROWS);
			Assert.assertFalse(String.format("can't find elements for path: '%s' in '%s'.", TestFloUtils.PATH_STEPSROWS,
					stepsContent), Convert.isEmpty(array));

			int order = 1;
			for (final Object obj : array) {
				Assert.assertNotNull("obj can't be null.", obj);
				Assert.assertEquals(String.format("unexpected class for step: '%s'.", obj.toString()),
						LinkedHashMap.class, obj.getClass());

				@SuppressWarnings("rawtypes")
				final String action = TestFloUtils.getAction((LinkedHashMap) obj, actionIndex);
				if (TestFloUtils.logger.isDebugEnabled()) {
					TestFloUtils.logger.debug(
							String.format("create test step for order: %d and action: '%s' and add to test plan.",
									Integer.valueOf(order), action));
				}

				testCase.add(new TestStep(order, action));

				order++;
			}
		} catch (final Exception ex) {
			final String message = String.format("can't get steps for test case '%s': stepsContent: '%s'.",
					testCase.getId(), stepsContent);
			TestFloUtils.logger.error(message, ex);
			throw new SystemException(message);
		}
	}

	private static String getAction(@SuppressWarnings("rawtypes") final LinkedHashMap map, final int actionIndex) {
		final Object cells = map.get(TestFloUtils.CELLS);
		Assert.assertNotNull("cells can't be null.", cells);
		Assert.assertEquals(String.format("unexpected class for cells: '%s'.", cells.toString()), JSONArray.class,
				cells.getClass());

		final Object obj = ((JSONArray) cells).get(actionIndex);
		Assert.assertNotNull(String.format("obj on index: %d can't be null.", Integer.valueOf(actionIndex)), obj);
		Assert.assertEquals(String.format("unexpected class for cell: '%s'.", obj.toString()), String.class,
				obj.getClass());

		return obj.toString();
	}

	private static int getColumnIndex(final String content, final String columnName) throws SystemException {
		Assert.assertFalse("content can't be null or empty string.", Convert.isEmpty(content));
		Assert.assertFalse("columnName can't be null or empty string.", Convert.isEmpty(columnName));

		final JSONArray array = JsonPath.read(content, TestFloUtils.PATH_STEPSCOLUMNS);

		final StringBuilder builder = new StringBuilder();
		int index = 0;
		for (final Object obj : array) {
			Assert.assertNotNull("obj can't be null.", obj);
			Assert.assertEquals(String.format("unexpected class for step: '%s'.", obj.toString()), LinkedHashMap.class,
					obj.getClass());

			@SuppressWarnings("rawtypes")
			final LinkedHashMap map = (LinkedHashMap) obj;
			final String name = (String) map.get(TestFloUtils.NAME);
			if (columnName.equals(name)) {
				return index;
			}

			index++;
			builder.append(", ").append(name);
		}

		final String message;
		if (2 < builder.length()) {
			message = String.format("can't find '%s' in '%s' (found fields: '%s').", columnName, content,
					builder.substring(2));
		} else {
			message = String.format("can't find '%s' in '%s'.", columnName, content);
		}

		throw new SystemException(message);
	}

	private static int getActionIndex(final String content) throws SystemException {
		return TestFloUtils.getColumnIndex(content, TestFloUtils.ACTION);
	}

	public static int getActualResultIndex(final Issue issue) throws SystemException {
		Assert.assertNotNull("issue can't be null.", issue);

		final String stepsContent = TestFloUtils.getField(issue, TestFloUtils.STEPS);
		if (TestFloUtils.logger.isDebugEnabled()) {
			TestFloUtils.logger.debug(String.format("issue '%s' has steps: '%s'.", issue.getId(), stepsContent));
		}

		return TestFloUtils.getColumnIndex(stepsContent, TestFloUtils.ACTUAL_RESULT);
	}

	public static String getField(final Issue issue, final String name) {
		Assert.assertNotNull("issue can't be null.", issue);
		Assert.assertFalse("name can't be null.", Convert.isEmpty(name));

		final IssueField field = issue.getFieldByName(name);
		Assert.assertNotNull(String.format("test case '%s': can't get field '%s'.", issue.getKey(), name), field);

		String result = Convert.EMPTY_STRING;

		if (field.getValue() != null) {
			result = field.getValue().toString();
			if (Convert.isEmpty(result)) {
				if (TestFloUtils.logger.isDebugEnabled()) {
					TestFloUtils.logger.debug(
							String.format("test case '%s': field '%s' without value.", issue.getKey(), name),
							field.getValue());
				}
			}
		}

		return result;
	}

	public static void checkType(final String key, final TestFloTypes expected, final IssueType actual)
			throws SystemException {
		Assert.assertNotNull("expected type can't be null.", expected);
		Assert.assertNotNull("actual type can't be null.", actual);

		if (TestFloUtils.logger.isDebugEnabled()) {
			TestFloUtils.logger.debug(String.format("key: '%s', expected: '%s', actual: '%s'.", key,
					expected.getTestFloName(), actual.getName()));
		}

		if (!expected.getTestFloName().equals(actual.getName())) {
			throw new SystemException(String.format("issue: '%s' has unexpected type: '%s' (expected is:'%s').", key,
					actual.getName(), expected));
		}
	}

	private static void checkStatus(final String key, final String expected, final Status actual)
			throws SystemException {
		if (TestFloUtils.logger.isDebugEnabled()) {
			TestFloUtils.logger
					.debug(String.format("key: '%s', expected: '%s', actual: '%s'.", key, expected, actual.getName()));
		}

		if (!expected.equals(actual.getName())) {
			throw new SystemException(String.format("issue: '%s' has unexpected status: '%s' (expected is:'%s').", key,
					actual.getName(), expected));
		}
	}

	public static void checkStatus(final Issue issue, final String expected) throws SystemException {
		Assert.assertNotNull("issue type can't be null.", issue);
		Assert.assertNotNull("expected type can't be null.", expected);

		TestFloUtils.checkStatus(issue.getKey(), expected, issue.getStatus());
	}

	public static void checkStatus(final Subtask subtask, final TestCaseStatus expected) throws SystemException {
		Assert.assertNotNull("subtask type can't be null.", subtask);
		Assert.assertNotNull("expected type can't be null.", expected);

		TestFloUtils.checkStatus(subtask.getIssueKey(), expected.getTestFloName(), subtask.getStatus());
	}

	public static void checkType(final Issue issue, final TestFloTypes expected) throws SystemException {
		TestFloUtils.checkType(issue.getKey(), expected, issue.getIssueType());
	}

	public static void checkType(final Subtask subtask, final TestFloTypes expected) throws SystemException {
		TestFloUtils.checkType(subtask.getIssueKey(), expected, subtask.getIssueType());
	}

	public static void checkTestPlanStatus(final Issue issue, final TestPlanStatus testPlanStatus,
			final TestCaseStatus testCaseStatus) throws SystemException {
		Assert.assertNotNull("issue type can't be null.", issue);
		Assert.assertNotNull("testPlanStatus type can't be null.", testPlanStatus);

		if (TestFloUtils.logger.isDebugEnabled()) {
			TestFloUtils.logger
					.debug(String.format("check status, expected for test plan '%s', expected for test case: '%s'.",
							testPlanStatus, testCaseStatus));
		}

		final StringBuilder builder = new StringBuilder();

		try {
			TestFloUtils.checkType(issue, TestFloTypes.TestPlan);
		} catch (final SystemException ex) {
			TestFloUtils.logger.error(String.format("test plan '%s'.. is no test plan..", issue.getKey()), ex);
			builder.append(", '").append(ex.getMessage()).append("'");
		}

		try {
			TestFloUtils.checkStatus(issue, testPlanStatus.getTestFloName());
		} catch (final SystemException ex) {
			TestFloUtils.logger.error(String.format("test plan '%s' unexpected status (expected is: '%s').",
					issue.getKey(), testPlanStatus.getTestFloName()), ex);
			builder.append(", '").append(ex.getMessage()).append("'");
		}

		if (testCaseStatus != null) {
			for (final Subtask subtask : issue.getSubtasks()) {
				try {
					TestFloUtils.checkType(subtask, TestFloTypes.TestCase);
					TestFloUtils.checkStatus(subtask, testCaseStatus);
				} catch (final SystemException ex) {
					TestFloUtils.logger.error(String.format("subtask '%s' can't be started.", subtask.getIssueKey()),
							ex);
					builder.append(", '").append(ex.getMessage()).append("'");
				}
			}
		}

		final String errors = builder.toString();
		if (!Convert.isEmpty(errors)) {
			throw new SystemException(
					String.format("can't start test plan: '%s'. Reason(s): %s.", issue.getKey(), errors.substring(2)));
		}
	}

	public static String getComment(final StepReport stepReport) {
		final Exception error = stepReport.getError();

		return error == null ? null : String.format("error: '%s'.", error.getMessage());
	}

	public static List<StepResult> prepareStepResult(final TestCase testCase, final TestcaseReport report)
			throws SystemException {
		if (TestFloUtils.logger.isDebugEnabled()) {
			TestFloUtils.logger.debug(
					String.format("prepare for test case: '%s' and report: '%s'.", testCase.getId(), report.getId()));
		}

		if (!testCase.getTemplateId().equals(report.getId())) {
			throw new SystemException(
					String.format("test case '%s', report '%s': not equals templates (test case '%s').",
							testCase.getId(), testCase.getTemplateId(), report.getId()));
		}

		Exception ex = new Exception();
		
		
				
		final Map<String, TestStep> map = new HashMap<>();
		for (final TestStep testStep : testCase.getTestSteps()) {
			map.put(testStep.getSummary(), testStep);
		}

		final List<StepResult> result = new ArrayList<>();

		int row = 0;
		for (final StepReport stepReport : report.getSteps()) {
			final String key = stepReport.getDescription();
			final TestStep testStep = map.get(key);
			if (testStep == null) {
				throw new SystemException(String.format("can't find test step for report step: '%s'.", key));
			}

			if (testStep.getOrder() != (row + 1)) {

				System.out.println("");

//				throw new SystemException(
//						String.format("test case '%s', report '%s' have different order (test case: %d, report: %d).",
//								testCase.getId(), report.getId(), Integer.valueOf(testStep.getOrder()),
//								Integer.valueOf(row + 1)));
			}

			else {
				map.remove(key);
			}

			final String comment = TestFloUtils.getComment(stepReport);
			final TestStepStatus status = TestStepStatus.get(stepReport.getResult());
			final File attachment = UtilsArtefact.saveAttachment(stepReport);
			@SuppressWarnings("rawtypes")
			final Artefact artefact = stepReport.getArtefact();
			final ArtefactEnum attachmentType = artefact == null ? null : artefact.getType();

			result.add(new StepResult(Integer.valueOf(row), status, comment, attachment, attachmentType));
			row++;
		}

		if (!map.isEmpty()) {
			if (TestFloUtils.logger.isDebugEnabled()) {
				TestFloUtils.logger.debug(String.format("test case '%s', report '%s' have different steps.",
						testCase.getId(), report.getId()));
			}
		}

		return result;
	}
}
