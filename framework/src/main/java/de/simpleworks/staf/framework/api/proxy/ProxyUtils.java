package de.simpleworks.staf.framework.api.proxy;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsNetwork;
import de.simpleworks.staf.data.exception.InvalidDataConstellationExcpetion;
import de.simpleworks.staf.framework.api.proxy.properties.ProxyServerProperties;
import de.simpleworks.staf.framework.elements.api.RewriteUrlObject;
import de.simpleworks.staf.framework.enums.TestcaseKindEnum;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.proxy.CaptureType;

public class ProxyUtils {
	private static final Logger logger = LogManager.getLogger(ProxyUtils.class);

	private static int getProxyPort(final ProxyServerProperties props, TestcaseKindEnum testcasekind) {
		if (props == null) {
			throw new IllegalArgumentException("props can't be null.");
		}

		if (testcasekind == null) {
			throw new IllegalArgumentException("testcasekind can't be null.");
		}

		int result;

		switch (testcasekind) {

		case API_TESTCASE:
			result = props.getAPIProxyPort();
			break;

		case GUI_TESTCASE:
			result = props.getGUIProxyPort();
			break;

		default:
			throw new IllegalArgumentException(
					String.format("testcasekind \"%s\" is not implemented yet.", testcasekind.getValue()));
		}

		if (result < 0) {
			throw new IllegalArgumentException("port can't be less than 0.");
		}

		if (result > 65535) {
			throw new IllegalArgumentException(
					String.format("port can't be more than 65535 but was %d.", Integer.valueOf(result)));
		}

		while (isPortAlreadyUsed(result)) {
			result += 1;

			if (result > 65535) {
				throw new RuntimeException(
						String.format("port can't be more than 65535 but was %d.", Integer.valueOf(result)));
			}

		}
		return result;
	}

	public static BrowserMobProxyServer addRewriteRules(final BrowserMobProxyServer proxy,
			final RewriteUrlObject rewrittenUrl) {
		if (proxy == null) {
			throw new IllegalArgumentException("proxy can't be null.");
		}

		if (rewrittenUrl == null) {
			throw new IllegalArgumentException("rewrittenUrl can't be null.");
		}

		if (ProxyUtils.logger.isDebugEnabled()) {
			ProxyUtils.logger.debug(String.format("add rewritte rule: '%s'.", rewrittenUrl));
		}

		BrowserMobProxyServer result;

		try {
			rewrittenUrl.validate();

			result = proxy;
			result.rewriteUrl(rewrittenUrl.getPattern(), rewrittenUrl.getRewriteExpression());
		} catch (final InvalidDataConstellationExcpetion ex) {
			// FIXME throw an exception
			ProxyUtils.logger.error("can't build RewriteRule.", ex);
			result = null;
		}

		return result;
	}

	/**
	 * @brief method to set up proxy instance
	 * @return already running proxy, null if it can't start (on the given port)
	 *         NOTE: if a proxy is already running, you will still get null.
	 */
	public static BrowserMobProxyServer createProxyServer(final ProxyServerProperties props,
			TestcaseKindEnum testcasekind) {
		final int proxyPort = ProxyUtils.getProxyPort(props, testcasekind);

		BrowserMobProxyServer result = new BrowserMobProxyServer();
		try {
			// set up capture types.
			final HashSet<CaptureType> enable = new HashSet<>();
			enable.add(CaptureType.REQUEST_HEADERS);
			enable.add(CaptureType.REQUEST_CONTENT);
			enable.add(CaptureType.RESPONSE_HEADERS);
			enable.add(CaptureType.RESPONSE_CONTENT);
			result.enableHarCaptureTypes(enable);

			result.setTrustAllServers(true);
			
			
			
			result.start(proxyPort);

		} catch (final Exception ex) {
			// FIXME throw an exception
			ProxyUtils.logger.error("can't set up proxy.", ex);
			result = null;
		}

		return result;
	}

	public static BrowserMobProxyServer setUpHeader(final BrowserMobProxyServer proxy,
			final Map<String, String> headers) {
		if (proxy == null) {
			throw new IllegalArgumentException("proxy can't be null.");
		}

		if ((headers == null) || Convert.isEmpty(headers)) {
			throw new IllegalArgumentException("headers can't be null or empty.");
		}

		final BrowserMobProxyServer result = proxy;

		for (final String key : headers.keySet()) {
			result.addHeader(key, headers.get(key));
		}

		return result;
	}

	private static boolean isPortAlreadyUsed(int port) {

		boolean result = false;

		try {
			final URI uri = new URI(String.format("PROTOCOL://0.0.0.0:%s", Integer.toString(port)));

			result = UtilsNetwork.isServerAvailable(uri);
		} catch (Exception ex) {
			ProxyUtils.logger.error(String.format("can't validate host '%s'.",
					String.format("0.0.0.0:%s", Integer.toString(port)), ex)); 
		}

		return result;
	}

}
