package de.simpleworks.staf.module.jira.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.module.jira.util.enums.TaskLabel;

public class JiraTestCase extends Task {
	private static final long serialVersionUID = -5118384850060834253L;

	private static final Logger logger = LogManager.getLogger(JiraTestCase.class);

	private List<TestStep> steps;

	public JiraTestCase() {
		this.steps = new ArrayList<>();
	}

	public List<TestStep> getSteps() {
		return steps;
	}

	public void setSteps(final List<TestStep> steps) {
		this.steps = steps;
	}

	@Override
	public void validate() throws SystemException {
		if (JiraTestCase.logger.isInfoEnabled()) {
			JiraTestCase.logger.info(String.format("validate %s..", this));
		}

		if (Convert.isEmpty(steps)) {
			throw new SystemException("steps can't be empty.");
		}

		for (final TestStep step : steps) {
			step.validate();
		}

		super.validate();
	}

	@Override
	public String toString() {
		return String.format("Class is %s, and consists of [%s, [%s]]", this.getClass(),
				String.join(";", steps.stream().map(step -> step.toString()).collect(Collectors.toList())),
				super.toString());
	}

	@Override
	public boolean checkLabels() throws SystemException {

		final boolean testPlanFlag = getLabels().indexOf(TaskLabel.Testplan) > -1 ? true : false;

		if (testPlanFlag) {
			if (JiraTestCase.logger.isDebugEnabled()) {
				JiraTestCase.logger.debug(String.format(
						"Testplan Label is set, for Testplan from Class \"%s\", as it was expected to be.",
						this.getClass().getName()));
			}
		}

		final boolean testCaseFlag = getLabels().indexOf(TaskLabel.Testcase) > -1 ? true : false;

		if (testCaseFlag) {
			if (JiraTestCase.logger.isDebugEnabled()) {
				JiraTestCase.logger.debug(String.format(
						"Testcase Label is set, for Testplan from Class \"%s\", as it was expected to be.",
						this.getClass().getName()));
			}
		}

		final boolean testStepFlag = getLabels().indexOf(TaskLabel.Teststep) > -1 ? true : false;

		if (testStepFlag) {
			if (JiraTestCase.logger.isDebugEnabled()) {
				JiraTestCase.logger.debug(String.format(
						"Teststep Label is set, for Testplan from Class \"%s\", as it was not expected to be.",
						this.getClass().getName()));
			}
		}

		return (!testPlanFlag) && (testCaseFlag) && (!testStepFlag);
	}
}
