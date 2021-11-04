package de.simpleworks.staf.commons.web.stafelements;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class STAFLabel extends STAFElement {
	private static final Logger logger = LogManager.getLogger(STAFInputBox.class);

	public STAFLabel(final WebDriver webDriver, final By by) {
		super(webDriver, by);
	}

	@Override
	public String getText() {

		final WebDriverWait wait = new WebDriverWait(getWebDriver(), getTimeout());
		wait.until(ExpectedConditions.presenceOfElementLocated(getBy()));

		final String text = getWebDriver().findElement(getBy()).getText();

		if (STAFLabel.logger.isDebugEnabled()) {
			STAFLabel.logger.debug(String.format("text from '%s' is '%s'.", getBy(), text));
		}

		return text;
	}
}
