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

	public void click() {
		if (STAFList.logger.isDebugEnabled()) {
			STAFList.logger.debug(String.format("click on element at '%s'.", getBy()));
		}

		final WebDriverWait wait = new WebDriverWait(getWebDriver(), getTimeout());
		wait.until(ExpectedConditions.presenceOfElementLocated(getBy()));
		wait.until(ExpectedConditions.elementToBeClickable(getBy()));

		getWebDriver().findElement(getBy()).click();
	}
}
