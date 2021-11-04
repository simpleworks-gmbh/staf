package de.simpleworks.staf.framework.gui.webdriver.properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.annotation.Property;
import de.simpleworks.staf.commons.annotation.Property.ClassPath;
import de.simpleworks.staf.commons.utils.PropertiesReader;
import de.simpleworks.staf.framework.consts.FrameworkConsts;
import de.simpleworks.staf.framework.gui.webdriver.module.WebDriverManagerImpl;

public class WebDriverProperties extends PropertiesReader {
	private static final Logger logger = LogManager.getLogger(WebDriverProperties.class);

	private static WebDriverProperties instance = null;

	@Property(FrameworkConsts.WEBDRIVER_SCREENSHOT)
	private boolean screenshot;

	public boolean isScreenshot() {
		return screenshot;
	}

	@Property(FrameworkConsts.WEBDRIVER_HEADLESS)
	private boolean headless;

	public boolean isHeadless() {
		return headless;
	}

	@ClassPath
	@Property(FrameworkConsts.WEBDRIVER_MANAGER_CLASS)
	private WebDriverManagerImpl webdrivermanager;

	public WebDriverManagerImpl getWebDriverManager() {
		return webdrivermanager;
	}

	@Override
	protected Class<?> getClazz() {
		return WebDriverProperties.class;
	}

	public static final synchronized WebDriverProperties getInstance() {
		if (WebDriverProperties.instance == null) {
			if (WebDriverProperties.logger.isDebugEnabled()) {
				WebDriverProperties.logger.debug("create instance.");
			}

			WebDriverProperties.instance = new WebDriverProperties();
		}

		return WebDriverProperties.instance;
	}
}
