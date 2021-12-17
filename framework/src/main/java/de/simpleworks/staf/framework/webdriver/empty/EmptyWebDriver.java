package de.simpleworks.staf.framework.webdriver.empty;

import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

public class EmptyWebDriver implements WebDriver, TakesScreenshot {

	@Override
	public <X> X getScreenshotAs(final OutputType<X> target) throws WebDriverException {
		return null;
	}

	@Override
	public void get(final String url) {
		// nothing to do
	}

	@Override
	public String getCurrentUrl() {
		return "no current url";
	}

	@Override
	public String getTitle() {
		return "no title";
	}

	@Override
	public List<WebElement> findElements(final By by) {
		return null;
	}

	@Override
	public WebElement findElement(final By by) {
		return null;
	}

	@Override
	public String getPageSource() {
		return "no source";
	}

	@Override
	public void close() {
		// nothing to do
	}

	@Override
	public void quit() {
		// nothing to do
	}

	@Override
	public Set<String> getWindowHandles() {
		return null;
	}

	@Override
	public String getWindowHandle() {
		return "no window handle";
	}

	@Override
	public TargetLocator switchTo() {
		return null;
	}

	@Override
	public Navigation navigate() {
		return null;
	}

	@Override
	public Options manage() {
		return new EmptyOptions();
	}
}
