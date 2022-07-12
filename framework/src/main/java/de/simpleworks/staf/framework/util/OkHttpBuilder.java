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

	public OkHttpBuilder() {
		throw new InstantiationError("utility class.");
	}

	public static OkHttpClientRecipe buildOkHttpClientRecipe() throws SystemException {
		final BrowserMobProxyServer proxy = OkHttpBuilder.proxyServerProperties.isProxyEnabled()
				? OkHttpBuilder.setUpProxyServer()
				: null;
		return new OkHttpClientRecipe(OkHttpBuilder.httpClientProperties.isIgnoreCertificate(),
				OkHttpBuilder.httpClientProperties.getCookiePolicy(),
				OkHttpBuilder.httpClientProperties.getLoggingLevel(), proxy,
				OkHttpBuilder.httpClientProperties.isRetryConnection(),
				OkHttpBuilder.httpClientProperties.getTimeout());
	}

	private static BrowserMobProxyServer setUpProxyServer() {
		return ProxyUtils.createProxyServer(OkHttpBuilder.proxyServerProperties, TestcaseKindEnum.API_TESTCASE);
	}
}
