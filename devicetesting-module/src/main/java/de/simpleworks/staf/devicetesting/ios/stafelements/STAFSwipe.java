package de.simpleworks.staf.devicetesting.ios.stafelements;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.web.stafelements.STAFElement;
import io.appium.java_client.ios.IOSDriver;

public class STAFSwipe extends STAFElement {

	private static final Logger logger = LogManager.getLogger(STAFSwipe.class);

	public STAFSwipe(final WebDriver webDriver, final By by) throws SystemException {
		super(webDriver, by);
		if (!(getWebDriver() instanceof IOSDriver)) {
			throw new SystemException(String.format("webdriver '%s' is no instance of '%s'.", getWebDriver().getClass(),
					IOSDriver.class));
		}
	}

	public void swipeToElement() {

		if (STAFSwipe.logger.isDebugEnabled()) {
			STAFSwipe.logger.debug(String.format("swipe element at '%s'.", getBy()));
		}
		final WebDriverWait wait = new WebDriverWait(getWebDriver(), getTimeout());
		wait.until(ExpectedConditions.presenceOfElementLocated(getBy()));

		final Actions swipe = new Actions(getWebDriver());
		swipe.clickAndHold(getWebDriver().findElement(getBy())).moveToElement(getWebDriver().findElement(getBy()))
				.release().build().perform();
	}

}