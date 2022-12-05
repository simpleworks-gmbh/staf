package de.simpleworks.staf.devicetesting.android.stafelements;

import java.time.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.web.stafelements.STAFElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidTouchAction;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.touch.LongPressOptions;
import io.appium.java_client.touch.offset.ElementOption;

@SuppressWarnings("deprecation")
public class STAFInputBox extends STAFElement {
	private static final Logger logger = LogManager.getLogger(STAFInputBox.class);

	public STAFInputBox(final WebDriver webDriver, final By by) throws SystemException {
		super(webDriver, by);
		if (!(getWebDriver() instanceof AndroidDriver)) {
			throw new SystemException(String.format("webdriver '%s' is no instance of '%s'.", getWebDriver().getClass(),
					AndroidDriver.class));
		}
	}

	public void enterText(final String text) {
		if (STAFInputBox.logger.isDebugEnabled()) {
			STAFInputBox.logger.debug(String.format("enter '%s' into the Input at '%s'.", text, getBy()));
		}
		final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(getTimeout()));
		wait.until(ExpectedConditions.presenceOfElementLocated(getBy()));
		wait.until(ExpectedConditions.elementToBeClickable(getBy()));
		getWebDriver().findElement(getBy()).click();
		if (markText()) {
			if (!pressDELETEKey()) {
				STAFInputBox.logger.error("cannot delete text.");
			}
		}
		Actions enterText = new Actions(getWebDriver());
		enterText.sendKeys(text);
		enterText.perform();
		((AndroidDriver) getWebDriver()).hideKeyboard();
	}

	@Override
	public String getText() throws SystemException {
		final WebDriverWait wait = new WebDriverWait(getWebDriver(),Duration.ofSeconds(getTimeout()) );
		wait.until(ExpectedConditions.presenceOfElementLocated(getBy()));
		final String value = getWebDriver().findElement(getBy()).getText();
		if (STAFInputBox.logger.isDebugEnabled()) {
			STAFInputBox.logger.debug(String.format("text is '%s' of Input at '%s'", value, getBy()));
		}
		return value;
	}

	@Override
	public boolean markText() {
		boolean result = true;
		try {
			// check if we need to mark text at all.
			if (Convert.isEmpty(getText())) {
				return false;
			}
			// (long) press on the text edit, so the whole texts gets selected.
			AndroidTouchAction ta = new AndroidTouchAction((AndroidDriver) getWebDriver());
			ta.longPress(LongPressOptions.longPressOptions()
					.withElement(ElementOption.element(getWebDriver().findElement(getBy())))
					.withDuration(Duration.ofMillis(2000))).release();
			ta.perform();
		} catch (Exception ex) {
			STAFInputBox.logger.error("can't mark text.", ex);
			result = false;
		}
		return result;
	}

	@Override
	public boolean pressDELETEKey() {
		boolean result = true;
		try {
			((AndroidDriver) getWebDriver()).pressKey(new KeyEvent(AndroidKey.DEL));
		} catch (Exception ex) {
			STAFInputBox.logger.error("can't delete text.", ex);
			result = false;
		}
		return result;
	}
}