package de.simpleworks.staf.framework.gui.webdriver.module;

import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

import com.google.common.base.Optional;
import com.neotys.selenium.proxies.DesignManager;

import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.manager.WebDriverManager;
import de.simpleworks.staf.framework.api.proxy.DummyParamBuilderProvider;
import de.simpleworks.staf.framework.api.proxy.ProxyUtils;
import de.simpleworks.staf.framework.api.proxy.properties.NeoloadRecordingProperties;
import de.simpleworks.staf.framework.api.proxy.properties.ProxyServerProperties;
import de.simpleworks.staf.framework.enums.TestcaseKindEnum;
import de.simpleworks.staf.framework.gui.webdriver.properties.WebDriverProperties;
import net.lightbody.bmp.BrowserMobProxyServer;

public abstract class WebDriverManagerImpl implements WebDriverManager {
	private static final Logger logger = LogManager.getLogger(WebDriverManagerImpl.class);
	private static final WebDriverProperties webdriverProperties = WebDriverProperties.getInstance();
	private static final ProxyServerProperties proxyServerProperties = ProxyServerProperties.getInstance();

	private boolean running;

	private BrowserMobProxyServer browserMobProxyServer;
	private Proxy nlProxy;

	private DesignManager designManager;

	protected WebDriver driver;

	protected abstract WebDriver createDriver();

	@Override
	public void startDriver() throws SystemException {
		try {
			driver = createDriver();
			driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
			running = true;
		} catch (final Throwable th) {
			final String message = "can't create driver.";
			WebDriverManagerImpl.logger.error(message, th);
			throw new SystemException(message);
		}
	}

	@Override
	public void startProxy() throws SystemException {
		try {
			if (isProxyEnabled()) {

				if (WebDriverManagerImpl.proxyServerProperties.isProxyEnabled()) {
					if (browserMobProxyServer == null) {
						browserMobProxyServer = WebDriverManagerImpl.setUpBrowserMobProxyServer();
					}

					if (!browserMobProxyServer.isStarted()) {
						browserMobProxyServer.start();
					}
				}

				else if (WebDriverManagerImpl.proxyServerProperties.isProxyEnabled()) {
					if (nlProxy == null) {
						nlProxy = WebDriverManagerImpl.setUpProxy();
					}

					if (designManager == null) {
						final NeoloadRecordingProperties neoloadRecordingProperties = NeoloadRecordingProperties
								.getInstance();

						final String projectPath = neoloadRecordingProperties.getNeoloadProjectPath();
						final String userPath = neoloadRecordingProperties.getNeoloadUserPath();

						if (WebDriverManagerImpl.logger.isInfoEnabled()) {
							WebDriverManagerImpl.logger.info(String
									.format("use 'Project Path' : '%s', 'User Path' : '%s'", projectPath, userPath));
						}

						designManager = new DesignManager(userPath, Optional.of(projectPath),
								new DummyParamBuilderProvider());
						designManager.start();
					}
				}

			}
		} catch (final Throwable th) {
			final String message = "can't start proxy.";
			WebDriverManagerImpl.logger.error(message, th);
			throw new SystemException(message);
		}
	}

	@Override
	public void quitProxy() {
		try {

			if (WebDriverManagerImpl.proxyServerProperties.isProxyEnabled()) {
				if (browserMobProxyServer != null) {
					if (!browserMobProxyServer.isStopped()) {
						browserMobProxyServer.stop();
					}
				}
			} else if (WebDriverManagerImpl.proxyServerProperties.isProxyEnabled()) {
				if (nlProxy == null) {
					nlProxy = WebDriverManagerImpl.setUpProxy();
				}

				if (designManager == null) {
					designManager.stop();
				}
			}

			browserMobProxyServer = null;
			nlProxy = null;
			designManager = null;

		} catch (final Exception ex) {
			// FIXME throw an exception.
			final String msg = "can't stop proxy successfully, will quit driver anyway.";
			WebDriverManagerImpl.logger.error(msg, ex);
		}
	}

	/**
	 * @return (boolean) true, if driver was closed successfully, false if not.
	 */
	@Override
	public boolean quitDriver() {
		if (isRunning()) {
			if (driver != null) {
				try {
					driver.quit();

					running = false;
				} catch (final Throwable th) {
					// FIXME throw an exception.
					WebDriverManagerImpl.logger.error("can't quit driver.", th);
					driver = null;

					return false;
				}
			}

			driver = null;
			return true;
		}

		if (WebDriverManagerImpl.logger.isDebugEnabled()) {
			WebDriverManagerImpl.logger
					.debug("WebDriverManager was requested to quit the driver, although it was already closed.");
		}

		return false;
	}

	@Override
	public final WebDriver get() {
		if (driver == null) {
			try {
				startDriver();
			} catch (final SystemException ex) {
				WebDriverManagerImpl.logger.error("Cannot start driver.", ex);
			}
		}

		return driver;
	}

	private static BrowserMobProxyServer setUpBrowserMobProxyServer() {

		final BrowserMobProxyServer result = ProxyUtils
				.createBrowserMobProxyServer(WebDriverManagerImpl.proxyServerProperties, TestcaseKindEnum.GUI_TESTCASE);
		if (result == null) {
			WebDriverManagerImpl.logger
					.error(String.format("can't set up proxy at port %d, does an instance already run on this port?.",
							Integer.valueOf(WebDriverManagerImpl.proxyServerProperties.getGUIProxyPort())));

			// FIXME throw an exception.
			return null;
		}

		return result;
	}

	private static Proxy setUpProxy() {

		final Proxy result = ProxyUtils.createProxyServer(WebDriverManagerImpl.proxyServerProperties,
				TestcaseKindEnum.GUI_TESTCASE_NL_RECORDING);

		if (result == null) {
			WebDriverManagerImpl.logger
					.error(String.format("can't set up proxy at port %d, does an instance already run on this port?.",
							Integer.valueOf(WebDriverManagerImpl.proxyServerProperties.getGUIProxyPort())));

			// FIXME throw an exception.
			return null;
		}

		return result;
	}

	public BrowserMobProxyServer getBrowserMobProxyServer() {
		return browserMobProxyServer;
	}

	public Proxy getNlProxy() {
		return nlProxy;
	}

	public static ProxyServerProperties getProxyServerProperties() {
		return WebDriverManagerImpl.proxyServerProperties;
	}

	@Override
	public final boolean isHeadless() {
		return WebDriverManagerImpl.webdriverProperties.isHeadless();
	}

	@Override
	public final boolean isScreenshot() {
		return WebDriverManagerImpl.webdriverProperties.isScreenshot();
	}

	@Override
	public final boolean isProxyEnabled() {
		return WebDriverManagerImpl.proxyServerProperties.isProxyEnabled()
				|| WebDriverManagerImpl.proxyServerProperties.isNeoloadProxyEnabled();
	}

	public final boolean isRunning() {
		return running;
	}
}
