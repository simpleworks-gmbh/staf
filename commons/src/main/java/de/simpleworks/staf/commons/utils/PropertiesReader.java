package de.simpleworks.staf.commons.utils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.simpleworks.staf.commons.annotation.Property;
import de.simpleworks.staf.commons.annotation.Property.ClassPath;
import de.simpleworks.staf.commons.annotation.Property.Default;
import de.simpleworks.staf.commons.annotation.Property.NotNull;
import de.simpleworks.staf.commons.consts.PropertiesConsts;
import de.simpleworks.staf.commons.exceptions.SystemException;

public abstract class PropertiesReader {
	private static final Logger logger = LoggerFactory.getLogger(PropertiesReader.class);

	public PropertiesReader() {
		final List<File> files;
		try {
			files = loadFiles();
		} catch (final SystemException ex) {
			final String msg = "can't get list of property files.";
			PropertiesReader.logger.error(msg, ex);
			throw new InstantiationError(msg);
		}

		for (final File file : files) {
			try {
				updateFields(PropertiesReader.loadProperties(file));
			} catch (final SystemException ex) {
				final String msg = String.format("can't read process properties from file: '%s'.", file);
				PropertiesReader.logger.error(msg, ex);
				throw new InstantiationError(msg);
			}
		}
	}

	/**
	 * @return files loaded relativaley from a designated directory, matched by a
	 *         wildcard
	 * @throws SystemException
	 */
	private List<File> loadFiles() throws SystemException {
		final List<File> result = new ArrayList<>();

		final String fileName = getName();
		final String root = System.getProperty(PropertiesConsts.PROPERTY_FILE_ROOT, "src/main/resources");

		for (String filePath : Arrays.asList(root.split(","))) {
			if (PropertiesReader.logger.isDebugEnabled()) {
				PropertiesReader.logger.debug(String.format("search for files in: '%s'.", filePath));
			}

			UtilsIO.listFiles(new File(filePath), fileName).forEach(file -> result.add(file));
			if (Convert.isEmpty(result)) {
				throw new SystemException(
						String.format("Cannot configure properties files, identified by '%s'.", getName()));
			}
		}

		return result;
	}

	private static Properties loadProperties(final File file) throws SystemException {
		if (file == null) {
			throw new IllegalArgumentException("file can't be null.");
		}

		if (!file.exists()) {
			throw new IllegalArgumentException(String.format("file at '%s' does not exist.", file.getAbsolutePath()));
		}

		final Properties result = UtilsIO.readProperties(file);
		try {
			result.keySet().stream().forEach(k -> {
				final String key = (String) k;
				final String value = (String) result.get(key);

				if (Convert.isEmpty(System.getProperty(key, null))) {
					if (PropertiesReader.logger.isDebugEnabled()) {
						PropertiesReader.logger.debug(String.format("Set Property '%s' with value '%s'.", key, value));
					}

					System.setProperty(key, value);
				}
			});
		} catch (final Exception ex) {
			final String msg = String.format("can't set system properties from file '%s'.", file);
			PropertiesReader.logger.error(msg, ex);
			throw new SystemException(msg);
		}

		return result;
	}

	private void updateFields(final Properties properties) throws SystemException {
		if (properties == null) {
			throw new IllegalArgumentException("properties can't be null.");
		}

		for (@SuppressWarnings("rawtypes")
		Class tempClass = this.getClass(); tempClass != null; tempClass = tempClass.getSuperclass()) {
			for (final Field field : Arrays.asList(tempClass.getDeclaredFields())) {
				final Property property = field.getAnnotation(Property.class);
				if (property == null) {
					continue;
				}

				final String key = property.value();
				String value = System.getProperty(key, null);
				try {
					if (Convert.isEmpty(value)) {
						if (field.getAnnotation(NotNull.class) != null) {
							throw new SystemException(
									String.format("Property '%s' may not be set to null value: ", key));
						}

						final Default defaultValue = field.getAnnotation(Default.class);
						if (defaultValue != null) {
							value = defaultValue.value();
							if (PropertiesReader.logger.isDebugEnabled()) {
								PropertiesReader.logger.debug(
										String.format("Property '%s' is set to default value: '%s'.", key, value));
							}
						}
					}

					setField(this, field, StringUtils.trimToNull(value));
				} catch (final Exception ex) {
					final String msg = String.format("Error setting property: key: '%s' value: '%s'.", key, value);
					PropertiesReader.logger.error(msg, ex);
					throw new SystemException(msg);
				}
			}
		}
	}

