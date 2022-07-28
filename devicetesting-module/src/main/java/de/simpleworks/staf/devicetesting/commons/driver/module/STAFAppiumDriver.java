package de.simpleworks.staf.devicetesting.commons.driver.module;

import org.openqa.selenium.WebDriver;

import de.simpleworks.staf.framework.gui.webdriver.module.WebDriverManagerImpl;
import io.appium.java_client.AppiumDriver;

public abstract class STAFAppiumDriver<DeviceDriver extends AppiumDriver> extends WebDriverManagerImpl {

	/**
	 * @brief method to validate if the driver's configuration is valid.
	 */
	public abstract boolean validate();
	

	public abstract DeviceDriver createAppiumDriver();
	

	@Override
	protected WebDriver createDriver() { 

		WebDriver result = null;

		if (validate()) {
			DeviceDriver driver = createAppiumDriver();
			if (!(driver instanceof WebDriver)) {
				throw new RuntimeException(String.format("driver is no instance of \"%s\".", WebDriver.class));
			}
			result = (WebDriver) driver;
		}

		return result;
	}
}