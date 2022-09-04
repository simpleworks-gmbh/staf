package de.simpleworks.staf.framework.util;

import java.net.Proxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.framework.api.httpclient.properties.HttpClientProperties;
import de.simpleworks.staf.framework.api.proxy.ProxyUtils;
import de.simpleworks.staf.framework.api.proxy.properties.ProxyServerProperties;
import de.simpleworks.staf.framework.consts.FrameworkConsts;
import de.simpleworks.staf.framework.enums.TestcaseKindEnum;
import net.lightbody.bmp.BrowserMobProxyServer;

/**
 * @info support only nl proxy
 * */
public class OkHttpBuilder {

	private static final Logger logger = LogManager.getLogger(OkHttpBuilder.class);

	private static final HttpClientProperties httpClientProperties = HttpClientProperties.getInstance();
	private static final ProxyServerProperties proxyServerProperties = ProxyServerProperties.getInstance();

	private static BrowserMobProxyServer browsermobProxy = null;
	private static Proxy proxy = null;

	public OkHttpBuilder() {
		throw new InstantiationError("utility class.");
	}

	public static OkHttpClientRecipe buildOkHttpClientRecipe() throws SystemException {

		if (OkHttpBuilder.proxyServerProperties.isProxyEnabled()
				&& OkHttpBuilder.proxyServerProperties.isNeoloadProxyEnabled()) {
			logger.error(String.format("can't run both proxies at once, consider to set '%s' or '%s' to false.",
					FrameworkConsts.PROXY_ENABLED, FrameworkConsts.PROXY_NEOLOAD_ENABLED));
			return null;
		}

		OkHttpClientRecipe result = null;

		if (OkHttpBuilder.proxyServerProperties.isProxyEnabled()) {
			final BrowserMobProxyServer browserMobProxyServer = OkHttpBuilder.setUpBrowserMobProxyServer();

			result = new OkHttpClientRecipe(OkHttpBuilder.httpClientProperties.isIgnoreCertificate(),
					OkHttpBuilder.httpClientProperties.getCookiePolicy(),
					OkHttpBuilder.httpClientProperties.getLoggingLevel(), browserMobProxyServer,
					OkHttpBuilder.httpClientProperties.isRetryConnection(),
					OkHttpBuilder.httpClientProperties.getTimeout());
		}

		else if (OkHttpBuilder.proxyServerProperties.isNeoloadProxyEnabled()) {

			final Proxy nlProxy = OkHttpBuilder.setUpProxyServer();

			result = new OkHttpClientRecipe(OkHttpBuilder.httpClientProperties.isIgnoreCertificate(),
					OkHttpBuilder.httpClientProperties.getCookiePolicy(),
					OkHttpBuilder.httpClientProperties.getLoggingLevel(), nlProxy,
					OkHttpBuilder.httpClientProperties.isRetryConnection(),
					OkHttpBuilder.httpClientProperties.getTimeout());
		}

		else {

			result = new OkHttpClientRecipe(OkHttpBuilder.httpClientProperties.isIgnoreCertificate(),
					OkHttpBuilder.httpClientProperties.getCookiePolicy(),
					OkHttpBuilder.httpClientProperties.getLoggingLevel(),
					OkHttpBuilder.httpClientProperties.isRetryConnection(),
					OkHttpBuilder.httpClientProperties.getTimeout());
		}

		return result;

	}

	private static BrowserMobProxyServer setUpBrowserMobProxyServer() {
		if (OkHttpBuilder.browsermobProxy == null) {
			OkHttpBuilder.browsermobProxy = ProxyUtils.createBrowserMobProxyServer(OkHttpBuilder.proxyServerProperties,
					TestcaseKindEnum.API_TESTCASE);
		} else if (OkHttpBuilder.browsermobProxy.isStopped()) {
			OkHttpBuilder.browsermobProxy = ProxyUtils.createBrowserMobProxyServer(OkHttpBuilder.proxyServerProperties,
					TestcaseKindEnum.API_TESTCASE);
		}

		return OkHttpBuilder.browsermobProxy;
	}

	private static Proxy setUpProxyServer() {
		if (OkHttpBuilder.proxy == null) {
			OkHttpBuilder.proxy = ProxyUtils.createProxyServer(OkHttpBuilder.proxyServerProperties,
					TestcaseKindEnum.API_TESTCASE_NL_RECORDING);
		}
		return OkHttpBuilder.proxy;
	}
}
