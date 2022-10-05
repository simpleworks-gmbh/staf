package de.simpleworks.staf.plugin.maven.surefire;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.annotation.Step;
import de.simpleworks.staf.commons.annotation.Testcase;
import de.simpleworks.staf.commons.elements.TestCase;
import de.simpleworks.staf.commons.elements.TestPlan;
import de.simpleworks.staf.commons.elements.TestStep;
import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.Scanner;
import de.simpleworks.staf.framework.util.TestCaseUtils;

public class TestplanValidator {
	static final Logger logger = LogManager.getLogger(TestplanValidator.class);

	private final TestcaseFinder finder;

	public TestplanValidator(final TestcaseFinder finder) {
		if (finder == null) {
			throw new IllegalArgumentException("finder can't be null.");
		}

		this.finder = finder;
	}

	private static boolean verify(final TestCase testcase, final Class<?> clazz, final String[] errorSummary) throws SystemException {

		if (!Scanner.doesClassExtendSpecificClass(clazz,
				de.simpleworks.staf.framework.elements.commons.TestCase.class)) {

			TestplanValidator.logger.error(String.format("wrong implementation: test case '%s' does not extend '%s'.",
					clazz.getName(), de.simpleworks.staf.framework.elements.commons.TestCase.class.getName()));

			return false;
		}
		
		if(!Convert.isEmpty(errorSummary)) {
			throw new IllegalArgumentException("errorSummary can't be null or empty.");
		}
		
		
		final List<TestStep> caseSteps = testcase.getTestSteps();
		final List<Method> allMethods = TestCaseUtils.collectMethods(clazz);
		final List<Step> classSteps = allMethods.stream().map(method -> method.getAnnotation(Step.class))
				.filter(Objects::nonNull).collect(Collectors.toList());
		Collections.sort(classSteps, (o1, o2) -> o1.order() - o2.order());

		if (caseSteps.size() != classSteps.size()) {

			TestplanValidator.logger.error(
					"test case definition '%s' and implementation class '%s' have not equal count of steps: definition %d vs implementation %d.",
					testcase.getId(), clazz.getName(), Integer.valueOf(caseSteps.size()),
					Integer.valueOf(classSteps.size()));

			return false;
		}

		int i = 0;
		boolean result = true;
		//List<String> errorMessages = new ArrayList<String>();

		for (final TestStep caseStep : caseSteps) {
			final Step classStep = classSteps.get(i++);
			if (caseStep.getOrder() != classStep.order()) {
				result = false;
				final String errorMessage = String.format(
						"test case definition '%s' and implementation class '%s' have steps with different order: definition %d vs implementation %d.",
						testcase.getId(), clazz.getName(), Integer.valueOf(caseStep.getOrder()),
						Integer.valueOf(classStep.order()));

				final String currentErrorSummary = errorSummary[0];
				errorSummary[0] = String.format("%s\r\n%s", currentErrorSummary, errorMessage);
			}

			if (!caseStep.getSummary().equals(classStep.description())) {
				result = false;
				final String errorMessage = String.format(
						"test case definition '%s' and implementation class '%s' have steps with different description: definition '%s' vs implementation '%s'.",
						testcase.getId(), clazz.getName(), caseStep.getSummary(), classStep.description());

				final String currentErrorSummary = errorSummary[0];
				errorSummary[0] = String.format("%s\r\n%s", currentErrorSummary, errorMessage);
			}
		}

		return result;
	}

	public List<Class<?>> validate(final TestPlan testplan) throws SystemException {
		if (testplan == null) {
			throw new IllegalArgumentException("testplan can't be null.");
		}

		if (TestplanValidator.logger.isDebugEnabled()) {
			TestplanValidator.logger.debug(String.format("search test cases for test plan: '%s'.", testplan.getId()));
		}

		final List<Class<?>> result = new ArrayList<>();
		
		final String[] errorSummary = new String[1];
		errorSummary[0] = Convert.EMPTY_STRING;
		
		for (final TestCase testcase : testplan.getTestCases()) {
			final String templateId = testcase.getTemplateId();
			if (TestplanValidator.logger.isDebugEnabled()) {
				TestplanValidator.logger.debug(String.format("search implementation for test case: '%s'.", templateId));
			}

			final Class<?> clazz = finder.get(templateId);
			if (clazz == null) {
				TestplanValidator.logger
						.error(String.format("can't find implementation for test case: '%s'.", templateId));
				continue;
			}

			if (TestplanValidator.verify(testcase, clazz, errorSummary)) {
				result.add(clazz);
			}
		}

		if (result.size() != testplan.getTestCases().size()) {

			final List<String> testcaseIds = result.stream().map(clazz -> (Testcase) clazz.getAnnotation(Testcase.class))
					.map(testcase -> testcase.id()).collect(Collectors.toList());
			
			final String validTestcases = String.join("\r\n", testcaseIds);

			if (TestplanValidator.logger.isInfoEnabled()) {
				TestplanValidator.logger.info(String.format("the testcases '%s' are valid.", validTestcases));
			}
			
			
			final List<String> invalidTestcaseIds = 
					testplan.getTestCases().stream().filter(testcase -> testcaseIds.indexOf(testcase.getTemplateId()) < 0)
					.map(testcase -> testcase.getTemplateId())
					.collect(Collectors.toList());
			

			final String invalidTestcases = String.join("\r\n", invalidTestcaseIds);

			if (TestplanValidator.logger.isInfoEnabled()) {
				TestplanValidator.logger.info(String.format("the testcases '%s' are invalid.", invalidTestcases));
			}

			throw new SystemException(String.format("only '%s' from '%s' are valid [%s].",
					Integer.toString(result.size()), Integer.toString(testplan.getTestCases().size()),
					String.join(",", result.stream().map(res -> res.getName()).collect(Collectors.toList()))));
		}

		return result;
	}
}
