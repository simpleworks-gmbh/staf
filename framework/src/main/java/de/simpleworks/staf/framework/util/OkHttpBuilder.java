package de.simpleworks.staf.framework.util;

import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.framework.api.httpclient.properties.HttpClientProperties;
import de.simpleworks.staf.framework.api.proxy.ProxyUtils;
import de.simpleworks.staf.framework.api.proxy.properties.ProxyServerProperties;
import de.simpleworks.staf.framework.enums.TestcaseKindEnum;
import net.lightbody.bmp.BrowserMobProxyServer;

public class OkHttpBuilder {
	private static final HttpClientProperties httpClientProperties = HttpClientProperties.getInstance();
	private static final ProxyServerProperties proxyServerProperties = ProxyServerProperties.getInstance();

	private static BrowserMobProxyServer browsermobProxy = null;

	public OkHttpBuilder() {
		throw new InstantiationError("utility class.");
	}

	public static OkHttpClientRecipe buildOkHttpClientRecipe() throws SystemException {
		final BrowserMobProxyServer proxy = OkHttpBuilder.proxyServerProperties.isProxyEnabled()
				? OkHttpBuilder.setUpProxyServer()
				: null;
		return new OkHttpClientRecipe(OkHttpBuilder.httpClientProperties.isIgnoreCertificate(),
				OkHttpBuilder.httpClientProperties.getCookiePolicy(),
				OkHttpBuilder.httpClientProperties.getLoggingLevel(), proxy);
	}

	private static BrowserMobProxyServer setUpProxyServer() {
		if (OkHttpBuilder.browsermobProxy == null) {
			OkHttpBuilder.browsermobProxy = ProxyUtils.createProxyServer(OkHttpBuilder.proxyServerProperties,
					TestcaseKindEnum.API_TESTCASE);
		} else if (OkHttpBuilder.browsermobProxy.isStopped()) {
			OkHttpBuilder.browsermobProxy = ProxyUtils.createProxyServer(OkHttpBuilder.proxyServerProperties,
					TestcaseKindEnum.API_TESTCASE);
		}

		return OkHttpBuilder.browsermobProxy;
	}
}
