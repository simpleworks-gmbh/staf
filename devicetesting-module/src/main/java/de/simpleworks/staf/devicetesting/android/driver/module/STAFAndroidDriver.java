package de.simpleworks.staf.devicetesting.android.driver.module;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.remote.DesiredCapabilities;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.devicetesting.commons.driver.module.STAFAppiumDriver;
import de.simpleworks.staf.devicetesting.consts.DeviceTestingConsts;
import de.simpleworks.staf.devicetesting.utils.AppiumDriverProperties;
import io.appium.java_client.android.AndroidDriver;

public class STAFAndroidDriver extends STAFAppiumDriver<AndroidDriver> {

	private static final Logger logger = LogManager.getLogger(STAFAndroidDriver.class);

	private AppiumDriverProperties properties = AppiumDriverProperties.getInstance();

	@Override
	public boolean validate() {

		boolean result = true;

		if (Convert.isEmpty(properties.getDeviceName())) {
			STAFAndroidDriver.logger.error(String.format("PROPERTY '%s' can't be null or empty string.",
					DeviceTestingConsts.APPIUM_DEVICENAME));
			result = false;
		}

		if (Convert.isEmpty(properties.getPlatformVersion())) {
			STAFAndroidDriver.logger.error(String.format("PROPERTY '%s' can't be null or empty string.",
					DeviceTestingConsts.APPIUM_DEVICENAME));
			result = false;
		}

		if (Convert.isEmpty(properties.getPlatformName())) {
			STAFAndroidDriver.logger.error(String.format("PROPERTY '%s' can't be null or empty string.",
					DeviceTestingConsts.APPIUM_PLATFORMNAME));
		}

		if (Convert.isEmpty(properties.getAppPackage())) {
			STAFAndroidDriver.logger.error(String.format("PROPERTY '%s' can't be null or empty string.",
					DeviceTestingConsts.APPIUM_APPPACKAGE));
		}

		if (Convert.isEmpty(properties.getAppActivity())) {
			STAFAndroidDriver.logger.error(String.format("PROPERTY '%s' can't be null or empty string.",
					DeviceTestingConsts.APPIUM_APPACTIVITY));
		}

		if (Convert.isEmpty(properties.getAppiumServerUrl())) {
			STAFAndroidDriver.logger.error(String.format("PROPERTY '%s' can't be null or empty string.",
					DeviceTestingConsts.APPIUM_APPPACKAGE));
		}

		return result;

	}

	@Override
	public AndroidDriver createAppiumDriver() {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability("appium:deviceName", properties.getDeviceName());
		capabilities.setCapability("platformVersion", properties.getPlatformVersion());
		capabilities.setCapability("appium:platformName", properties.getPlatformName());
		capabilities.setCapability("appium:appPackage", properties.getAppPackage());
		capabilities.setCapability("appium:appActivity", properties.getAppActivity());

		URL url;
		final String urlFromAppiumServer = properties.getAppiumServerUrl();
		try {
			url = new URL(urlFromAppiumServer);
		} catch (MalformedURLException ex) {
			final String msg = String.format("can't set up URL from '%s'.", urlFromAppiumServer);
			throw new RuntimeException(msg, ex);
		}

		AndroidDriver result = new AndroidDriver(url, capabilities);
		return result;
	}
}