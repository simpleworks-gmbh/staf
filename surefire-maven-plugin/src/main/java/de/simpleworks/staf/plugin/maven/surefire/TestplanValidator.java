package de.simpleworks.staf.plugin.maven.surefire;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.annotation.Step;
import de.simpleworks.staf.commons.elements.TestCase;
import de.simpleworks.staf.commons.elements.TestPlan;
import de.simpleworks.staf.commons.elements.TestStep;
import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.utils.Scanner;
import de.simpleworks.staf.commons.utils.UtilsCollection;

public class TestplanValidator {
	static final Logger logger = LogManager.getLogger(TestplanValidator.class);

	private final TestcaseFinder finder;

	public TestplanValidator(final TestcaseFinder finder) {
		if (finder == null) {
			throw new IllegalArgumentException("finder can't be null.");
		}

		this.finder = finder;
	}

	private static void verify(final TestCase testcase, final Class<?> clazz) throws SystemException {
		if (!Scanner.doesClassExtendSpecificClass(clazz,
				de.simpleworks.staf.framework.elements.commons.TestCase.class)) {
			throw new SystemException(String.format("wrong implementation: test case '%s' does not extend '%s'.",
					clazz.getName(), de.simpleworks.staf.framework.elements.commons.TestCase.class.getName()));
		}

		final List<TestStep> caseSteps = testcase.getTestSteps();
		final List<Step> classSteps = UtilsCollection.toList(clazz.getDeclaredMethods()).stream()
				.map(method -> method.getAnnotation(Step.class)).filter(Objects::nonNull).collect(Collectors.toList());
		Collections.sort(classSteps, (o1, o2) -> o1.order() - o2.order());

		if (caseSteps.size() != classSteps.size()) {
			throw new SystemException(String.format(
					"test case definition '%s' and implementation class '%s' have not equal count of steps: definition %d vs implementation %d.",
					testcase.getId(), clazz.getName(), Integer.valueOf(caseSteps.size()),
					Integer.valueOf(classSteps.size())));
		}

		int i = 0;
		for (final TestStep caseStep : caseSteps) {
			final Step classStep = classSteps.get(i++);
			if (caseStep.getOrder() != classStep.order()) {
				throw new SystemException(String.format(
						"test case definition '%s' and implementation class '%s' have steps with different order: definition %d vs implementation %d.",
						testcase.getId(), clazz.getName(), Integer.valueOf(caseStep.getOrder()),
						Integer.valueOf(classStep.order())));
			}

			if (!caseStep.getSummary().equals(classStep.description())) {
				throw new SystemException(String.format(
						"test case definition '%s' and implementation class '%s' have steps with different description: definition '%s' vs implementation '%s'.",
						testcase.getId(), clazz.getName(), caseStep.getSummary(), classStep.description()));
			}
		}
	}

	public List<Class<?>> validate(final TestPlan testplan) throws SystemException {
		if (testplan == null) {
			throw new IllegalArgumentException("testplan can't be null.");
		}

		if (TestplanValidator.logger.isDebugEnabled()) {
			TestplanValidator.logger.debug(String.format("search test cases for test plan: '%s'.", testplan.getId()));
		}

		final List<Class<?>> result = new ArrayList<>();
		for (final TestCase testcase : testplan.getTestCases()) {
			final String templateId = testcase.getTemplateId();
			if (TestplanValidator.logger.isDebugEnabled()) {
				TestplanValidator.logger.debug(String.format("search implementation for test case: '%s'.", templateId));
			}

			final Class<?> clazz = finder.get(templateId);
			if (clazz == null) {
				throw new SystemException(String.format("can't find implementation for test case: '%s'.", templateId));
			}

			TestplanValidator.verify(testcase, clazz);

			result.add(clazz);
		}

		return result;
	}
}
