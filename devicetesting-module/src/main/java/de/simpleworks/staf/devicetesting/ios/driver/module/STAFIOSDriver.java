package de.simpleworks.staf.devicetesting.ios.driver.module;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.remote.DesiredCapabilities;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.devicetesting.commons.driver.module.STAFAppiumDriver;
import de.simpleworks.staf.devicetesting.consts.DeviceTestingConsts;
import de.simpleworks.staf.devicetesting.utils.AppiumDriverProperties;
import io.appium.java_client.ios.IOSDriver;

public class STAFIOSDriver extends STAFAppiumDriver<IOSDriver> {

	private static final Logger logger = LogManager.getLogger(STAFIOSDriver.class);

	private AppiumDriverProperties properties = AppiumDriverProperties.getInstance();

	@Override
	public boolean validate() {

		boolean result = true;

		if (Convert.isEmpty(properties.getDeviceName())) {
			STAFIOSDriver.logger.error(String.format("PROPERTY '%s' can't be null or empty string.",
					DeviceTestingConsts.APPIUM_DEVICENAME));
			result = false;
		}

		if (Convert.isEmpty(properties.getPlatformVersion())) {
			STAFIOSDriver.logger.error(String.format("PROPERTY '%s' can't be null or empty string.",
					DeviceTestingConsts.APPIUM_DEVICENAME));
			result = false;
		}

		if (Convert.isEmpty(properties.getPlatformName())) {
			STAFIOSDriver.logger.error(String.format("PROPERTY '%s' can't be null or empty string.",
					DeviceTestingConsts.APPIUM_PLATFORMNAME));
			result = false;
		}

		if (Convert.isEmpty(properties.getBundleId())) {
			STAFIOSDriver.logger.error(
					String.format("PROPERTY '%s' can't be null or empty string.", DeviceTestingConsts.APPIUM_BUNDLEID));
			result = false;
		}

		if (Convert.isEmpty(properties.getAppiumServerUrl())) {
			STAFIOSDriver.logger.error(String.format("PROPERTY '%s' can't be null or empty string.",
					DeviceTestingConsts.APPIUM_APPPACKAGE));
			result = false;
		}

		return result;

	}

	@Override
	public IOSDriver createAppiumDriver() {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability("appium:deviceName", properties.getDeviceName());
		capabilities.setCapability("platformVersion", properties.getPlatformVersion());
		capabilities.setCapability("appium:platformName", properties.getPlatformName());
		capabilities.setCapability("appium:bundleId", properties.getBundleId());

		URL url;
		final String urlFromAppiumServer = properties.getAppiumServerUrl();
		try {
			url = new URL(urlFromAppiumServer);
		} catch (MalformedURLException ex) {
			final String msg = String.format("can't set up URL from '%s'.", urlFromAppiumServer);
			throw new RuntimeException(msg, ex);
		}

		IOSDriver result = new IOSDriver(url, capabilities);
		return result;
	}
}