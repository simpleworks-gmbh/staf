package de.simpleworks.staf.commons.utils;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import de.simpleworks.staf.commons.exceptions.SystemException;

@TestMethodOrder(OrderAnnotation.class)
class UtilsIOTest {
	@SuppressWarnings("static-method")
	@Test
	@Order(1)
	void testReadProperties() throws SystemException {
		final Properties properties = UtilsIO.readProperties("test.properties");
		Assertions.assertEquals("value", properties.getProperty("key", null), "unexpected value for key.");
	}

	@SuppressWarnings("static-method")
	@Test
	@Order(2)
	void testReadPropertiesPathRelative() {
		final SystemException exception = Assertions.assertThrows(SystemException.class,
				() -> UtilsIO.readProperties("resources/test.properties"), "expected is SystemException.");
		Assertions.assertEquals("can't get InputStream for 'resources/test.properties'.", exception.getMessage());
	}

	@SuppressWarnings("static-method")
	@Test
	@Order(3)
	void testReadPropertiesPathAbsolute() {
		final SystemException exception = Assertions.assertThrows(SystemException.class,
				() -> UtilsIO.readProperties("/resources/test.properties"), "expected is SystemException.");
		Assertions.assertEquals("can't get InputStream for '/resources/test.properties'.", exception.getMessage());
	}

	@SuppressWarnings("static-method")
	@Test
	@Order(10)
	void testReadPropertiesClasspathPlain() throws SystemException {
		final Properties properties = UtilsIO.readProperties("classpath:test.properties");
		Assertions.assertEquals("value", properties.getProperty("key", null), "unexpected value for key.");
	}

	@SuppressWarnings("static-method")
	@Test
	@Order(11)
	void testReadPropertiesClasspathPathRelative() {
		final SystemException exception = Assertions.assertThrows(SystemException.class,
				() -> UtilsIO.readProperties("classpath:resources/test.properties"), "expected is SystemException.");
		Assertions.assertEquals("can't get InputStream for 'classpath:resources/test.properties'.",
				exception.getMessage());
	}

	@SuppressWarnings("static-method")
	@Test
	@Order(12)
	void testReadPropertiesClasspathPathAbsolute() {
		final SystemException exception = Assertions.assertThrows(SystemException.class,
				() -> UtilsIO.readProperties("classpath:/resources/test.properties"), "expected is SystemException.");
		Assertions.assertEquals("can't get InputStream for 'classpath:/resources/test.properties'.",
				exception.getMessage());
	}

	@SuppressWarnings("static-method")
	@Test
	@Order(20)
	void testReadPropertiesFile() {
		final SystemException exception = Assertions.assertThrows(SystemException.class,
				() -> UtilsIO.readProperties("file:test.properties"), "expected is SystemException.");
		Assertions.assertEquals("can't get InputStream for 'file:test.properties'.", exception.getMessage());
	}

	@SuppressWarnings("static-method")
	@Test
	@Order(21)
	void testReadPropertiesFilePathRelative() throws SystemException {
		final Properties properties = UtilsIO.readProperties("file:./src/test/resources/test.properties");
		Assertions.assertEquals("value", properties.getProperty("key", null), "unexpected value for key.");
	}

	@SuppressWarnings("static-method")
	@Test
	@Order(22)
	void testReadPropertiesFilePathAbsolute() throws SystemException, IOException {
		final Properties prop = new Properties();
		prop.put("key", "value");
		final File file = File.createTempFile("test", ".properties");
		UtilsIO.writeProperties(file, prop);

		final String resource = String.format("file:%s", file.getAbsolutePath());
		final Properties properties = UtilsIO.readProperties(resource);
		Assertions.assertEquals("value", properties.getProperty("key", null), "unexpected value for key.");
	}

	@SuppressWarnings("static-method")
	@Test
	@Order(30)
	void testReadPropertiesConfigurationPropertyNotSet() {
		System.getProperties().remove("configuration.file");
		final SystemException exception = Assertions.assertThrows(SystemException.class,
				() -> UtilsIO.readProperties("config:configuration.file"), "expected is SystemException.");
		Assertions.assertEquals("can't get InputStream for 'config:configuration.file'.", exception.getMessage());
	}

	@SuppressWarnings("static-method")
	@Test
	@Order(31)
	void testReadPropertiesConfiguration() throws SystemException {
		System.setProperty("configuration.file", "test.properties");
		final Properties properties = UtilsIO.readProperties("config:configuration.file");
		Assertions.assertEquals("value", properties.getProperty("key", null), "unexpected value for key.");
	}

	@SuppressWarnings("static-method")
	@Test
	@Order(32)
	void testReadPropertiesConfigurationClasspath() {
		System.setProperty("configuration.file", "classpath:test.properties");
		final SystemException exception = Assertions.assertThrows(SystemException.class,
				() -> UtilsIO.readProperties("config:configuration.file"), "expected is SystemException.");
		Assertions.assertEquals("can't get InputStream for 'config:configuration.file'.", exception.getMessage());
	}
}
