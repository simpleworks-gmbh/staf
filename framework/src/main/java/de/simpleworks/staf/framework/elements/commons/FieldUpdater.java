package de.simpleworks.staf.framework.elements.commons;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.framework.elements.commons.properties.TestCaseProperties;

public class FieldUpdater {

	private static final Logger logger = LogManager.getLogger(FieldUpdater.class);

	public final static String PARAM_CLASS = "PARAM_CLASS";
	public final static String PARAM_VALUE = "PARAM_VALUE";

	public final static String CURRENT_STEP_STORAGE_NAME = "CURRENT_STEP_STORAGE";

	private final static TestCaseProperties testCaseProperties = TestCaseProperties.getInstance();

	public static final <T> T updateFields(final Class<? extends T> clazz, final T ob,
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

		Field currentField = null;

		try {
			// update request
			for (final Field field : Arrays.asList(clazz.getDeclaredFields())) {
				field.setAccessible(true);

				currentField = field;

				if (java.lang.reflect.Modifier.isStatic(currentField.getModifiers())) {
					continue;
				}

				if (FieldUpdater.logger.isTraceEnabled()) {
					FieldUpdater.logger.trace(String.format("Update field '%s'.", currentField.getName()));
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

							if (obj == null) {
								if (FieldUpdater.logger.isDebugEnabled()) {
									FieldUpdater.logger.debug(String.format(
											"field '%s' has value null, will skip update.", currentField.getName()));
								}
								continue;
							}

							final Object updatedField = updateFields(currentField.getType(), obj, storage);
							final Object castedupdatedField = theClass.cast(updatedField);

							currentField.set(ob, castedupdatedField);
						} else if (String.class.getName().equals(currentField.getType().getName())) {

							List<T> list = new ArrayList<>();

							list.add(ob);

							// substituteValue
							substituteValue(clazz, UtilsCollection.toArray(ob.getClass(), list), currentField, storage);

							// substituteFunctions
							runFunctions(clazz, UtilsCollection.toArray(ob.getClass(), list), currentField, storage);
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
			FieldUpdater.logger.error(msg, ex);
			throw new Exception(msg);
		}

		return ob;
	}

	private static <T> void substituteValue(final Class<? extends T> clazz, final T[] obs, Field currentField,
			final Map<String, Map<String, String>> storage) throws Exception {

		if (Convert.isEmpty(obs)) {
			throw new IllegalArgumentException("obs can't be null or empty.");
		}

		if (currentField == null) {
			throw new IllegalArgumentException("currentField can't be null.");
		}

		final List<String> keys = UtilsCollection.toList(storage.keySet());

		final String valuePattern = testCaseProperties.getValueSubstitutionRegularExpression();

		if (FieldUpdater.logger.isDebugEnabled()) {
			FieldUpdater.logger
					.debug(String.format("use \"%s\" as regular expression for substitution.", valuePattern));
		}

		final String literal = "#";
		final String substitutePattern = "<&%s" + literal + "%s&>";

		final Object currentFieldValue = currentField.get(obs[0]);

		// Fields that are null, will not be substituted
		if (currentFieldValue == null) {
			currentField.set(obs[0], null);
			return;
		}

		String tmpFieldValue = Convert.EMPTY_STRING;

		try {
			tmpFieldValue = (String) currentFieldValue;
		} catch (Exception ex) {
			final String msg = String.format("Value of field of type '%s' from class '%s' can't be casted to String.",
					currentField.getType(), clazz);
			FieldUpdater.logger.error(msg, ex);
			throw new Exception(msg);
		}

		final String fieldValue = tmpFieldValue;
		String substitute = fieldValue;

		final Optional<String> op = keys.stream().filter(key -> fieldValue.contains(key)).findFirst();
		if (op.isPresent()) {
			final Pattern r = Pattern.compile(valuePattern);
			final Matcher m = r.matcher(fieldValue);

			while (m.find()) {
				final String key = m.group();

				// we expected, that values match the "substitutePattern"
				final String[] parts = key.split(literal);

				if (parts.length != substitutePattern.split(literal).length) {
					throw new IllegalArgumentException(String.format(
							"The value \"%s\" does not match the substitutePattern \"%s\".", key, substitutePattern));
				}

				// the "left" value besides to the literal, needs to be the key!
				final String lefthandassignment = parts[0];
				final String sub = String.join(Convert.EMPTY_STRING, Arrays.asList(parts).subList(1, parts.length));
				final String assertion = sub;

				if (FieldUpdater.logger.isDebugEnabled()) {
					FieldUpdater.logger.debug(String.format("fetch stored values of \"%s\".", lefthandassignment));
				}

				final Map<String, String> map = storage.getOrDefault(lefthandassignment, null);

				if (map == null) {
					if (CURRENT_STEP_STORAGE_NAME.equals(lefthandassignment)) {
						if (FieldUpdater.logger.isInfoEnabled()) {
							FieldUpdater.logger
									.info(String.format("storage '%s', has not been loaded, will not substitute.",
											CURRENT_STEP_STORAGE_NAME));
						}
						continue;
					}

					throw new Exception(String.format("storage \"%s\" is unknown.", lefthandassignment));
				}

				final String value = map.getOrDefault(assertion, "VARIABLE_UNKNOWN");

				if ("VARIABLE_UNKNOWN".equals(value)) {

					if (CURRENT_STEP_STORAGE_NAME.equals(lefthandassignment)) {
						if (FieldUpdater.logger.isInfoEnabled()) {
							FieldUpdater.logger
									.info(String.format("storage '%s', has not been loaded, will not substitute.",
											CURRENT_STEP_STORAGE_NAME));
						}
						continue;
					}

					throw new Exception(String.format("variable \"%s\" is unknown.", assertion));
				}

				if(value == null) {
					substitute = null;
				}
				else {
					substitute = substitute.replace(String.format(substitutePattern, lefthandassignment, assertion), value);
				}
			}
		}
		currentField.set(obs[0], substitute);
	}

	private static <T> void runFunctions(final Class<? extends T> clazz, final T[] obs, Field currentField,
			final Map<String, Map<String, String>> storage) throws Exception {

		if (Convert.isEmpty(obs)) {
			throw new IllegalArgumentException("obs can't be null or empty.");
		}

		if (currentField == null) {
			throw new IllegalArgumentException("currentField can't be null.");
		}

		final String valuePattern = testCaseProperties.getFunctionSubstitutionRegularExpression();

		if (FieldUpdater.logger.isDebugEnabled()) {
			FieldUpdater.logger.debug(String.format("use \"%s\" as regular expression for functions.", valuePattern));
		}
		
		
		final Object currentFieldValue = currentField.get(obs[0]);

		// Fields that are null, will not be substituted
		if (currentFieldValue == null) {
			currentField.set(obs[0], null);
			return;
		}

		String tmpFieldValue = Convert.EMPTY_STRING;

		try {
			tmpFieldValue = (String) currentFieldValue;
		} catch (Exception ex) {
			final String msg = String.format("Value of field of type '%s' from class '%s' can't be casted to String.",
					currentField.getType(), clazz);
			FieldUpdater.logger.error(msg, ex);
			throw new Exception(msg);
		}

		final String fieldValue = tmpFieldValue;
		final String template = testCaseProperties.getFunctionTemplateRegularExpression();

		if (FieldUpdater.logger.isDebugEnabled()) {
			FieldUpdater.logger
					.debug(String.format("use \"%s\" as regular expression for function templates.", template));
		}

		final Pattern pattern = Pattern.compile(template);

		final Matcher mm = pattern.matcher(fieldValue);

		while (mm.find()) {

			final Pattern r = Pattern.compile(valuePattern);

			final Matcher m = r.matcher(mm.group(1));

			List<String> arguments = new ArrayList<String>();

			while (m.find()) {
				arguments.add(m.group());
			}

			final String classname = arguments.remove(0);
			final String methodname = arguments.remove(0);

			final List<Map<String, String>> params = new ArrayList<>();

			// find Parameter
			for (final String argument : arguments) {

				FieldArgument args = new FieldArgument();

				final String argumentsRegEx = testCaseProperties.getArgumentSubstitutionRegularExpression();

				if (FieldUpdater.logger.isDebugEnabled()) {
					FieldUpdater.logger
							.debug(String.format("use \"%s\" as regular expression for arguments.", argumentsRegEx));
				}

				final Pattern argumentsPattern = Pattern.compile(argumentsRegEx);
				final Matcher matchedArgument = argumentsPattern.matcher(argument);

				while (matchedArgument.find()) {
					if (Convert.isEmpty(args.get(PARAM_CLASS))) {
						args.put(PARAM_CLASS, matchedArgument.group());
					} else if (Convert.isEmpty(args.get(PARAM_VALUE))) {
						args.put(PARAM_VALUE, matchedArgument.group());
					}
				}

				params.add(args);
			}

			List<Class<?>> paramClasses = new ArrayList<>();
			List<Object> paramValues = new ArrayList<>();

			for (Map<String, String> param : params) {
				if (Convert.isEmpty(param.getOrDefault(PARAM_CLASS, Convert.EMPTY_STRING))) {
					FieldUpdater.logger.error("'PARAM_CLASS' is null or empty string, will skip.");
					continue;
				}

				if (Convert.isEmpty(param.getOrDefault(PARAM_VALUE, Convert.EMPTY_STRING))) {
					FieldUpdater.logger.error("'PARAM_VALUE' is null or empty string, will skip.");
					continue;
				}

				Class<?> paramTypeClass = null;

				try {
					paramTypeClass = Class.forName(param.get(PARAM_CLASS));
					paramClasses.add(paramTypeClass);
				} catch (Exception ex) {
					if (ex instanceof ClassNotFoundException) {
						FieldUpdater.logger
								.error(String.format("Class '%s' can't be found.", param.get("PARAM_CLASS")));
					} else {
						FieldUpdater.logger.error(String.format("can't fetch Class '%s'.", param.get("PARAM_CLASS")),
								ex);
					}

					throw ex;
				}

				final String paramValue = param.get(PARAM_VALUE);

				if (Integer.class.getName().equals(paramTypeClass.getTypeName())) {
					Integer value = Integer.parseInt(paramValue);
					paramValues.add(value);
				} else if (String.class.getName().equals(paramTypeClass.getTypeName())) {
					paramValues.add(paramValue);
				} else {
					final String msg = String.format("'PARAM_VALUE' of class %s is not implemented yet.",
							paramTypeClass.getClass());
					FieldUpdater.logger.error(msg);
					throw new SystemException(msg);
				}
			}

			Class<?> functionClass = Class.forName(classname);

			Method functionMethod = functionClass.getDeclaredMethod(methodname,
					UtilsCollection.toArray(Class.class, paramClasses));

			Object[] paramArray = new Object[paramValues.size()];

			for (int itr = 0; itr < paramValues.size(); itr += 1) {
				paramArray[itr] = paramValues.get(itr);
			}

			Object result = functionMethod.invoke(functionClass.newInstance(), paramArray);

			if (result instanceof Integer) {
				Integer convertedToInteger = (Integer) result;
				currentField.set(obs[0], convertedToInteger.toString());
			} else if (result instanceof String) {
				String convertedToString = (String) result;
				currentField.set(obs[0], convertedToString);
			} else {
				final String msg = String.format("%s as result type is not implemented yet.", result.getClass());
				FieldUpdater.logger.error(msg);
				throw new SystemException(msg);
			}
		}
	}
}

class FieldArgument extends HashMap<String, String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7476704748181228390L;

	FieldArgument() {
		put(FieldUpdater.PARAM_CLASS, Convert.EMPTY_STRING);
		put(FieldUpdater.PARAM_VALUE, Convert.EMPTY_STRING);
	}
}
