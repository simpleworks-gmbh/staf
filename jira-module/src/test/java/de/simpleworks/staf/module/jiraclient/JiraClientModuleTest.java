package de.simpleworks.staf.module.jiraclient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import de.simpleworks.staf.module.jira.module.JiraModule;

class JiraClientModuleTest {

	private static final Logger logger = LogManager.getLogger(JiraClientModuleTest.class);

	@Test
	void testJiraClientModule() {

		if (JiraClientModuleTest.logger.isInfoEnabled()) {
			JiraClientModuleTest.logger.info("start..");
		}

		final JiraClientModuleTestClass module = new JiraClientModuleTestClass();
		module.configure();
	}

	class JiraClientModuleTestClass extends JiraModule {

		@Override
		protected void configure() {
			super.configure();
		}
	}
}
