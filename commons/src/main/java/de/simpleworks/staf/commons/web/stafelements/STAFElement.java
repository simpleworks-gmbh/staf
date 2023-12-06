package de.simpleworks.staf.commons.web.stafelements;

import java.time.Duration;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.gui.GUIProperties;
import de.simpleworks.staf.commons.web.elements.utils.STAFElementFieldDecorator;
import de.simpleworks.staf.commons.web.elements.utils.STAFElementTransformer;
import de.simpleworks.staf.commons.web.elements.utils.STAFElementTransformer.LocatorType;

public class STAFElement {
	private final Logger logger = LogManager.getLogger(this.getClass());

	/**
	 * The webDriver which can be used in subclasses.
	 **/
	private final WebDriver webDriver;

	/**
	 * The locator through which the element(s) used for an action will be
	 * identified.
	 **/
	private final By by;

	/**
	 * Used to access the locators of a webelement.
	 **/
	private final STAFElementTransformer transformer;

	private final GUIProperties guiproperties;

	public STAFElement(final WebDriver webDriver, final By by) {
		if (webDriver == null) {
			throw new IllegalArgumentException("webDriver can't be null.");
		}

		if (by == null) {
			throw new IllegalArgumentException("by can't be null.");
		}

		this.webDriver = webDriver;
		this.guiproperties = GUIProperties.getInstance();
		this.by = by;
		this.transformer = new STAFElementTransformer();

		// Call the page factory on this object to initialize custom webelements in
		// custom webelements (aka nesting)
		PageFactory.initElements(new STAFElementFieldDecorator(webDriver, webDriver), this);
	}

	protected Logger getLogger() {
		return logger;
	}

	public By getBy() {
		return by;
	}

	protected STAFElementTransformer getTransformer() {
		return transformer;
	}

	protected WebDriver getWebDriver() {
		return webDriver;
	}

	protected LocatorType getLocatorType() {
		return STAFElementTransformer.getLocatorType(getBy());
	}

	protected String getLocatorValue(final LocatorType type) throws SystemException {
		if (type == null) {
			throw new SystemException("type can't be null.");
		}

		return STAFElementTransformer.getLocatorValue(getBy(), type);
	}

	public String getText() throws SystemException {
		if (getWebDriver() == null) {
			throw new SystemException("webDriver can't be null.");
		}

		final String text = getWebElement().getText();

		if (Convert.isEmpty(text)) {
			if (logger.isWarnEnabled()) {
				logger.warn("text is null or empty string.");
			}
		}

		return text;
	}

	public String getAttribute(final String attributeName) throws SystemException {
		if (Convert.isEmpty(attributeName)) {
			throw new IllegalArgumentException("attributeName can't be null or empty.");
		}

		if (getWebDriver() == null) {
			throw new SystemException("webDriver can't be null.");
		}

		final String attributeValue = getWebElement().getAttribute(attributeName);

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Attribute '%s' has the value '%s'.", attributeName, attributeValue));
		}

		return attributeValue;
	}

	public boolean exists() {
		final List<WebElement> matchingElements = getWebDriver().findElements(getBy());

		return !matchingElements.isEmpty();
	}

	public boolean isVisible() {
		final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(getTimeout()));
		wait.until(ExpectedConditions.visibilityOfElementLocated(getBy()));
		return exists();
	}

	public boolean isPresent() {
		final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(getTimeout()));
		wait.until(ExpectedConditions.presenceOfElementLocated(getBy()));
		return exists();
	}

	public List<WebElement> getChildElements(final String childXpath) throws SystemException {
		if (!exists()) {
			throw new SystemException(String.format("No webElement can be found at '%s'.", getBy()));
		}

		if (Convert.isEmpty(childXpath)) {
			throw new IllegalArgumentException("childXpath can't be null or empty string.");
		}

		final List<WebElement> result = getWebDriver().findElements(getBy()).get(0).findElements(By.xpath(childXpath));
		if (Convert.isEmpty(result)) {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("No child elemets can be found at '%s' with '%s'.", getBy(), childXpath));
			}
		}

		return result;
	}

	public List<WebElement> getChildElements(final By locator) throws SystemException {
		if (!exists()) {
			throw new SystemException(String.format("No webElement can be found at '%s'.", getBy()));
		}

		if (locator == null) {
			throw new IllegalArgumentException("locator can't be null.");
		}

		final List<WebElement> matchingElements = getWebDriver().findElements(getBy());
		if (Convert.isEmpty(matchingElements)) {
			throw new SystemException(String.format("no matchingElements for %s.", getBy()));
		}

		final List<WebElement> result = matchingElements.get(0).findElements(locator);
		if (Convert.isEmpty(result)) {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("No child elemets can be found at '%s' with '%s'.", getBy(), locator));
			}
		}

		return result;
	}

	protected WebElement getWebElement() throws SystemException {
		final WebElement result = getWebDriver().findElement(getBy());

		if (result == null) {
			throw new SystemException(String.format("No webElement can be found at '%s'.", getBy()));
		}

		return result;
	}

	/**
	 * @throws SystemException
	 * @brief actions on the WebElement itself
	 */

	private boolean pressAnyKey(final Keys key) {
		if (key == null) {
			throw new IllegalArgumentException("key can't be null.");
		}

		boolean result = false;

		try {
			final WebElement webElement = getWebElement();
			webElement.sendKeys(key);

			result = true;
		} catch (final Exception ex) {
			logger.error(String.format("can't press key: '%s'.", key), ex);
			// FIXME throw an exception.
		}

		return result;
	}

	public boolean pressTABKey() {
		return pressAnyKey(Keys.TAB);
	}

	public boolean pressENTERKey() {
		return pressAnyKey(Keys.ENTER);
	}

	public boolean pressBACKSPACEKey() {
		return pressAnyKey(Keys.BACK_SPACE);
	}

	public boolean pressDELETEKey() {
		return pressAnyKey(Keys.DELETE);
	}

	public boolean doShortCut(final Keys key, final char letter) {
		if (key == null) {
			throw new IllegalArgumentException("key can't be null.");
		}

		if (Convert.isEmpty(Character.toString(letter))) {
			throw new IllegalArgumentException("letter can't be null or empty string.");
		}

		boolean result = false;
		try {
			final WebElement webElement = getWebElement();
			webElement.sendKeys(key + Character.toString(letter));

			result = true;
		} catch (final Exception ex) {
			// FIXME throw an exception like "CannotPressKey".
			logger.error(String.format("can't press shortcut: '%s': letter: '%s'.", key, Character.toString(letter)),
					ex);
		}

		return result;
	}

	public boolean markText() {
		return doShortCut(Keys.CONTROL, 'a');
	}

	public int getTimeout() {
		final int result = guiproperties.getTimeout();

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("using timeout %d.", Integer.valueOf(result)));
		}

		return result;
	}
}
