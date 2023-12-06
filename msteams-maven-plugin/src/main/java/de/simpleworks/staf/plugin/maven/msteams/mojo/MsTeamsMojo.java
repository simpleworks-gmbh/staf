package de.simpleworks.staf.plugin.maven.msteams.mojo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;

import com.google.inject.Guice;
import com.google.inject.Module;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.framework.enums.TestcaseKindEnum;
import de.simpleworks.staf.module.jira.module.JiraModule;
import de.simpleworks.staf.plugin.maven.msteams.module.MsTeamsModule;

public abstract class MsTeamsMojo extends AbstractMojo {

	private static final Logger logger = LogManager.getLogger(MsTeamsMojo.class);

	public MsTeamsMojo(final Module... modules) {

		if (Convert.isEmpty(modules)) {
			if (MsTeamsMojo.logger.isDebugEnabled()) {
				MsTeamsMojo.logger.debug(String.format("No Custom-Modules will be injected."));
			}
			
			TestcaseKindEnum test = TestcaseKindEnum.API_TESTCASE;
		}
		try {
			// load guice models
			Guice.createInjector(UtilsCollection.add(Module.class, modules, new MsTeamsModule(), new JiraModule()))
					.injectMembers(this);
		} catch (final Exception ex) {
			final String message = String.format("Cannot initiate Mojo of Type \"%s\" , due to: \"%s\".",
					this.getClass().getName());
			MsTeamsMojo.logger.error(message, ex);
			throw new RuntimeException(message);
		}
	}

}
