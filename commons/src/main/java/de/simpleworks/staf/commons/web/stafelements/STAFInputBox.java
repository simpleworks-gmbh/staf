package de.simpleworks.staf.commons.web.stafelements;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import de.simpleworks.staf.commons.exceptions.SystemException;

public class STAFInputBox extends STAFElement {
	private static final Logger logger = LogManager.getLogger(STAFInputBox.class);

	public STAFInputBox(final WebDriver webDriver, final By by) {
		super(webDriver, by);
	}

	public void enterText(final String text) {
		if (STAFInputBox.logger.isDebugEnabled()) {
			STAFInputBox.logger.debug(String.format("enter '%s' into the Input at '%s'.", text, getBy()));
		}

		final WebDriverWait wait = new WebDriverWait(getWebDriver(), getTimeout());
		wait.until(ExpectedConditions.presenceOfElementLocated(getBy()));
		wait.until(ExpectedConditions.elementToBeClickable(getBy()));

		getWebDriver().findElement(getBy()).click();

		if (!markText()) {
			STAFInputBox.logger.error("cannot mark text for deletion.");
		}

		if (!pressDELETEKey()) {
			STAFInputBox.logger.error("cannot delete.");
		}

		getWebDriver().findElement(getBy()).sendKeys(text);
	}

	public void enterFile(final File file) {
		if (file == null) {
			throw new IllegalArgumentException("file can't be null.");
		}

		if (!file.exists()) {
			throw new IllegalArgumentException(String.format("File at '%s' does not exist.", file.getAbsolutePath()));
		}

		final WebDriverWait wait = new WebDriverWait(getWebDriver(), getTimeout());
		wait.until(ExpectedConditions.presenceOfElementLocated(getBy()));

		getWebDriver().findElement(getBy()).sendKeys(file.getAbsolutePath());
	}

	@Override
	public String getText() throws SystemException {

		final WebDriverWait wait = new WebDriverWait(getWebDriver(), getTimeout());
		wait.until(ExpectedConditions.presenceOfElementLocated(getBy()));

		final String value = getAttribute("value");

		if (STAFInputBox.logger.isDebugEnabled()) {
			STAFInputBox.logger.debug(String.format("text is '%s' of Input at '%s'", value, getBy()));
		}

		return value;
	}
}
