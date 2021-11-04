package de.simpleworks.staf.framework.elements.commons;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Module;

import de.simpleworks.staf.commons.annotation.Property;
import de.simpleworks.staf.commons.annotation.Step;
import de.simpleworks.staf.commons.api.Assertion;
import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.interfaces.ITeststep;
import de.simpleworks.staf.commons.mapper.Mapper;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.Scanner;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.commons.utils.UtilsEnum;
import de.simpleworks.staf.framework.api.httpclient.TeststepProvider;

public abstract class TemplateTestCase<Teststep extends ITeststep, Response> extends TestCase {
	private static final Logger logger = LogManager.getLogger(TemplateTestCase.class);

	private final String environmentName;
	private final TeststepProvider<Teststep> provider;

	protected TemplateTestCase(final String resource, final String environmentName, Mapper<Teststep> mapper,
			final Module... modules) throws SystemException {
		super(modules);

		if (Convert.isEmpty(resource)) {
			throw new IllegalArgumentException("filePath can't be null or empty string.");
		}

		if (Convert.isEmpty(environmentName)) {
			throw new IllegalArgumentException("environmentName can't be null or empty string.");
		}

		if (mapper == null) {
			throw new IllegalArgumentException("mapper can't be null.");
		}

		try {

			this.environmentName = environmentName;

			initEnvironmentVariables();

			if (TemplateTestCase.logger.isDebugEnabled()) {
				TemplateTestCase.logger.debug(String.format("read steps from: '%s'.", resource));
			}
			final List<Teststep> steps = mapper.read(resource);

			final List<Step> methodSteps = UtilsCollection.toList(this.getClass().getMethods()).stream()
					.map(method -> method.getAnnotation(Step.class)).filter(Objects::nonNull)
					.collect(Collectors.toList());

			provider = new TeststepProvider<>(steps, methodSteps);

		} catch (final Exception ex) {
			final String msg = String.format("can't initialize instance of class '%s'.",
					Convert.getClassFullName(this));
			TemplateTestCase.logger.error(msg, ex);
			throw new SystemException(msg);
		}
	}

	protected abstract Map<String, String> validateAssertions(final Response response, final List<Assertion> assertions)
			throws SystemException;

	protected abstract Teststep updateTeststep(final Teststep step, final Map<String, Map<String, String>> values)
			throws SystemException;

	protected abstract void getNextTeststep() throws SystemException;

	protected void addExtractedValues(final String key, final Map<String, String> values) {
		if (Convert.isEmpty(key)) {
			throw new IllegalArgumentException("key can't be null or empty string.");
		}

		if (values == null) {
			return;
		}

		if (TemplateTestCase.logger.isDebugEnabled()) {
			TemplateTestCase.logger.debug(String.format("add variables for key: '%s'.", key));
		}

		if (getExtractedValues() == null) {
			throw new IllegalStateException("extractedValues can't be null.");
		}

		if (getExtractedValues().containsKey(key)) {
			final Map<String, String> tmp = getExtractedValues().get(key);

			tmp.keySet().stream().forEach(k -> {
				if (!values.containsKey(k)) {
					values.put(k, tmp.get(k));
				}
			});
		}

		getExtractedValues().put(key, values);
	}

	protected TeststepProvider<Teststep> getProvider() {
		return provider;
	}

	protected void initEnvironmentVariables() throws Exception {

		final Map<String, String> map = new HashMap<>();

		for (Field field : Scanner.getAnnotatedFields(this.getClass(), Property.class)) {
			field.setAccessible(true);

			Property property = field.getAnnotation(Property.class);
			if (property != null) {

				final String propertyKey = property.value();
				final String name = field.getName();

				String value = System.getProperty(propertyKey, Convert.EMPTY_STRING);

				if (field.getAnnotation(Inject.class) != null) {
					try {
						if (Convert.isEmpty(value)) {
							value = (String) field.get(this);
						}
					} catch (Exception ex) {
						final String msg = "can't determine value.";
						TemplateTestCase.logger.error(msg, ex);
						throw new SystemException(msg);
					}
				}

				final Class<?> type = field.getType();

				if (int.class.equals(type)) {
					field.set(this, Integer.valueOf(value));
				} else if (double.class.equals(type)) {
					field.set(this, Double.valueOf(value));
				} else if (boolean.class.equals(type)) {
					field.set(this, Boolean.valueOf(value));
				} else if (float.class.equals(type)) {
					field.set(this, Float.valueOf(value));
				} else if (long.class.equals(type)) {
					field.set(this, Long.valueOf(value));
				} else if (String.class.equals(type)) {
					field.set(this, value);
				} else if (type.isEnum()) {
					final Object typeValue = UtilsEnum.getEnum(type, value);
					field.set(this, typeValue);
				} else {
					throw new IllegalArgumentException(
							String.format("Cannot handle type: '%s', value '%s'.", type, value));
				}

				map.put(name, value);

				if (TemplateTestCase.logger.isDebugEnabled()) {
					TemplateTestCase.logger.debug(String.format("save map \"%s:%s\".", name, value));
				}

			} else {
				throw new InstantiationError(String.format("can't set up variable %s.", this.environmentName));
			}

			addExtractedValues(this.environmentName, map);
		}
	}

