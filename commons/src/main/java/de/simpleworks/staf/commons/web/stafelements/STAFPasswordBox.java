package de.simpleworks.staf.commons.web.stafelements;

import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import de.simpleworks.staf.commons.exceptions.SystemException;

public class STAFPasswordBox extends STAFInputBox {
	private static final Logger logger = LogManager.getLogger(STAFPasswordBox.class);

	public STAFPasswordBox(final WebDriver webDriver, final By by) {
		super(webDriver, by);
	}

	@Override
	public void enterText(final String text) {
		if (STAFPasswordBox.logger.isDebugEnabled()) {
			STAFPasswordBox.logger.debug(String.format("enter confidential input at '%s'.", getBy()));
		}

		final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(getTimeout()));
		wait.until(ExpectedConditions.presenceOfElementLocated(getBy()));
		wait.until(ExpectedConditions.elementToBeClickable(getBy()));

		getWebDriver().findElement(getBy()).click();

		if (!markText()) {
			STAFPasswordBox.logger.error("cannot mark text for deletion.");
		}

		if (!pressDELETEKey()) {
			STAFPasswordBox.logger.error("cannot delete.");
		}

		getWebDriver().findElement(getBy()).sendKeys(text);
	}

	@Override
	public String getText() throws SystemException {

		final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(getTimeout()));
		wait.until(ExpectedConditions.presenceOfElementLocated(getBy()));

		final String value = getAttribute("value");

		if (STAFPasswordBox.logger.isDebugEnabled()) {
			STAFPasswordBox.logger.debug(String.format("text is confidential will not be logged from '%s'.", getBy()));
		}

		return value;
	}
}
