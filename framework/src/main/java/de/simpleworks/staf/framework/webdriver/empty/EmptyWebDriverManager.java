package de.simpleworks.staf.framework.webdriver.empty;

import org.openqa.selenium.WebDriver;

import de.simpleworks.staf.framework.gui.webdriver.module.WebDriverManagerImpl;

public class EmptyWebDriverManager extends WebDriverManagerImpl {

	@Override
	protected WebDriver createDriver() {
		return new EmptyWebDriver();
	}

}
