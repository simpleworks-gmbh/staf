package de.simpleworks.staf.framework.webdriver.empty;

import java.util.Set;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.WebDriver.ImeHandler;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WebDriver.Window;
import org.openqa.selenium.logging.Logs;
import de.simpleworks.staf.framework.webdriver.empty.EmptyTimeouts;

public class EmptyOptions implements WebDriver.Options {

	@Override
	public void addCookie(final Cookie cookie) {
		// nothing to do
	}

	@Override
	public void deleteCookieNamed(final String name) {
		// nothing to do
	}

	@Override
	public void deleteCookie(final Cookie cookie) {
		// nothing to do
	}

	@Override
	public void deleteAllCookies() {
		// nothing to do
	}

	@Override
	public Set<Cookie> getCookies() {
		return null;
	}

	@Override
	public Cookie getCookieNamed(final String name) {
		return null;
	}

	@Override
	public Timeouts timeouts() {
		return new EmptyTimeouts();
	}

	/*
	@Override
	public ImeHandler ime() {
		return null;
	}
	*/

	@Override
	public Window window() {
		return null;
	}

	@Override
	public Logs logs() {
		return null;
	}
}
