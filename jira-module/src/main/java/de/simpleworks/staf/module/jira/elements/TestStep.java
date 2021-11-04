package de.simpleworks.staf.module.jira.elements;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.module.jira.util.enums.TaskLabel;

public class TestStep extends Task {
	private static final long serialVersionUID = 8981290089246501670L;

	private static final Logger logger = LogManager.getLogger(TestStep.class);

	@Override
	public long getIssueType() {
		// FIXME move it into configuration file.
		return 10102;
	}

	@Override
	public boolean checkLabels() throws SystemException {
		final boolean testPlanFlag = getLabels().indexOf(TaskLabel.Testplan) > -1 ? true : false;
		if (testPlanFlag) {
			if (TestStep.logger.isDebugEnabled()) {
				TestStep.logger.debug(String.format(
						"Testplan Label is set, for Testplan from Class \"%s\", as it was not expected to be.",
						this.getClass().getName()));
			}
		}

		final boolean testCaseFlag = getLabels().indexOf(TaskLabel.Testcase) > -1 ? true : false;
		if (testCaseFlag) {
			if (TestStep.logger.isDebugEnabled()) {
				TestStep.logger.debug(String.format(
						"Testcase Label is set, for Testplan from Class \"%s\", as it was not expected to be.",
						this.getClass().getName()));
			}
		}

		final boolean testStepFlag = getLabels().indexOf(TaskLabel.Teststep) > -1 ? true : false;
		if (testStepFlag) {
			if (TestStep.logger.isDebugEnabled()) {
				TestStep.logger.debug(String.format(
						"Teststep Label is set, for Testplan from Class \"%s\", as it was expected to be.",
						this.getClass().getName()));
			}
		}

		return (!testPlanFlag) && (!testCaseFlag) && (testStepFlag);
	}
}
