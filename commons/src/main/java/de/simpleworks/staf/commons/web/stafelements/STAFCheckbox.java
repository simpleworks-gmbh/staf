package de.simpleworks.staf.commons.web.stafelements;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import de.simpleworks.staf.commons.exceptions.SystemException;

public class STAFCheckbox extends STAFElement {
	private static final Logger logger = LogManager.getLogger(STAFCheckbox.class);

	public STAFCheckbox(final WebDriver webDriver, final By by) {
		super(webDriver, by);
	}

	public void check() throws SystemException {
		if (!isChecked()) {
			click();

			if (STAFCheckbox.logger.isDebugEnabled()) {
				STAFCheckbox.logger.debug(
						String.format("The Checkbox at '%s' is checked: '%s'.", getBy(), Boolean.valueOf(isChecked())));
			}
		} else {
			if (STAFCheckbox.logger.isDebugEnabled()) {
				STAFCheckbox.logger.debug("The Checkbox at '%s' was already checked.");
			}
		}
	}

	public void unCheck() throws SystemException {
		if (isChecked()) {
			click();

			if (STAFCheckbox.logger.isDebugEnabled()) {
				STAFCheckbox.logger.debug(
						String.format("The Checkbox at '%s' is checked: '%s'.", getBy(), Boolean.valueOf(isChecked())));
			}
		} else {
			if (STAFCheckbox.logger.isDebugEnabled()) {
				STAFCheckbox.logger.debug("The Checkbox at '%s'  was already unchecked.");
			}
		}
	}

	public boolean isChecked() throws SystemException {
		final boolean isChecked = getWebElement().isSelected();

		if (STAFCheckbox.logger.isDebugEnabled()) {
			STAFCheckbox.logger.debug(
					String.format("The Checkbox at '%s' is checked: '%s'.", getBy(), Boolean.valueOf(isChecked)));
		}

		return isChecked;
	}

	private void click() {
		if (STAFCheckbox.logger.isDebugEnabled()) {
			STAFCheckbox.logger.debug(String.format("click on element at '%s'.", getBy()));
		}

		final WebDriverWait wait = new WebDriverWait(getWebDriver(), getTimeout());
		wait.until(ExpectedConditions.presenceOfElementLocated(getBy()));
		wait.until(ExpectedConditions.elementToBeClickable(getBy()));

		getWebDriver().findElement(getBy()).click();
	}
}
