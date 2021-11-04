package de.simpleworks.staf.framework.gui.webdriver.module;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import de.simpleworks.staf.commons.manager.WebDriverManager;
import de.simpleworks.staf.framework.gui.webdriver.properties.WebDriverProperties;

public class DriverModule extends AbstractModule {
	private static final Logger logger = LogManager.getLogger(DriverModule.class);

	private static final WebDriverProperties properties = WebDriverProperties.getInstance();

	@Override
	protected void configure() {
		final WebDriverManagerImpl webDriverManagerImpl = DriverModule.properties.getWebDriverManager();

		if (webDriverManagerImpl == null) {
			throw new RuntimeException("webDriverManagerImpl can't be null.");
		}

		try {
			bind(WebDriverManagerImpl.class).toInstance(webDriverManagerImpl);
			bind(WebDriverManager.class).toInstance(webDriverManagerImpl);
			bind(WebDriver.class).toProvider(webDriverManagerImpl);
		} catch (final Exception ex) {
			final String message = "Cannot set up Driver Module.";
			DriverModule.logger.error(message, ex);
			throw new RuntimeException(message);
		}
	}

	@SuppressWarnings("static-method")
	@Provides
	public Actions getActions(final WebDriver driver) {
		return new Actions(driver);
	}

	@SuppressWarnings("static-method")
	@Provides
	public JavascriptExecutor getExecutor(final WebDriver driver) {
		return (JavascriptExecutor) driver;
	}
}
