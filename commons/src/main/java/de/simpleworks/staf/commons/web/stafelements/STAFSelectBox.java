package de.simpleworks.staf.commons.web.stafelements;

import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.utils.Convert;

public class STAFSelectBox extends STAFElement {
	private static final Logger logger = LogManager.getLogger(STAFSelectBox.class);

	public STAFSelectBox(final WebDriver webDriver, final By by) {
		super(webDriver, by);
	}

	public void selectByOption(final String text) {
		if (Convert.isEmpty(text)) {
			throw new IllegalArgumentException("text can't be null or empty string.");
		}

		final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(getTimeout()));
		wait.until(ExpectedConditions.presenceOfElementLocated(getBy()));
		if (STAFSelectBox.logger.isDebugEnabled()) {
			STAFSelectBox.logger.debug(String.format("select '%s' into the Input at '%s'.", text, getBy()));
		}

		final Select select = new Select(getWebDriver().findElement(getBy()));
		select.selectByVisibleText(text);
	}

	public void selectByValue(final String text) {
		if (Convert.isEmpty(text)) {
			throw new IllegalArgumentException("text can't be null or empty string.");
		}

		final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(getTimeout()));
		wait.until(ExpectedConditions.presenceOfElementLocated(getBy()));
		if (STAFSelectBox.logger.isDebugEnabled()) {
			STAFSelectBox.logger.debug(String.format("select '%s' into the Input at '%s'.", text, getBy()));
		}

		final Select select = new Select(getWebDriver().findElement(getBy()));
		select.selectByValue(text);
	}

	@Override
	public String getText() throws SystemException {
		final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(getTimeout()));
		wait.until(ExpectedConditions.presenceOfElementLocated(getBy()));

		final Select select = new Select(getWebDriver().findElement(getBy()));
		final String selectedOption = select.getFirstSelectedOption().getText();
		if (STAFSelectBox.logger.isDebugEnabled()) {
			STAFSelectBox.logger.debug(String.format("return option '%s' at '%s'.", selectedOption, getBy()));
		}

		return selectedOption;
	}
}
