package de.simpleworks.staf.commons.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Scanner {
	private static final Logger logger = LogManager.getLogger(Scanner.class);

	/**
	 * @brief looks for any method identified by its name {@param methodname} in
	 *        {@param clazz} or its superclasses.
	 * @param clazz
	 * @param methodname
	 *
	 * @return method, or null if no matching method has been found
	 */
	public static Method getMethod(final Class<?> clazz, final String methodname) {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz can't be null.");
		}

		if (clazz.isInterface()) {
			throw new IllegalArgumentException("clazz can't be an interface.");
		}

		if (Convert.isEmpty(methodname)) {
			throw new IllegalArgumentException("methodname can't be null or empty string.");
		}

		Method result = null;

		for (Class<?> current = clazz; current != null; current = current.getSuperclass()) {

			final Optional<Method> opMethod = UtilsCollection.toList(current.getMethods()).stream()
					.filter(m -> methodname.equals(m.getName())).findAny();

			if (opMethod.isPresent()) {

				if (Scanner.logger.isDebugEnabled()) {
					Scanner.logger.debug(String.format("Class: '%s' is extending '%s'.", Convert.getClassName(clazz),
							Convert.getClassName(current)));
				}

				result = opMethod.get();
				break;
			}
		}

		return result;
	}

	/**
	 * @brief looks for any method identified by its name {@param methodname} in
	 *        {@param clazz} or its superclasses
	 * @param clazz
	 * @param methodname
	 *
	 * @return method, or null if no matching method has been found
	 */
	public static Method getDeclaredMethod(final Class<?> clazz, final String methodname) {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz can't be null.");
		}

		if (clazz.isInterface()) {
			throw new IllegalArgumentException("clazz can't be an interface.");
		}

		if (Convert.isEmpty(methodname)) {
			throw new IllegalArgumentException("methodname can't be null or empty string.");
		}

		Method result = null;

		for (Class<?> current = clazz; current != null; current = current.getSuperclass()) {

			final Optional<Method> opMethod = UtilsCollection.toList(current.getDeclaredMethods()).stream()
					.filter(m -> methodname.equals(m.getName())).findAny();

			if (opMethod.isPresent()) {
				if (Scanner.logger.isDebugEnabled()) {
					Scanner.logger.debug(String.format("Class: '%s' is extending '%s'.", Convert.getClassName(clazz),
							Convert.getClassName(current)));
				}

				result = opMethod.get();
				break;
			}
		}

		return result;
	}

	/**
	 * @brief looks for any field that is annotated with {@param annotationClazz} in
	 *        {@param clazz} or its superclasses
	 * @param clazz           that is checked for its annotated fields
	 * @param annotationClazz class that describes the annotation
	 *
	 * @return List of fields, that wear the respecting annotation
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<Field> getAnnotatedFields(final Class<?> clazz, final Class annotationClazz) {

		if (clazz == null) {
			throw new IllegalArgumentException("clazz can't be null.");
		}

		if (clazz.isInterface()) {
			throw new IllegalArgumentException("clazz can't be an interface.");
		}

		if (annotationClazz == null) {
			throw new IllegalArgumentException("annotationClazz can't be null.");
		}

		if (!Convert.isAnnotation(annotationClazz)) {
			throw new IllegalArgumentException("annotationClazz is invalid.");
		}

		List<Field> result = new ArrayList<>();

		for (Class<?> current = clazz; current != null; current = current.getSuperclass()) {

			for (Field field : current.getDeclaredFields()) {
				if (field.getAnnotation(annotationClazz) != null) {
					if (Scanner.logger.isDebugEnabled()) {
						Scanner.logger.debug(String.format("Field: '%s' in class '%s' has annotation '%s'.",
								field.getName(), Convert.getClassName(clazz), Convert.getClassName(annotationClazz)));
					}
					result.add(field);
				}
			}
		}

		return result;
	}

	// @FIXME: switch paramnames
	public static boolean doesClassExtendSpecificClass(final Class<?> clazz, final Class<?> extendedClass) {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz can't be null.");
		}

		if (clazz.isInterface()) {
			throw new IllegalArgumentException("clazz can't be an interface.");
		}

		if (extendedClass == null) {
			throw new IllegalArgumentException("extendedClass can't be null.");
		}

		boolean result = false;

		for (Class<?> current = clazz; current != null; current = current.getSuperclass()) {
			if (current.equals(extendedClass)) {
				if (Scanner.logger.isDebugEnabled()) {
					Scanner.logger.debug(String.format("Class: '%s' is extending '%s'.", Convert.getClassName(clazz),
							Convert.getClassName(current)));
				}

				result = true;
				break;
			}
		}

		return result;
	}
}
