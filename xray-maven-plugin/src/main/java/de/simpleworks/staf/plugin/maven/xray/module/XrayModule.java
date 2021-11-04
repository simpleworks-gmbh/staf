package de.simpleworks.staf.plugin.maven.xray.module;

import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import de.simpleworks.staf.commons.exceptions.InvalidConfiguration;
import de.simpleworks.staf.plugin.maven.xray.commons.Xray;
import de.simpleworks.staf.plugin.maven.xray.consts.XrayConsts;
import de.simpleworks.staf.plugin.maven.xray.utils.XrayProperties;
import okhttp3.OkHttpClient;

public class XrayModule extends AbstractModule {

	private static final Logger logger = LogManager.getLogger(XrayModule.class);

	private final XrayProperties xrayProperties;

	public XrayModule() {
		this.xrayProperties = XrayProperties.getInstance();
	}

	@Override
	protected void configure() {

		try {

			bind(URL.class).annotatedWith(Names.named(XrayConsts.XRAY_AUTHENTICATE_URL))
					.toInstance(getAuthenticationUrl());
			bind(URL.class).annotatedWith(Names.named(XrayConsts.XRAY_GRAPHQL_APT_URL)).toInstance(getApiUrl());
			bind(String.class).annotatedWith(Names.named(XrayConsts.JIRA_USERNAME)).toInstance(getJiraUsername());
			bind(String.class).annotatedWith(Names.named(XrayConsts.JIRA_PASSWORD)).toInstance(getJiraPassword());
			bind(String.class).annotatedWith(Names.named(XrayConsts.XRAY_CLIENT_ID)).toInstance(getClientId());
			bind(String.class).annotatedWith(Names.named(XrayConsts.XRAY_CLIENT_SECRET)).toInstance(getClientSecret());
			bind(URL.class).annotatedWith(Names.named(XrayConsts.JIRA_URL)).toInstance(getJiraUrl());
			bind(Xray.class).annotatedWith(Names.named(XrayConsts.XRAY_CLIENT)).toInstance(getXrayClient());
		} catch (final Exception ex) {
			final String msg = String.format("Cannot configure Testcase, due \"%s\".", ex);
			XrayModule.logger.error(msg, ex);
			throw new RuntimeException(msg);
		}
	}

	private URL getAuthenticationUrl() throws InvalidConfiguration {
		final URL result = this.xrayProperties.getAuthenticationUrl();
		return result;
	}

	private URL getApiUrl() throws InvalidConfiguration {
		final URL result = this.xrayProperties.getApiUrl();
		return result;
	}

	private String getJiraPassword() {
		final String result = this.xrayProperties.getJiraPassword();
		return result;
	}

	private String getJiraUsername() {
		final String result = this.xrayProperties.getJiraUsername();
		return result;
	}

	private String getClientId() {
		final String result = this.xrayProperties.getClientId();
		return result;
	}

	private String getClientSecret() {
		final String result = this.xrayProperties.getClientSecret();
		return result;
	}

	private URL getJiraUrl() throws InvalidConfiguration {
		final URL result = this.xrayProperties.getJiraUrl();
		return result;
	}

	private Xray getXrayClient() throws Exception {
		final Xray result = new Xray(getApiUrl(), getAuthenticationUrl(), new OkHttpClient());

		// Get token for X-Ray GraphQL API
		try {
			result.setToken(getClientId(), getClientSecret());
		} catch (final Exception ex) {
			final String msg = "can't fetch token.";
			XrayModule.logger.error(msg, ex);
			throw new Exception(msg);
		}

		return result;
	}
}
