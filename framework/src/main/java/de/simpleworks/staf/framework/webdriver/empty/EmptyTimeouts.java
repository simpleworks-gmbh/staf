package de.simpleworks.staf.framework.webdriver.empty;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Timeouts;

public class EmptyTimeouts implements WebDriver.Timeouts {
	@Override
	public Timeouts implicitlyWait(final long time, final TimeUnit unit) {
		return new EmptyTimeouts();
	}

	@Override
	public Timeouts setScriptTimeout(final long time, final TimeUnit unit) {
		return new EmptyTimeouts();
	}

	@Override
	public Timeouts pageLoadTimeout(final long time, final TimeUnit unit) {
		return new EmptyTimeouts();
	}
}
