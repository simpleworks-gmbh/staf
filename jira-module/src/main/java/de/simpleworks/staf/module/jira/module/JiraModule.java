package de.simpleworks.staf.module.jira.module;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.module.jira.util.JiraProperties;

public class JiraModule extends AbstractModule {
	private static final Logger logger = LogManager.getLogger(JiraModule.class);

	private final JiraProperties properties;
	private JiraRestClient client;

	public JiraModule() {
		properties = JiraProperties.getInstance();
	}

	@Override
	protected void configure() {
		URI uri = null;

		try {
			uri = getJiraUri();
			final JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
			client = factory.createWithBasicHttpAuthentication(uri, properties.getUsername(), properties.getPassword());
		} catch (final Throwable th) {
			final String msg = String.format("can't create jira rest client (url: '%s', user: '%s')", uri,
					properties.getUsername());
			JiraModule.logger.error(msg, th);
			throw new RuntimeException(msg);
		}
	}

	@Provides
	public IssueRestClient getIssueRestClient() {
		return client.getIssueClient();
	}

	private URI getJiraUri() throws SystemException {
		if (properties == null) {
			throw new IllegalArgumentException("configuration can't be null.");
		}

		final URL url = properties.getUrl();
		if (url == null) {
			throw new IllegalArgumentException("url can't be null.");
		}

		URI uri;
		try {
			uri = url.toURI();
		} catch (final URISyntaxException ex) {
			throw new SystemException(
					String.format("can't parse URL %s to URI, due to %s.", url.toString(), ex.getMessage()));
		}

		return uri;
	}
}
