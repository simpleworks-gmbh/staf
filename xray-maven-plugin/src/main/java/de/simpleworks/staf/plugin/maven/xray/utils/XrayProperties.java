package de.simpleworks.staf.plugin.maven.xray.utils;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.annotation.Property;
import de.simpleworks.staf.commons.exceptions.InvalidConfiguration;
import de.simpleworks.staf.commons.utils.PropertiesReader;
import de.simpleworks.staf.plugin.maven.xray.consts.XrayConsts;

public class XrayProperties extends PropertiesReader {
	private static final Logger logger = LogManager.getLogger(XrayProperties.class);

	private static XrayProperties instance;

	@Property(value = XrayConsts.XRAY_AUTHENTICATE_URL, required = true)
	private String authenticationUrl;

	@Property(value = XrayConsts.XRAY_GRAPHQL_APT_URL, required = true)
	private String apiUrl;

	@Property(value = XrayConsts.XRAY_CLIENT_ID, required = true)
	private String clientId;

	@Property(value = XrayConsts.XRAY_CLIENT_SECRET, required = true)
	private String clientSecret;

	@Property(value = XrayConsts.JIRA_PASSWORD, required = true)
	private String jiraPassword;

	@Property(value = XrayConsts.JIRA_USERNAME, required = true)
	private String jiraUsername;

	@Property(value = XrayConsts.JIRA_URL, required = true)
	private String jiraUrl;

	public URL getAuthenticationUrl() throws InvalidConfiguration {
		URL result;

		try {
			result = new URL(authenticationUrl);
		} catch (final MalformedURLException ex) {

			final String message = String.format("property: \"%s\" is invalid URL (value: \"%s\").",
					XrayConsts.XRAY_AUTHENTICATE_URL, authenticationUrl);
			XrayProperties.logger.error(message, ex);

			throw new InvalidConfiguration(message);
		}

		return result;
	}

	public URL getApiUrl() throws InvalidConfiguration {
		URL result;

		try {
			result = new URL(apiUrl);
		} catch (final MalformedURLException ex) {

			final String message = String.format("property: \"%s\" is invalid URL (value: \"%s\").",
					XrayConsts.XRAY_GRAPHQL_APT_URL, apiUrl);
			XrayProperties.logger.error(message, ex);

			throw new InvalidConfiguration(message);
		}

		return result;
	}

	public URL getJiraUrl() throws InvalidConfiguration {
		URL result;

		try {
			result = new URL(jiraUrl);
		} catch (final MalformedURLException ex) {

			final String message = String.format("property: \"%s\" is invalid URL (value: \"%s\").",
					XrayConsts.JIRA_URL, jiraUrl);
			XrayProperties.logger.error(message, ex);

			throw new InvalidConfiguration(message);
		}

		return result;
	}

	public String getClientId() {
		return clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public String getJiraPassword() {
		return jiraPassword;
	}

	public String getJiraUsername() {
		return jiraUsername;
	}

	@Override
	protected Class<?> getClazz() {
		return XrayProperties.class;
	}

	@Override
	protected String getName() {
		return "*.properties";
	}

	public static final synchronized XrayProperties getInstance() {
		if (XrayProperties.instance == null) {
			if (XrayProperties.logger.isDebugEnabled()) {
				XrayProperties.logger.debug("create instance.");
			}

			XrayProperties.instance = new XrayProperties();
		}

		return XrayProperties.instance;
	}
}