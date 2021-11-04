package de.simpleworks.staf.framework.elements.gui;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.gui.GUIProperties;
import de.simpleworks.staf.commons.web.elements.utils.STAFElementFieldDecorator;
import de.simpleworks.staf.commons.web.stafelements.STAFElement;

public class PageObject {
	private static final Logger logger = LogManager.getLogger(PageObject.class);

	private final WebDriver driver;
	private final GUIProperties guiproperties;

	protected PageObject(final WebDriver driver) {
		if (driver == null) {
			throw new IllegalArgumentException("driver can't be null.");
		}

		this.driver = driver;
		this.guiproperties = GUIProperties.getInstance();

		PageFactory.initElements(new STAFElementFieldDecorator(this.driver, this.driver), this);
	}

	public final WebDriver getDriver() {
		return driver;
	}

	protected final URL getURL() {
		URL result = null;

		try {
			final String url = getDriver().getCurrentUrl();
			result = new URL(url);
		} catch (final Exception ex) {
			final String msg = "can't fetch current url.";
			PageObject.logger.error(msg, ex);
		}

		return result;
	}

	protected final void refreshPage() {
		getDriver().navigate().refresh();
	}

	public int getTimeout() {
		final int timeout = guiproperties.getTimeout();

		if (PageObject.logger.isDebugEnabled()) {
			PageObject.logger.debug(String.format("using timeout %d.", Integer.valueOf(timeout)));
		}

		return timeout;
	}

	/**
	 * @brief method to return a WebElement that matches the locator
	 *        {@param locator}
	 * @param By locator
	 * @return an element that can be found, that matches the locator, null if not,
	 *         even if the xpath matches several elements!
	 */
	protected final WebElement findElement(final By locator) {
		if (locator == null) {
			throw new IllegalArgumentException("locator can't be null.");
		}

		if (PageObject.logger.isDebugEnabled()) {
			PageObject.logger.debug(String.format("looking for element identified by '%s'.", locator));
		}

		WebElement result = null;

		try {

			result = getDriver().findElement(locator);
		} catch (final Exception ex) {
			final String msg = String.format("can't find element identified by '%s'.", locator.toString());
			PageObject.logger.error(msg, ex);
		}

		return result;
	}

	/**
	 * @brief method to return all WebElements that match the locator
	 *        {@param locator}
	 * @param By locator
	 * @return returns all elements that can be found, empty list if none element
	 *         can be found
	 */
	protected final List<WebElement> findElements(final By locator) {
		if (locator == null) {
			throw new IllegalArgumentException("locator can't be null.");
		}

		if (PageObject.logger.isDebugEnabled()) {
			PageObject.logger.debug(String.format("looking for elements identified by '%s'.", locator));
		}

		List<WebElement> result = new ArrayList<>();
		try {
			result = getDriver().findElements(locator);
		} catch (final Exception ex) {
			// FIXME throw an exception like "ElementNotFound".
			final String msg = String.format("can't find element identified by '%s'.", locator);
			PageObject.logger.error(msg, ex);
		}

		return result;
	}

	protected final Boolean elementExists(final By locator) {
		if (locator == null) {
			throw new IllegalArgumentException("locator can't be null.");
		}

		if (findElement(locator) == null) {
			if (PageObject.logger.isDebugEnabled()) {
				PageObject.logger.debug(String.format("element identified by '%s' does not exist.", locator));
			}

			return Boolean.FALSE;
		}

		return Boolean.TRUE;
	}

	protected final void openUrl(final URL url) {
		if (url == null) {
			throw new IllegalArgumentException("url can't be null.");
		}

		if (PageObject.logger.isDebugEnabled()) {
			PageObject.logger.debug(String.format("open url: '%s'.", url));
		}

		getDriver().get(url.toString());
	}

	protected boolean doShortCut(final Keys key, final char letter) {
		if (key == null) {
			throw new IllegalArgumentException("key can't be null.");
		}

		if (Convert.isEmpty(Character.toString(letter))) {
			throw new IllegalArgumentException("letter can't be null or empty.");
		}

		final By locator = By.xpath("//html");

		boolean result = false;
		try {
			final STAFElement page = new STAFElement(getDriver(), locator);

			result = page.doShortCut(key, letter);
		} catch (final Exception ex) {
			// FIXME throw an exception like "CannotPressKey".
			PageObject.logger.error(String.format("can't call shortcut for key: '%s' and letter: '%s'.", key,
					Character.toString(letter)), ex);
		}

		return result;
	}
}
