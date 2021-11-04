package de.simpleworks.staf.framework.api.httpclient.properties;

import java.net.CookiePolicy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.annotation.Property;
import de.simpleworks.staf.commons.annotation.Property.Default;
import de.simpleworks.staf.commons.utils.PropertiesReader;
import de.simpleworks.staf.framework.consts.FrameworkConsts;
import okhttp3.logging.HttpLoggingInterceptor.Level;

public class HttpClientProperties extends PropertiesReader {
	private static final Logger logger = LogManager.getLogger(HttpClientProperties.class);

	private static HttpClientProperties instance = null;

	@Default("BODY")
	@Property(FrameworkConsts.LOGGING_LEVEL)
	private Level loggingLevel;

	@Default("true")
	@Property(FrameworkConsts.IGNORE_CERTIFICATE)
	private boolean ignoreCertificate;

	@Default("ACCEPT_ALL")
	@Property(FrameworkConsts.COOKIE_POLICY)
	private String cookiePolicy;

	public Level getLoggingLevel() {
		return loggingLevel;
	}

	public boolean isIgnoreCertificate() {
		return ignoreCertificate;
	}

	public CookiePolicy getCookiePolicy() {
		switch (cookiePolicy) {
		case "ACCEPT_ALL":
			return CookiePolicy.ACCEPT_ALL;
		default:
			throw new IllegalArgumentException(
					String.format("the cookie policy '%s' was not implemented yet.", cookiePolicy));

		}
	}

	@Override
	protected Class<?> getClazz() {
		return HttpClientProperties.class;
	}

	public static final synchronized HttpClientProperties getInstance() {
		if (HttpClientProperties.instance == null) {
			if (HttpClientProperties.logger.isDebugEnabled()) {
				HttpClientProperties.logger.debug("create instance.");
			}

			HttpClientProperties.instance = new HttpClientProperties();
		}

		return HttpClientProperties.instance;
	}
}
