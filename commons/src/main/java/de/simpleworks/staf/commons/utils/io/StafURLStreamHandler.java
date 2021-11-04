package de.simpleworks.staf.commons.utils.io;

import java.net.URLStreamHandler;

import de.simpleworks.staf.commons.utils.Convert;

public abstract class StafURLStreamHandler extends URLStreamHandler {
	private final String protocol;

	protected StafURLStreamHandler(final String protocol) {
		if (Convert.isEmpty(protocol)) {
			throw new IllegalArgumentException("protocol can't be null or empty string.");
		}

		this.protocol = protocol;
	}

	public final String getProtocol() {
		return protocol;
	}
}
