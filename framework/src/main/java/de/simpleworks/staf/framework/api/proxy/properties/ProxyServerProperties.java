package de.simpleworks.staf.framework.api.proxy.properties;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.annotation.Property;
import de.simpleworks.staf.commons.annotation.Property.Default;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.PropertiesReader;
import de.simpleworks.staf.framework.consts.FrameworkConsts;

public class ProxyServerProperties extends PropertiesReader {
	private static final Logger logger = LogManager.getLogger(ProxyServerProperties.class);

	private static ProxyServerProperties instance = null;

	private final static String PROXY_HEADERS_LIST_LITERAL = ";";
	private final static String PROXY_HEADERS_KEY_VALUE_LITERAL = ":";

	@Property(FrameworkConsts.PROXY_ENABLED)
	private boolean proxyEnabled;

	@Default("8888")
	@Property(FrameworkConsts.PROXY_GUI_PORT)
	private String proxyGUIPort;

	@Default("9999")
	@Property(FrameworkConsts.PROXY_API_PORT)
	private String proxyAPIPort;

	// "PROXY_HEADERS_LIST_LITERAL" separated list of "Key:Value"-Pairs
	@Default(Convert.EMPTY_STRING)
	@Property(FrameworkConsts.PROXY_HEADERS)
	private String proxyHeaders;
 
	@Property(FrameworkConsts.PROXY_NEOLOAD_ENABLED)
	private boolean neoloadProxyEnabled;

	@Default("127.0.0.1")
	@Property(FrameworkConsts.PROXY_NEOLOAD_HOST)
	private String proxyNLHost;

	@Default("8090")
	@Property(FrameworkConsts.PROXY_NEOLOAD_PORT)
	private String proxyNLPort;

	public boolean isProxyEnabled() {
		return proxyEnabled;
	}

	public int getGUIProxyPort() {
		return Integer.parseInt(proxyGUIPort);
	}

	public int getAPIProxyPort() {
		return Integer.parseInt(proxyAPIPort);
	}

	public boolean isNeoloadProxyEnabled() {
		return neoloadProxyEnabled;
	}

	public String getProxyNLHost() {
		return proxyNLHost;
	}

	public int getProxyNLPort() {
		return Integer.parseInt(proxyNLPort);
	}

	public Map<String, String> getHeaders() {
		final Map<String, String> result = new HashedMap<>();

		if (Convert.isEmpty(proxyHeaders)) {
			return result;
		}

		final List<String> keyValuePairs = Arrays
				.asList(proxyHeaders.split(ProxyServerProperties.PROXY_HEADERS_LIST_LITERAL));
		if (Convert.isEmpty(keyValuePairs)) {
			return result;
		}

		for (final String keyValuePair : keyValuePairs) {
			final String[] literals = keyValuePair.split(ProxyServerProperties.PROXY_HEADERS_KEY_VALUE_LITERAL);

			if (literals.length != 2) {
				if (ProxyServerProperties.logger.isDebugEnabled()) {
					ProxyServerProperties.logger.debug(String.format("header '%s' is invalid.", keyValuePair));
				}

				continue;
			}

			final String key = literals[0];
			final String value = literals[1];

			if (result.containsKey(key)) {
				if (ProxyServerProperties.logger.isDebugEnabled()) {
					ProxyServerProperties.logger.debug(
							String.format("key '%s' was already set (current value: '%s'), will be updated to '%s'.",
									key, result.get(key), value));
				}
			}

			result.put(key, value);
		}

		return result;
	}

	@Override
	protected Class<?> getClazz() {
		return ProxyServerProperties.class;
	}

	public static final synchronized ProxyServerProperties getInstance() {
		if (ProxyServerProperties.instance == null) {
			if (ProxyServerProperties.logger.isDebugEnabled()) {
				ProxyServerProperties.logger.debug("create instance.");
			}

			ProxyServerProperties.instance = new ProxyServerProperties();
		}

		return ProxyServerProperties.instance;
	}

}
