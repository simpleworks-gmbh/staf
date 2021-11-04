package de.simpleworks.staf.commons.manager;

import org.openqa.selenium.WebDriver;

import com.google.inject.Provider;

import de.simpleworks.staf.commons.exceptions.SystemException;

public interface WebDriverManager extends Provider<WebDriver> {

	void startDriver() throws SystemException;

	void startProxy() throws SystemException;

	boolean quitDriver();

	void quitProxy();

	boolean isHeadless();

	boolean isScreenshot();

	boolean isProxyEnabled();

	@Override
	WebDriver get();
}
