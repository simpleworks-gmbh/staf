package de.simpleworks.staf.framework.util;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.simpleworks.staf.commons.exceptions.SystemException;
import net.lightbody.bmp.BrowserMobProxyServer;
import okhttp3.CookieJar;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;

public class OkHttpClientRecipe {
	private final Logger logger = LoggerFactory.getLogger(OkHttpClientRecipe.class);

	private final boolean ignoreCertificate;
	private final CookiePolicy cookiePolicy;
	private final Level loggingLevel;
	private OkHttpClient client;

	private final BrowserMobProxyServer browsermobProxy;

	public OkHttpClientRecipe(final boolean ignoreCertificate, final CookiePolicy cookiePolicy,
			final Level loggingLevel, final BrowserMobProxyServer browsermobProxy) throws SystemException {
		if (cookiePolicy == null) {
			throw new IllegalArgumentException("cookiePolicy can't be null.");
		}

		if (loggingLevel == null) {
			throw new IllegalArgumentException("loggingLevel can't be null.");
		}

		this.ignoreCertificate = ignoreCertificate;
		this.cookiePolicy = cookiePolicy;
		this.loggingLevel = loggingLevel;
		this.browsermobProxy = browsermobProxy;
		buildOkHttpClient();
	}

	public BrowserMobProxyServer getBrowsermobProxy() {
		return browsermobProxy;
	}

	public OkHttpClient getClient() {
		return client;
	}

	private void buildOkHttpClient() throws SystemException {
		Builder builder = null;

		try {
			builder = init(this.ignoreCertificate);
		} catch (final Exception ex) {
			final String message = "can't determine if certificates should be ignored, or not.";
			logger.error(message, ex);
			throw new SystemException(message);
		}

		final HttpLoggingInterceptor interceptor = getNetworkInterceptor();
		final CookieJar cookieJar = getCookieJar();
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("interceptor: '%s', cookieJar: '%s'.", interceptor, cookieJar));
		}

		if (browsermobProxy == null) {
			client = builder.addNetworkInterceptor(interceptor).cookieJar(cookieJar).build();
		} else {
			final InetSocketAddress address = new InetSocketAddress(browsermobProxy.getServerBindAddress(),
					browsermobProxy.getPort());
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("address: '%s'.", address));
			}

			final Proxy proxy = new Proxy(Type.HTTP, address);
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("proxy: '%s'.", proxy));
			}
			client = builder.addNetworkInterceptor(interceptor).cookieJar(cookieJar).proxy(proxy).build();
		}

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("client: '%s'.", client));
		}
	}

	private CookieJar getCookieJar() {
		JavaNetCookieJar result = null;

		try {
			final CookieManager cookieManager = new CookieManager();
			cookieManager.setCookiePolicy(cookiePolicy);

			result = new JavaNetCookieJar(cookieManager);
		} catch (final Exception ex) {
			// FIXME throw an exception.
			logger.error("can't set up cookie jar.", ex);
			result = null;
		}

		return result;
	}

	private HttpLoggingInterceptor getNetworkInterceptor() {
		final HttpLoggingInterceptor result = new HttpLoggingInterceptor((msg) -> {
			logger.debug(msg);
		});

		result.setLevel(loggingLevel);

		return result;
	}

	private Builder init(final boolean ignoreCert) throws Exception {
		Builder builder = new Builder();

		if (ignoreCert) {
			builder = configureToIgnoreCertificate(builder);
		}

		return builder;
	}

	// Setting testMode configuration. If set as testMode, the connection will skip
	// certification check
	private Builder configureToIgnoreCertificate(final Builder builder) {
		if (builder == null) {
			throw new IllegalArgumentException("builder can't be null.");
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Ignore Ssl Certificate.");
		}

		try {
			// Create a trust manager that does not validate certificate chains
			final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				@Override
				public void checkClientTrusted(final java.security.cert.X509Certificate[] chain, final String authType)
						throws CertificateException {
					// nothing to do.
				}

				@Override
				public void checkServerTrusted(final java.security.cert.X509Certificate[] chain, final String authType)
						throws CertificateException {
					// nothing to do.
				}

				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return new java.security.cert.X509Certificate[] {};
				}
			} };

			// Install the all-trusting trust manager
			final SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

			// Create an ssl socket factory with our all-trusting manager
			final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

			builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
			builder.hostnameVerifier((hostname, session) -> true);
		} catch (final Exception ex) {
			// FIXME throw an exception.
			logger.error("Exception while configuring IgnoreSslCertificate.", ex);
		}

		return builder;
	}
}
