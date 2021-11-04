package de.simpleworks.staf.framework.elements.commons.properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.annotation.Property;
import de.simpleworks.staf.commons.annotation.Property.Default;
import de.simpleworks.staf.commons.utils.PropertiesReader;
import de.simpleworks.staf.framework.consts.CreateArtefactValue;
import de.simpleworks.staf.framework.consts.FrameworkConsts;
import de.simpleworks.staf.framework.enums.CreateArtefactEnum;

public class TestCaseProperties extends PropertiesReader {
	private static final Logger logger = LogManager.getLogger(TestCaseProperties.class);

	private static TestCaseProperties instance;

	@Default("TestcaseExecution")
	@Property(FrameworkConsts.TESTCASE_HEADER_NAME)
	private String testCaseHeaderName;

	@Default("X-REQUEST-ID")
	@Property(FrameworkConsts.TESTSTEP_HEADER_NAME)
	private String testStepHeaderName;

	@Default(CreateArtefactValue.ON_FAILURE)
	@Property(FrameworkConsts.TESTCASE_CREATE_ARTEFACT)
	private CreateArtefactEnum createArtefactOn;

	public String getTestCaseHeaderName() {
		return testCaseHeaderName;
	}

	public String getTestStepHeaderName() {
		return testStepHeaderName;
	}

	public CreateArtefactEnum getCreateArtefactOn() {
		return createArtefactOn;
	}

	@Override
	protected Class<?> getClazz() {
		return TestCaseProperties.class;
	}

	public static final synchronized TestCaseProperties getInstance() {
		if (TestCaseProperties.instance == null) {
			if (TestCaseProperties.logger.isDebugEnabled()) {
				TestCaseProperties.logger.debug("create instance.");
			}

			TestCaseProperties.instance = new TestCaseProperties();
		}

		return TestCaseProperties.instance;
	}
}
