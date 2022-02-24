package de.simpleworks.staf.commons.web.stafelements;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class STAFList extends STAFElement {
	private static final Logger logger = LogManager.getLogger(STAFList.class);

	public STAFList(final WebDriver webDriver, final By by) {
		super(webDriver, by);

	}

	public List<WebElement> getAllElements() throws SystemException {
		if (!exists()) {
			throw new SystemException(String.format("No webElement can be found at '%s'.", getBy()));
		}

		final List<WebElement> result = getWebDriver().findElements(getBy());

		if (Convert.isEmpty(result)) {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("No list can be found at with '%s'.", getBy()));
			}
		}
		return result;
	}

	public WebElement getElement(final int i) throws SystemException {

		if (i <= 0) {
			throw new IllegalArgumentException("Number of list can't be small or equals 0");
		}

		if (STAFList.logger.isDebugEnabled()) {
			STAFList.logger.debug(String.format("List element can't be found '%s' with '%s'.", getBy(), i));
		}

		final WebDriverWait wait = new WebDriverWait(getWebDriver(), getTimeout());
		wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(getBy()));

		final List<WebElement> elements = getWebDriver().findElements(getBy());

		if (Convert.isEmpty(elements)) {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("No list can be found at with '%s'.", getBy()));
			}
		}

		return elements.get(i - 1);
	}

	public void clickOnElement(final int i) throws SystemException {

		if (i <= 0) {
			throw new IllegalArgumentException("Number of list can't be small or equals 0");
		}

		if (STAFList.logger.isDebugEnabled()) {
			STAFList.logger.debug(String.format("click on element at '%s'.", getBy()));
		}

		final WebDriverWait wait = new WebDriverWait(getWebDriver(), getTimeout());
		wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(getBy()));
		wait.until(ExpectedConditions.elementToBeClickable(getAllElements().get(i - 1)));

		final List<WebElement> elements = getWebDriver().findElements(getBy());

		if (Convert.isEmpty(elements)) {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("No list can be found at with '%s'.", getBy()));
			}
		}

		elements.get(i - 1).click();

	}

	public String getElementText(final int i) throws SystemException {

		if (i <= 0) {
			throw new IllegalArgumentException("Number of list can't be small or equals 0");
		}

		if (STAFList.logger.isDebugEnabled()) {
			STAFList.logger.debug(String.format("get element text at '%s'.", getBy()));
		}

		final WebDriverWait wait = new WebDriverWait(getWebDriver(), getTimeout());
		wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(getBy()));

		final List<WebElement> elements = getWebDriver().findElements(getBy());

		if (Convert.isEmpty(elements)) {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("No list can be found at with '%s'.", getBy()));
			}
		}

		final String text = elements.get(i - 1).getText();

		if (Convert.isEmpty(text)) {
			if (logger.isWarnEnabled()) {
				logger.warn("text is null or empty string.");
			}
		}
		return text;
	}

	public String getElementAttribute(final int i, final String attributeName) throws SystemException {

		if (i <= 0) {
			throw new IllegalArgumentException("Number of list can't be small or equals 0");
		}
		if (Convert.isEmpty(attributeName)) {
			throw new IllegalArgumentException("attributeName can't be null or empty.");
		}

		final WebDriverWait wait = new WebDriverWait(getWebDriver(), getTimeout());
		wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(getBy()));

		final List<WebElement> elements = getWebDriver().findElements(getBy());

		if (Convert.isEmpty(elements)) {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("No list can be found at with '%s'.", getBy()));
			}
		}

		return elements.get(i - 1).getAttribute(attributeName);
	}

	public void enterTextAtElement(final int i, final String text) throws SystemException {

		if (i <= 0) {
			throw new IllegalArgumentException("Number of list can't be small or equals 0");
		}

		if (STAFList.logger.isDebugEnabled()) {
			STAFList.logger.debug(String.format("enter '%s' into the Input at '%s' . index '%s'.", text, getBy(), i));
		}

		final WebDriverWait wait = new WebDriverWait(getWebDriver(), getTimeout());
		wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(getBy()));

		final List<WebElement> elements = getWebDriver().findElements(getBy());

		if (Convert.isEmpty(elements)) {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("No list can be found at with '%s'.", getBy()));
			}
		}

		elements.get(i - 1).click();

		if (!markText()) {
			STAFList.logger.error("cannot mark text for deletion.");
		}

		if (!pressDELETEKey()) {
			STAFList.logger.error("cannot delete.");
		}

		elements.get(i - 1).sendKeys(text);
	}
}
