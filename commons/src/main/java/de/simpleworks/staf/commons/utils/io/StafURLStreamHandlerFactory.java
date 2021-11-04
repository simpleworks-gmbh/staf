package de.simpleworks.staf.commons.utils.io;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.utils.Convert;

public class StafURLStreamHandlerFactory implements URLStreamHandlerFactory {
	private static final Logger logger = LogManager.getLogger(StafURLStreamHandlerFactory.class);

	private final Map<String, StafURLStreamHandler> handlers;

	public StafURLStreamHandlerFactory() {
		handlers = new HashMap<>();
	}

	public void addHandler(final StafURLStreamHandler handler) {
		if (handler == null) {
			throw new IllegalArgumentException("handler can't be null.");
		}

		if (StafURLStreamHandlerFactory.logger.isDebugEnabled()) {
			StafURLStreamHandlerFactory.logger.debug(String.format("add handler '%s' for protocol: '%s'.",
					Convert.getClassFullName(handler), handler.getProtocol()));
		}
		handlers.put(handler.getProtocol(), handler);
	}

	@Override
	public URLStreamHandler createURLStreamHandler(final String protocol) {
		if (Convert.isEmpty(protocol)) {
			throw new IllegalArgumentException("protocol can't be null or empty string.");
		}

		return handlers.get(protocol);
	}
}
