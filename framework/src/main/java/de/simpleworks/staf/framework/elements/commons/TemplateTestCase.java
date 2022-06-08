package de.simpleworks.staf.framework.elements.commons;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

	public final static String CURRENT_STEP_STORAGE_NAME = "CURRENT_STEP_STORAGE";

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

			// reset current "Step Storage"
			addExtractedValues(TemplateTestCase.CURRENT_STEP_STORAGE_NAME,
					Collections.singletonMap("class", this.getClass().toString()));

		} catch (final Exception ex) {
			final String msg = String.format("can't initialize instance of class '%s'.",
					Convert.getClassFullName(this));
			TemplateTestCase.logger.error(msg, ex);
			throw new SystemException(msg);
		}
	}

	protected abstract Teststep updateTeststep(final Teststep step, final Map<String, Map<String, String>> values)
			throws SystemException;

	protected abstract Map<String, String> runAssertion(final Response response, final Assertion assertions)
			throws SystemException;

	protected abstract void getNextTeststep() throws SystemException;

	protected final Map<String, String> validateAssertions(final Response response, final List<Assertion> assertions)
			throws Exception {

		if (response == null) {
			throw new IllegalArgumentException("response can't be null.");
		}

		if (Convert.isEmpty(assertions)) {
			throw new IllegalArgumentException("assertions can't be null or empty.");
		}

		final Map<String, String> result = new HashMap<>();

		if (TemplateTestCase.logger.isDebugEnabled()) {
			TemplateTestCase.logger.debug("run assertions");
		}

		for (final Assertion assertion : assertions) {

			if (!assertion.validate()) {
				throw new IllegalArgumentException(String.format("assertion is invaid [\"5s\"].", assertion));
			}

			final Assertion updatedAssertion = updateFields(Assertion.class, assertion, getExtractedValues());

			if (TemplateTestCase.logger.isDebugEnabled()) {
				TemplateTestCase.logger.debug(String.format("work with assertion: '%s'.", assertion));
			}

			final Map<String, String> results = runAssertion(response, updatedAssertion);

			results.keySet().stream().forEach(key -> {
				result.put(key, results.get(key));
			});

			addExtractedValues(TemplateTestCase.CURRENT_STEP_STORAGE_NAME, results);
		}

		// reset current "Step Storage"
		Map<String, String> map = new HashMap<String, String>();
		map.put("class", this.getClass().toString());

		addExtractedValues(TemplateTestCase.CURRENT_STEP_STORAGE_NAME, map);
		return result;
	}

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

					final String str = tmp.get(k);
					values.put(k, str);
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
				} else if (Object.class.equals(type)) {
					if (Convert.isEmpty(value)) {
						value = null;
						field.set(this, value);
					}
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
		return FieldUpdater.updateFields(clazz, ob, storage);
	}

}
