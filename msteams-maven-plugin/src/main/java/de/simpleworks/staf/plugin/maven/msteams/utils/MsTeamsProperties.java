package de.simpleworks.staf.plugin.maven.msteams.utils;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.annotation.Property;
import de.simpleworks.staf.commons.exceptions.InvalidConfiguration;
import de.simpleworks.staf.commons.utils.PropertiesReader;
import de.simpleworks.staf.module.jira.util.consts.ClientConsts;
import de.simpleworks.staf.plugin.maven.msteams.consts.MsTeamsConsts;

public class MsTeamsProperties extends PropertiesReader {

	private static final Logger logger = LogManager.getLogger(MsTeamsProperties.class);

	@Property(MsTeamsConsts.TEAMS_WEBHOOK)
	private String webhook;

	private static MsTeamsProperties instance;

	@Override
	protected Class<MsTeamsProperties> getClazz() {
		return MsTeamsProperties.class;
	}

	public URL getWebhook() throws InvalidConfiguration {

		URL result;

		try {
			result = new URL(webhook);
		} catch (final MalformedURLException ex) {

			final String message = String.format("property: \"%s\" is invalid URL (value: \"%s\").", ClientConsts.URL,
					webhook);
			MsTeamsProperties.logger.error(message, ex);

			throw new InvalidConfiguration(message);
		}

		return result;
	}

	public static synchronized MsTeamsProperties getInstance() {

		if (MsTeamsProperties.instance == null) {
			MsTeamsProperties.instance = new MsTeamsProperties();
		}

		return MsTeamsProperties.instance;
	}
}