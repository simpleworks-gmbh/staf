package de.simpleworks.staf.plugin.maven.testflo.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.annotation.Property;
import de.simpleworks.staf.commons.utils.PropertiesReader;
import de.simpleworks.staf.plugin.maven.testflo.consts.Consts;

public class TestFLOProperties extends PropertiesReader {
	private static final Logger logger = LogManager.getLogger(TestFLOProperties.class);

	private static TestFLOProperties instance = null;

	@Property(Consts.JIRA_REST_API)
	private String api;

	@Property(Consts.JIRA_REST_TMS)
	private String tms;

	@Override
	protected Class<?> getClazz() {
		return TestFLOProperties.class;
	}

	public String getApi() {
		return api;
	}

	public String getTms() {
		return tms;
	}

	public static final synchronized TestFLOProperties getInstance() {
		if (TestFLOProperties.instance == null) {
			if (TestFLOProperties.logger.isDebugEnabled()) {
				TestFLOProperties.logger.debug("create instance.");
			}

			TestFLOProperties.instance = new TestFLOProperties();
		}

		return TestFLOProperties.instance;
	}
}
