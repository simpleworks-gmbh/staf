package de.simpleworks.staf.plugin.maven.testflo.commons.enums;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;

import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.utils.Convert;

public class Configuration {

	private static final Logger logger = LogManager.getLogger(Configuration.class);

	private static final String TESTFLO_CONFIGURATION = "testflo.configuration";
	private static final String TESTFLO_PROPERTIES = "classpath:testflo.properties";

	private static final Configuration instance = new Configuration();

	private final Properties properties;

	private Configuration() {
		String resource = System.getProperty(Configuration.TESTFLO_CONFIGURATION, null);
		if (Configuration.logger.isDebugEnabled()) {
			Configuration.logger.debug(String.format("system property: '%s' has value: '%s'.",
					Configuration.TESTFLO_CONFIGURATION, resource));
		}

		if (Convert.isEmpty(resource)) {
			resource = Configuration.TESTFLO_PROPERTIES;
			Configuration.logger.debug(String.format("system property: '%s' not set, use default: '%s'.",
					Configuration.TESTFLO_CONFIGURATION, resource));
		}

		if (Configuration.logger.isDebugEnabled()) {
			Configuration.logger.debug(String.format("read properties from: '%s'.", resource));
		}

		try {
			properties = Configuration.readProperties(resource);
		} catch (final SystemException ex) {
			final String message = String.format("can't read properties from '%s'.", resource);
			Configuration.logger.error(message, ex);
			throw new RuntimeException(message);
		}
	}

	// FIXME this method should be mover to de.simpleworks.commons.utils.UtilsIO
	public static Properties readProperties(final String resource) throws SystemException {
		Assert.assertNotNull("resource can't be null.", resource);

		if (Configuration.logger.isDebugEnabled()) {
			Configuration.logger.debug(String.format("read properties from resource: '%s'.", resource));
		}

		final Properties result = new Properties();

		try {
			final URL url = new URL(resource);
			if (Configuration.logger.isDebugEnabled()) {
				Configuration.logger.debug(String.format("url: '%s'.", url));
			}

			try (final InputStream stream = url.openStream();) {
				if (stream == null) {
					throw new SystemException(String.format("can't open stream for resource: '%s'.", resource));
				}

				result.load(stream);
			}
		} catch (final IOException ex) {
			final String message = String.format("can't read data from resource: '%s'.", resource);
			Configuration.logger.error(message, ex);
			throw new SystemException(message);
		}

		return result;
	}

	public static final synchronized Configuration getInstance() {
		return Configuration.instance;
	}

	public final String get(final String key) {
		Assert.assertNotNull("key can't be null.", key);

		final String value = properties.getProperty(key, null);
		if (Convert.isEmpty(value)) {
			throw new RuntimeException(String.format("can't get value for key: '%s'.", key));
		}

		return value;
	}

	// FIXME move the method toInt to de.simpleworks.commons.utils.Convert
	public static int toInt(final String text) throws SystemException {
		Assert.assertFalse("text can't be null .", Convert.isEmpty(text));

		try {
			return Integer.parseInt(text);
		} catch (final NumberFormatException ex) {
			final String message = String.format("can't parse Integer from: '%s'.", text);
			Configuration.logger.error(message, ex);
			throw new SystemException(message);
		}
	}

	public final int getInt(final String key) {
		Assert.assertNotNull("key can't be null.", key);

		final String value = properties.getProperty(key, null);
		if (Convert.isEmpty(value)) {
			throw new RuntimeException(String.format("can't get value for key: '%s'.", key));
		}

		try {
			return Configuration.toInt(value);
		} catch (final SystemException ex) {
			final String message = String.format("key: '%s' has invalid value: '%s' (expected is an valid int).", key,
					value);
			Configuration.logger.error(message, ex);
			throw new RuntimeException(message);
		}
	}
}
