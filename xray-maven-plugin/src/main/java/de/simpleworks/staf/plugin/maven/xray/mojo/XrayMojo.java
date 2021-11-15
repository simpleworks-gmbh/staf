package de.simpleworks.staf.plugin.maven.xray.mojo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;

import com.google.inject.Guice;
import com.google.inject.Module;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.module.jira.module.JiraModule;
import de.simpleworks.staf.plugin.maven.xray.module.XrayModule;

public abstract class XrayMojo extends AbstractMojo {

	private static final Logger logger = LogManager.getLogger(XrayMojo.class);

	public XrayMojo(final Module... modules) {

		if (Convert.isEmpty(modules)) {
			if (XrayMojo.logger.isDebugEnabled()) {
				XrayMojo.logger.debug(String.format("No Custom-Modules will be injected."));
			}
		}
		try {
			// load guice models
			Guice.createInjector(UtilsCollection.add(Module.class, modules, new XrayModule(), new JiraModule()))
					.injectMembers(this);
		} catch (final Exception ex) {
			final String message = String.format("Cannot initiate Mojo of Type \"%s\".", this.getClass().getName());
			XrayMojo.logger.error(message, ex);
			throw new RuntimeException(message);
		}
	}
}
