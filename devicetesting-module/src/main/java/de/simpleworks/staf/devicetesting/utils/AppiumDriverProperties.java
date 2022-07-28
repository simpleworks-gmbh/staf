package de.simpleworks.staf.devicetesting.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.annotation.Property;
import de.simpleworks.staf.commons.annotation.Property.Default;
import de.simpleworks.staf.commons.utils.PropertiesReader;
import de.simpleworks.staf.devicetesting.consts.DeviceTestingConsts;

public class AppiumDriverProperties extends PropertiesReader {

	private static final Logger logger = LogManager.getLogger(AppiumDriverProperties.class);
	private static AppiumDriverProperties instance = null;

	@Property(DeviceTestingConsts.APPIUM_DEVICENAME)
	private String deviceName;

	@Property(DeviceTestingConsts.PLATFORM_VERSION)
	private String platformVersion;

	@Property(DeviceTestingConsts.APPIUM_PLATFORMNAME)
	private String platformName;

	@Property(DeviceTestingConsts.APPIUM_APPPACKAGE)
	private String appPackage;

	@Property(DeviceTestingConsts.APPIUM_APPACTIVITY)
	private String appActivity;

	@Property(DeviceTestingConsts.APPIUM_BUNDLEID)
	private String bundleId;

	@Default("http://127.0.0.1:4723/wd/hub")
	@Property(DeviceTestingConsts.APPIUM_SERVER_URL)
	private String appiumServerUrl;

	public String getDeviceName() {
		return deviceName;
	}

	public String getPlatformVersion() {
		return platformVersion;
	}

	public String getPlatformName() {
		return platformName;
	}

	public String getAppPackage() {
		return appPackage;
	}

	public String getAppActivity() {
		return appActivity;
	}

	public String getBundleId() {
		return bundleId;
	}

	public String getAppiumServerUrl() {
		return appiumServerUrl;
	}

	@Override
	protected Class<?> getClazz() {
		return AppiumDriverProperties.class;
	}

	public static final synchronized AppiumDriverProperties getInstance() {
		if (AppiumDriverProperties.instance == null) {
			if (AppiumDriverProperties.logger.isDebugEnabled()) {
				AppiumDriverProperties.logger.debug("create instance.");
			}
			AppiumDriverProperties.instance = new AppiumDriverProperties();
		}
		return AppiumDriverProperties.instance;
	}
}