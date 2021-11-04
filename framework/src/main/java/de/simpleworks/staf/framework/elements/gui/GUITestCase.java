package de.simpleworks.staf.framework.elements.gui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import com.google.inject.Inject;
import com.google.inject.Module;

import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.manager.WebDriverManager;
import de.simpleworks.staf.commons.report.artefact.Screenshot;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.framework.api.proxy.ProxyUtils;
import de.simpleworks.staf.framework.elements.api.RewriteUrlObject;
import de.simpleworks.staf.framework.elements.commons.TestCase;
import de.simpleworks.staf.framework.gui.webdriver.module.DriverModule;
import de.simpleworks.staf.framework.gui.webdriver.module.WebDriverManagerImpl;
import net.lightbody.bmp.BrowserMobProxyServer;

public abstract class GUITestCase extends TestCase {
	private static final Logger logger = LogManager.getLogger(GUITestCase.class);

	@Inject
	private WebDriverManager driverManager;

	@Inject
	private WebDriverManagerImpl drivermanagerimpl;

	public GUITestCase(final Module... modules) {
		super(UtilsCollection.add(Module.class, modules, new DriverModule()));
	}

	private void configureProxy() {
		final BrowserMobProxyServer proxy = drivermanagerimpl.getProxy();
		if (proxy == null) {
			if (GUITestCase.logger.isDebugEnabled()) {
				GUITestCase.logger.debug("steps will not be marked, because the proxy is null.");
			}

			return;
		}

		for (final RewriteUrlObject rewrittenUrl : getRewriteUrls()) {
			try {
				if (rewrittenUrl != null) {
					rewrittenUrl.validate();
					if (GUITestCase.logger.isDebugEnabled()) {
						GUITestCase.logger.debug(String.format("matching requests will be rewritten from '%s' to '%s'.",
								rewrittenUrl.getPattern(), rewrittenUrl.getRewriteExpression()));
					}

					ProxyUtils.addRewriteRules(proxy, rewrittenUrl);
				}
			} catch (final Exception ex) {
				GUITestCase.logger.error("can't add rewrite rule.", ex);
				continue;
			}
		}
	}

	@Override
	public BrowserMobProxyServer getProxy() {
		return drivermanagerimpl.getProxy();
	}

	@Override
	public void bootstrap() throws Exception {
		if (!drivermanagerimpl.isRunning()) {
			drivermanagerimpl.startDriver();
		}

		configureProxy();

		drivermanagerimpl.startProxy();
	}

	@Override
	public void shutdown() throws Exception {
		if (drivermanagerimpl.isRunning()) {
			drivermanagerimpl.quitProxy();
			drivermanagerimpl.quitDriver();
		}
	}

	public final WebDriver getDriver() throws SystemException {
		final WebDriver driver = driverManager.get();

		if (driver == null) {
			final String msg = "driver can't be null.";
			GUITestCase.logger.error(msg);
			throw new SystemException(msg);
		}

		return driver;
	}

	@Override
	public Screenshot createArtefact() {
		Screenshot result = null;

		try {
			final TakesScreenshot screenshot = (TakesScreenshot) getDriver();
			if (screenshot == null) {
				throw new IllegalArgumentException("screenshot can't be null.");
			}

			result = new Screenshot(screenshot);
		} catch (final Exception ex) {
			GUITestCase.logger.error("can't create artifact.", ex);
		}

		return result;
	}

	@Override
	public void executeTestStep() throws Exception {
		throw new SystemException(
				String.format("executeTestStep not supported on \"%s\".", this.getClass().toString()));
	}
}
