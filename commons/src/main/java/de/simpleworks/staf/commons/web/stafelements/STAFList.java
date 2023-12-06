package de.simpleworks.staf.commons.web.stafelements;

import java.time.Duration;
import java.util.List;

import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.utils.Convert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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

		final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(getTimeout()));
		wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(getBy()));

		final List<WebElement> result = getWebDriver().findElements(getBy());

		if (Convert.isEmpty(result)) {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("No list can be found at with '%s'.", getBy()));
			}
		}
		return result;
	}

	public WebElement getElement(final int index) throws SystemException {

		if (index < 0) {
			throw new IllegalArgumentException(
					String.format("index is less than 0, which is not supported '%s'.", Integer.toString(index)));
		}

		final List<WebElement> elements = getAllElements();

		if (elements.size() <= index) {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("The index '%s' is greater or equal than the element list of '%s'",
						Integer.toString(index), Integer.toString(elements.size())));
			}

			return null;
		}

		final WebElement result = elements.get(index);

		if (result == null) {
			if (logger.isDebugEnabled()) {
				logger.debug((String.format("No element can't be found at '%s'", Integer.toString(index))));
			}
		}

		return result;
	}

	public void clickOnElement(final int index) throws SystemException {

		if (index < 0) {
			throw new IllegalArgumentException(
					String.format("index is less than 0, which is not supported '%s'.", Integer.toString(index)));
		}

		if (STAFList.logger.isDebugEnabled()) {
			STAFList.logger.debug(String.format("click on element at '%s'.", getBy()));
		}

		final WebElement element = getElement(index);

		if (element == null) {
			throw new SystemException(String.format("No element can't be found at '%s'", Integer.toString(index)));
		}

		final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(getTimeout()));
		wait.until(ExpectedConditions.elementToBeClickable(element));

		element.click();
	}

	public String getElementText(final int index) throws SystemException {

		if (index < 0) {
			throw new IllegalArgumentException(
					String.format("index is less than 0, which is not supported '%s'.", Integer.toString(index)));
		}

		if (STAFList.logger.isDebugEnabled()) {
			STAFList.logger.debug(String.format("click on element at '%s'.", getBy()));
		}

		final WebElement element = getElement(index);

		if (element == null) {
			throw new SystemException(String.format("No element can't be found at '%s'", Integer.toString(index)));
		}

		final String result = element.getText();

		if (Convert.isEmpty(result)) {
			if (logger.isWarnEnabled()) {
				logger.warn("result is null or empty string.");
			}
		}
		return result;
	}

	public String getElementAttribute(final int index, final String attributeName) throws SystemException {

		if (index < 0) {
			throw new IllegalArgumentException(
					String.format("index is less than 0, which is not supported '%s'.", Integer.toString(index)));
		}

		if (Convert.isEmpty(attributeName)) {
			throw new IllegalArgumentException("attributeName can't be null or empty.");
		}

		if (STAFList.logger.isDebugEnabled()) {
			STAFList.logger.debug(String.format("click on element at '%s'.", getBy()));
		}

		final WebElement element = getElement(index);

		if (element == null) {
			throw new SystemException(String.format("No element can't be found at '%s'", Integer.toString(index)));
		}

		final String result = element.getAttribute(attributeName);

		if (Convert.isEmpty(result)) {
			if (logger.isWarnEnabled()) {
				logger.warn("result is null or empty string.");
			}
		}
		return result;

	}

	public void enterTextAtElement(final int index, final String text) throws SystemException {

		if (index < 0) {
			throw new IllegalArgumentException(
					String.format("index is less than 0, which is not supported '%s'.", Integer.toString(index)));
		}

		if (STAFList.logger.isDebugEnabled()) {
			STAFList.logger.debug(String.format("click on element at '%s'.", getBy()));
		}

		final WebElement element = getElement(index);

		if (element == null) {
			throw new SystemException(String.format("No element can't be found at '%s'", Integer.toString(index)));
		}

		final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(getTimeout()));
		wait.until(ExpectedConditions.elementToBeClickable(element));

		element.click();

		if (!markText()) {
			STAFList.logger.error("cannot mark text for deletion.");
		}

		if (!pressDELETEKey()) {
			STAFList.logger.error("cannot delete.");
		}

		element.sendKeys(text);
	}
}
