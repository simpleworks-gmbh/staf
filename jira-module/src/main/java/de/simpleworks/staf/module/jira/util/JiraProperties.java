package de.simpleworks.staf.module.jira.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.annotation.Property;
import de.simpleworks.staf.commons.annotation.Property.Default;
import de.simpleworks.staf.commons.exceptions.InvalidConfiguration;
import de.simpleworks.staf.commons.utils.PropertiesReader;
import de.simpleworks.staf.module.jira.util.consts.ClientConsts;

public class JiraProperties extends PropertiesReader {
	private static final Logger logger = LogManager.getLogger(JiraProperties.class);

	private static JiraProperties instance = null;

	@Property(ClientConsts.URL)
	private String url;

	@Property(ClientConsts.USERNAME)
	private String username;

	@Property(ClientConsts.PASSWORD)
	private String password;
	
	@Property(ClientConsts.ACESSTOKEN)
	private String accessToken;
	
	@Default("png")
	@Property(ClientConsts.SCREENSHOT_FORMAT)
	private String screenshot;

	@Default("0")
	@Property(ClientConsts.ISSUE_TYPE)
	private long issuetype;

	public URL getUrl() throws InvalidConfiguration {
		URL result;

		try {
			result = new URL(url);
		} catch (final MalformedURLException ex) {
			final String message = String.format("property: '%s' is invalid URL (value: '%s').", ClientConsts.URL, url);
			JiraProperties.logger.error(message, ex);

			throw new InvalidConfiguration(message);
		}

		return result;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
	
	public String getAccessToken() {
		return accessToken;
	}

	public String getScreenshotFormat() {
		return screenshot;
	}

	public long getIssueType() {
		return issuetype;
	}

	@Override
	protected Class<?> getClazz() {
		return JiraProperties.class;
	}

	@Override
	protected String getName() {
		return "*.properties";
	}

	public static final synchronized JiraProperties getInstance() {
		if (JiraProperties.instance == null) {
			if (JiraProperties.logger.isDebugEnabled()) {
				JiraProperties.logger.debug("create instance.");
			}

			JiraProperties.instance = new JiraProperties();
		}

		return JiraProperties.instance;
	}
}