	private void setField(final Object ob, final Field field, final String value) throws SystemException {
		if (ob == null) {
			throw new IllegalArgumentException("ob can't be null.");
		}

		if (field == null) {
			throw new IllegalArgumentException("field can't be null.");
		}

		try {
			field.setAccessible(true);

			final Class<?> type = field.getType();

			if ((field.getAnnotation(ClassPath.class) != null)) {
				field.set(ob, loadClass(type, value));
			} else {
				// FIXME define converting methods (like getInteger()) in Convert and use they
				// here.
				if (int.class.equals(type)) {
					field.set(ob, Integer.valueOf(value));
				} else if (double.class.equals(type)) {
					field.set(ob, Double.valueOf(value));
				} else if (boolean.class.equals(type)) {
					field.set(ob, Boolean.valueOf(value));
				} else if (float.class.equals(type)) {
					field.set(ob, Float.valueOf(value));
				} else if (long.class.equals(type)) {
					field.set(ob, Long.valueOf(value));
				} else if (String.class.equals(type)) {
					field.set(ob, value);
				} else if (Map.class.equals(type)) {
					field.set(ob, PropertiesReader.setMap(value));
				} else if (type.isEnum()) {
					final Object typeValue = UtilsEnum.getEnum(type, value);

					field.set(ob, typeValue);
				} else {
					throw new IllegalArgumentException(
							String.format("Cannot handle type: '%s', value '%s'.", type, value));
				}
			}
		} catch (final Exception ex) {
			final String msg = String.format("for object '%s': can't set field: '%s' to value: '%s'.", ob, field,
					value);
			PropertiesReader.logger.error(msg, ex);
			throw new SystemException(msg);
		}
	}

	private Object loadClass(final Class<?> type, final String className) throws SystemException {
		if (type == null) {
			throw new IllegalArgumentException("type can't be null.");
		}

		if (Convert.isEmpty(className)) {
			throw new IllegalArgumentException("className can't be null or empty string.");
		}

		if (PropertiesReader.logger.isTraceEnabled()) {
			PropertiesReader.logger.trace(String.format("loading class: '%s'.", className));
		}

		try {
			// get class via class loader
			final Class<?> clazz = this.getClass().getClassLoader().loadClass(className);
			if (!(Scanner.doesClassExtendSpecificClass(clazz, type))) {
				throw new SystemException(String.format("type '%s' does not extend class '%s'.",
						type.getClass().getName(), clazz.getName()));
			}

			if (PropertiesReader.logger.isTraceEnabled()) {
				PropertiesReader.logger.trace(String.format("create new instance for class: '%s'.", className));
			}

			return clazz.getDeclaredConstructor().newInstance();

		} catch (final Throwable th) {
			final String msg = String.format("can't create new instance for class: '%s'.", className);
			PropertiesReader.logger.error(msg, th);
			throw new SystemException(msg);
		}
	}

	private static Map<String, String> setMap(final String content) throws Exception {
		if (Convert.isEmpty(content)) {
			throw new IllegalArgumentException("value can't be null or empty string.");
		}

		final Map<String, String> result = new HashedMap<>();
		for (final String literal : Arrays.asList(content.split(";"))) {
			if (!literal.contains(":")) {
				throw new Exception(String.format("literal '%s' does not contain '%s'.", literal, ":"));
			}

			final String[] keyValue = literal.split(":");
			if (keyValue.length != 2) {
				throw new Exception(String.format("literal '%s' is invalid.", literal));
			}

			final String key = keyValue[0];
			final String value = keyValue[1];

			if (result.containsKey(key)) {
				if (PropertiesReader.logger.isDebugEnabled()) {
					PropertiesReader.logger
							.debug(String.format("key '%s' is already stored, override with value: '%s'.", key, value));
				}
			}

			result.put(key, value);
		}

		return result;
	}

	@SuppressWarnings("static-method")
	protected String getName() {
		return "*.properties";
	}

	protected abstract Class<?> getClazz();
}