	/**
	 * @brief method to update "String" declared fields in {@param} ob
	 * @param Class<? extends T> clazz; class of {@param ob}, T ob; object that
	 *                should be updated, List<String> keys; list of keys, to
	 *                describe a specific, result, to update the value of a field in
	 *                {@param ob}
	 * @return T copy of updated {@param ob}
	 * @throws Exception
	 *
	 **/
	protected final <T> T updateFields(final Class<? extends T> clazz, final T ob,
			final Map<String, Map<String, String>> storage) throws Exception {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz can't be null.");
		}

		if (ob == null) {
			throw new IllegalArgumentException("ob can't be null.");
		}

		if (storage == null) {
			throw new IllegalArgumentException("storage can't be null.");
		}

		if (storage.keySet().isEmpty()) {
			throw new IllegalArgumentException("storage can't be empty.");
		}

		final List<String> keys = UtilsCollection.toList(storage.keySet());

		// FIXME: put all the constants in a config file.
		final String valuePattern = "(?<=\\<&)(.*?)(?=\\&>)";
		final String literal = "#";
		final String substitutePattern = "<&%s" + literal + "%s&>";

		Field currentField = null;

		try {
			// update request
			for (final Field field : Arrays.asList(clazz.getDeclaredFields())) {
				field.setAccessible(true);

				currentField = field;

				if (java.lang.reflect.Modifier.isStatic(currentField.getModifiers())) {
					continue;
				}

				if (TemplateTestCase.logger.isTraceEnabled()) {
					TemplateTestCase.logger.trace(String.format("Update field '%s'.", currentField.getName()));
				}

				if (currentField.getType().isArray()) {
					final Object[] arrayField = (Object[]) currentField.get(ob);
					if (arrayField.length != 0) {
						final List<Object> updatedArray = new ArrayList<>();
						final String clazzName = currentField.getType().getComponentType().getName();

						final Class<?> theClass = Class.forName(clazzName);
						for (final Object singleField : arrayField) {
							final Object obj = theClass.cast(singleField);
							final Object updatedObject = updateFields(currentField.getType().getComponentType(), obj,
									storage);

							updatedArray.add(theClass.cast(updatedObject));
						}

						currentField.set(ob, UtilsCollection.toArray(theClass, updatedArray));
					}
				} else {
					if (!currentField.getType().isPrimitive()) {
						if (!(String.class.getName().equals(currentField.getType().getName()))) {
							final String clazzName = currentField.getType().getName();
							final Class<?> theClass = Class.forName(clazzName);
							final Object fieldValue = currentField.get(ob);

							final Object obj = theClass.cast(fieldValue);
							final Object updatedField = updateFields(currentField.getType(), obj, storage);
							final Object castedupdatedField = theClass.cast(updatedField);

							currentField.set(ob, castedupdatedField);
						} else if (String.class.getName().equals(currentField.getType().getName())) {
							final String fieldValue = (String) currentField.get(ob);
							String substitute = fieldValue;

							final Optional<String> op = keys.stream().filter(key -> fieldValue.contains(key))
									.findFirst();
							if (op.isPresent()) {
								final Pattern r = Pattern.compile(valuePattern);
								final Matcher m = r.matcher(fieldValue);

								while (m.find()) {
									final String key = m.group();

									// we expected, that values match the "substitutePattern"
									final String[] parts = key.split(literal);

									if (parts.length != substitutePattern.split(literal).length) {
										throw new IllegalArgumentException(String.format(
												"The value \"%s\" does not match the substitutePattern \"%s\".", key,
												substitutePattern));
									}

									// the "left" value besides to the literal, needs to be the key!
									final String lefthandassignment = parts[0];
									final String sub = String.join(Convert.EMPTY_STRING,
											Arrays.asList(parts).subList(1, parts.length));
									final String assertion = sub;

									if (TemplateTestCase.logger.isDebugEnabled()) {
										TemplateTestCase.logger.debug(
												String.format("fetch stored values of \"%s\".", lefthandassignment));
									}

									final Map<String, String> map = storage.getOrDefault(lefthandassignment, null);

									if (map == null) {
										throw new Exception(
												String.format("storage \"%s\" is unknown.", lefthandassignment));
									}

									final String value = map.getOrDefault(assertion, null);

									if (Convert.isEmpty(value)) {
										throw new Exception(String.format("variable \"%s\" is unknown.", assertion));
									}

									substitute = substitute.replace(
											String.format(substitutePattern, lefthandassignment, assertion), value);
								}
							}
							currentField.set(ob, substitute);
						}
					}
				}
			}
		} catch (final Exception ex) {
			final String msg = String.format("can't update field '%s' with any of the stored values '%s'.",
					(currentField == null ? null : currentField.getName()),
					storage.entrySet().stream()
							.map(s -> String.format("key: '%s', value: '%s'", s.getKey(), s.getValue()))
							.collect(Collectors.joining(";")));
			TemplateTestCase.logger.error(msg, ex);
			throw new Exception(msg);
		}

		return ob;
	}

}
