package de.simpleworks.staf.plugin.maven.testflo.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.annotation.Property;
import de.simpleworks.staf.commons.annotation.Property.Default;
import de.simpleworks.staf.commons.utils.PropertiesReader;
import de.simpleworks.staf.plugin.maven.testflo.consts.Consts;

public class TestFLOProperties extends PropertiesReader {
	private static final Logger logger = LogManager.getLogger(TestFLOProperties.class);

	private static TestFLOProperties instance = null;

	@Property(value = Consts.JIRA_REST_API, required = true)
	private String api;

	@Property(value = Consts.JIRA_REST_TMS, required = true)
	private String tms;

	@Default("1")
	@Property(Consts.JIRA_REST_TIMEOUT)
	private int timeout;

	@Default("false")
	@Property(Consts.JIRA_REST_SKIP_TIMEOUT)
	private boolean skipTimeout;

	public String getApi() {
		return api;
	}

	public String getTms() {
		return tms;
	}

	public int getTimeout() {
		return timeout;
	}

	public boolean isSkipTimeout() {
		return skipTimeout;
	}

	@Override
	protected Class<?> getClazz() {
		return TestFLOProperties.class;
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
