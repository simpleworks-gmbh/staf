
package de.simpleworks.staf.module.jira.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;
import de.simpleworks.staf.module.jira.util.enums.TaskLabel;

public class TestPlan extends Task {
	private static final long serialVersionUID = 7648048807664924508L;

	private static final Logger logger = LogManager.getLogger(TestPlan.class);

	private List<JiraTestCase> tests;

	public TestPlan() {
		this.tests = new ArrayList<>();
	}

	public List<JiraTestCase> getTests() {
		return tests;
	}

	public void setTests(final List<JiraTestCase> tests) {
		this.tests = tests;
	}

	@Override
	public void validate() throws SystemException {
		if (TestPlan.logger.isInfoEnabled()) {
			TestPlan.logger.info(String.format("validate %s..", this));
		}

		if (Convert.isEmpty(tests)) {
			throw new SystemException("tests can't be empty.");
		}

		for (final JiraTestCase test : tests) {
			test.validate();
		}

		super.validate();
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s]", Convert.getClassName(TestPlan.class), super.toString(), UtilsFormat.format(
				"tests", String.join(";", tests.stream().map(test -> test.toString()).collect(Collectors.toList()))));
	}

	@Override
	public boolean checkLabels() throws SystemException {
		final boolean testPlanFlag = getLabels().indexOf(TaskLabel.Testplan) > -1 ? true : false;

		if (testPlanFlag) {
			if (TestPlan.logger.isDebugEnabled()) {
				TestPlan.logger.debug(String.format(
						"Testplan Label is set, for Testplan from Class \"%s\", as it was expected to be.",
						this.getClass().getName()));
			}
		}

		final boolean testCaseFlag = getLabels().indexOf(TaskLabel.Testcase) > -1 ? true : false;

		if (testCaseFlag) {
			if (TestPlan.logger.isDebugEnabled()) {
				TestPlan.logger.debug(String.format(
						"Testcase Label is set, for Testplan from Class \"%s\", as it was not expected to be.",
						this.getClass().getName()));
			}
		}

		final boolean testStepFlag = getLabels().indexOf(TaskLabel.Teststep) > -1 ? true : false;

		if (testStepFlag) {
			if (TestPlan.logger.isDebugEnabled()) {
				TestPlan.logger.debug(String.format(
						"Teststep Label is set, for Testplan from Class \"%s\", as it was not expected to be.",
						this.getClass().getName()));
			}
		}

		return (testPlanFlag) && (!testCaseFlag) && (!testStepFlag);
	}
}
