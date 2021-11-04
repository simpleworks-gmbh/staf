package de.simpleworks.staf.commons.utils.io;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.utils.Convert;

public class StafURLStreamHandlerConfiguration extends StafURLStreamHandler {
	private static final Logger logger = LogManager.getLogger(StafURLStreamHandlerConfiguration.class);
	private static final String PROTOCOL = "config";

	private final ClassLoader classLoader;

	public StafURLStreamHandlerConfiguration() {
		super(StafURLStreamHandlerConfiguration.PROTOCOL);

		this.classLoader = getClass().getClassLoader();
	}

	public StafURLStreamHandlerConfiguration(final ClassLoader classLoader) {
		super(StafURLStreamHandlerConfiguration.PROTOCOL);

		if (classLoader == null) {
			throw new IllegalArgumentException("classLoader can't be null.");
		}

		this.classLoader = classLoader;
	}

	@Override
	protected URLConnection openConnection(final URL url) throws IOException {
		if (url == null) {
			throw new IllegalArgumentException("url can't be null.");
		}

		if (StafURLStreamHandlerConfiguration.logger.isTraceEnabled()) {
			StafURLStreamHandlerConfiguration.logger.trace(String.format("url: '%s'.", url));
		}

		if (!StafURLStreamHandlerConfiguration.PROTOCOL.equals(url.getProtocol())) {
			throw new IOException(String.format("unexpected protocol: '%s' (expected is: '%s'.", url.getProtocol(),
					StafURLStreamHandlerConfiguration.PROTOCOL));
		}

		final String key = url.getPath();
		final String path = System.getProperty(key, null);
		if (Convert.isEmpty(path)) {
			throw new IOException(String.format("can't get value for key: '%s' (url: '%s').", key, url));
		}

		final URL resourceUrl = classLoader.getResource(path);
		if (StafURLStreamHandlerConfiguration.logger.isTraceEnabled()) {
			StafURLStreamHandlerConfiguration.logger.trace(String.format("resourceUrl: '%s'.", resourceUrl));
		}

		if (resourceUrl == null) {
			throw new IOException(String.format("can't find resource for url: '%s'.", url));
		}

		return resourceUrl.openConnection();
	}
}
