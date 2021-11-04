package de.simpleworks.staf.commons.utils.io;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StafURLStreamHandlerClasspath extends StafURLStreamHandler {
	private static final Logger logger = LogManager.getLogger(StafURLStreamHandlerClasspath.class);
	private static final String PROTOCOL = "classpath";
	private final ClassLoader classLoader;

	public StafURLStreamHandlerClasspath() {
		super(StafURLStreamHandlerClasspath.PROTOCOL);

		this.classLoader = getClass().getClassLoader();
	}

	public StafURLStreamHandlerClasspath(final ClassLoader classLoader) {
		super(StafURLStreamHandlerClasspath.PROTOCOL);

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

		if (StafURLStreamHandlerClasspath.logger.isTraceEnabled()) {
			StafURLStreamHandlerClasspath.logger.trace(String.format("url: '%s'.", url));
		}

		if (!StafURLStreamHandlerClasspath.PROTOCOL.equals(url.getProtocol())) {
			throw new IOException(String.format("unexpected protocol: '%s' (expected is: '%s'.", url.getProtocol(),
					StafURLStreamHandlerClasspath.PROTOCOL));
		}

		final URL resourceUrl = classLoader.getResource(url.getPath());
		if (StafURLStreamHandlerClasspath.logger.isTraceEnabled()) {
			StafURLStreamHandlerClasspath.logger.trace(String.format("resourceUrl: '%s'.", resourceUrl));
		}

		if (resourceUrl == null) {
			throw new IOException(String.format("can't find resource for url: '%s'.", url));
		}

		return resourceUrl.openConnection();
	}
}
