/*
 * Copyright (C) 2012 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Modifications copyright (C) 2021 Simpleworks GmbH
 */

package com.atlassian.jira.rest.client.internal.async;

import java.io.File;
import java.net.URI;
import java.util.Date;

import javax.annotation.Nonnull;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.apache.httpcomponents.DefaultHttpClientFactory;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.executor.ThreadLocalContextManager;

/**
 * Factory for asynchronous http clients.
 *
 * @since v2.0
 */
public class AsynchronousHttpClientFactory {
	@SuppressWarnings("static-method")
	public DisposableHttpClient createClient(final URI serverUri, final AuthenticationHandler authenticationHandler) {
		final HttpClientOptions options = new HttpClientOptions();

		@SuppressWarnings({ "rawtypes", "unchecked" })
		final DefaultHttpClientFactory defaultHttpClientFactory = new DefaultHttpClientFactory(new NoOpEventPublisher(),
				new RestClientApplicationProperties(serverUri), new ThreadLocalContextManager() {
					@Override
					public Object getThreadLocalContext() {
						return null;
					}

					@Override
					public void setThreadLocalContext(final Object context) {
						// nothing to do.
					}

					@Override
					public void clearThreadLocalContext() {
						// nothing to do.
					}
				});

		final HttpClient httpClient = defaultHttpClientFactory.create(options);
		return new AtlassianHttpClientDecorator(httpClient, authenticationHandler) {
			@Override
			public void destroy() throws Exception {
				defaultHttpClientFactory.dispose(httpClient);
			}
		};
	}

	public static DisposableHttpClient createClient(final HttpClient client) {
		return new AtlassianHttpClientDecorator(client, null) {

			@Override
			public void destroy() throws Exception {
				// This should never be implemented. This is simply creation of a wrapper
				// for AtlassianHttpClient which is extended by a destroy method.
				// Destroy method should never be called for AtlassianHttpClient coming from
				// a client! Imagine you create a RestClient, pass your own HttpClient there
				// and it gets destroy.
			}
		};
	}

	private static class NoOpEventPublisher implements EventPublisher {
		public NoOpEventPublisher() {
			// nothing to do.
		}

		@Override
		public void publish(final Object o) {
			// nothing to do.
		}

		@Override
		public void register(final Object o) {
			// nothing to do.
		}

		@Override
		public void unregister(final Object o) {
			// nothing to do.
		}

		@Override
		public void unregisterAll() {
			// nothing to do.
		}
	}

	/**
	 * These properties are used to present JRJC as a User-Agent during http
	 * requests.
	 */
	private static class RestClientApplicationProperties implements ApplicationProperties {
		private final String baseUrl;

		RestClientApplicationProperties(final URI jiraURI) {
			this.baseUrl = jiraURI.getPath();
		}

		@Override
		public String getBaseUrl() {
			return baseUrl;
		}

		/**
		 * We'll always have an absolute URL as a client.
		 */
		@Nonnull
		@Override
		public String getBaseUrl(final UrlMode urlMode) {
			return baseUrl;
		}

		@Nonnull
		@Override
		public String getDisplayName() {
			return "Atlassian JIRA Rest Java Client";
		}

		@Nonnull
		@Override
		public String getPlatformId() {
			return ApplicationProperties.PLATFORM_JIRA;
		}

		@Nonnull
		@Override
		public String getVersion() {
			return "STAF Client";
		}

		@Nonnull
		@Override
		public Date getBuildDate() {
			// TODO implement using MavenUtils, JRJC-123
			throw new UnsupportedOperationException();
		}

		@Nonnull
		@Override
		public String getBuildNumber() {
			// TODO implement using MavenUtils, JRJC-123
			return String.valueOf(0);
		}

		@Override
		public File getHomeDirectory() {
			return new File(".");
		}

		@Override
		public String getPropertyValue(final String s) {
			throw new UnsupportedOperationException("Not implemented");
		}
	}
}
