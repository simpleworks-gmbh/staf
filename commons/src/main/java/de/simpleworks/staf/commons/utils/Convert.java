package de.simpleworks.staf.commons.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Convert {

	private static final Logger logger = LogManager.getLogger(Convert.class);

	public static final String EMPTY_STRING = "";
	public static final String BLANK_STRING = " ";
	public static final String SLASH_STRING = "/";

	public static final String UNKNOWN = "UNKNOWN";
	private static final String DEFAULT_TIMESTAMP_FORMAT = "dd.MM.yyyy HH:mm:ss";

	private Convert() {
		throw new IllegalStateException("utility class.");
	}

	public static String getDate(final Date date) {
		return new SimpleDateFormat(Convert.DEFAULT_TIMESTAMP_FORMAT).format(date);
	}

	public static boolean isEmpty(final String text) {
		return (text == null) || (Convert.EMPTY_STRING.equals(text.trim()));
	}

	public static <T> boolean isEmpty(final Collection<T> list) {
		return (list == null) || (list.size() == 0);
	}

	public static <T> boolean isEmpty(final List<T> list) {
		return (list == null) || (list.size() == 0);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static boolean isAnnotation(final Class clazz) {

		if (clazz == null) {
			throw new IllegalArgumentException("clazz can't be null.");
		}

		if (!clazz.isInterface()) {
			if (logger.isDebugEnabled()) {
				logger.debug("clazz must be an interface.");
			}

			return false;
		}

		if (!clazz.isAnnotationPresent(Retention.class)) {

			if (logger.isDebugEnabled()) {
				logger.debug("clazz has no Retention Annotation.");
			}

			return false;
		}

		if (!clazz.isAnnotationPresent(Target.class)) {

			if (logger.isDebugEnabled()) {
				logger.debug("clazz has no Target Annotation.");
			}

			return false;
		}

		return true;
	}

	public static <T> boolean isEmpty(final T[] array) {
		return (array == null) || (array.length == 0);
	}

	public static <T, U> boolean isEmpty(final Map<T, U> map) {
		return (map == null) || (map.isEmpty());
	}

	public static String getClassName(final Class<?> clazz) {
		return clazz == null ? Convert.UNKNOWN : clazz.getSimpleName();
	}

	public static String getClassName(final Object object) {
		return object == null ? Convert.UNKNOWN : Convert.getClassName(object.getClass());
	}

	public static String getClassFullName(final Class<?> clazz) {
		return clazz == null ? Convert.UNKNOWN : clazz.getCanonicalName();
	}

	public static String getClassFullName(final Object object) {
		return object == null ? Convert.UNKNOWN : Convert.getClassFullName(object.getClass());
	}

	@SuppressWarnings("unused")
	public static boolean isBoolean(final Object ob) {
		if (ob == null) {
			throw new IllegalArgumentException("ob can't be null.");
		}

		try {
			@SuppressWarnings("boxing")
			final boolean i = (boolean) ob;
			return true;
		} catch (final Exception nfe) {
			// nothing to do
		}

		return false;
	}

	@SuppressWarnings("unused")
	public static boolean isNumeric(final Object ob) {
		if (ob == null) {
			throw new IllegalArgumentException("ob can't be null.");
		}

		try {
			@SuppressWarnings("boxing")
			final int i = (int) ob;
			return true;
		} catch (final Exception nfe) {
			// nothing to do
		}

		try {
			@SuppressWarnings("boxing")
			final float f = (float) ob;
			return true;
		} catch (final Exception nfe) {
			// nothing to do
		}

		try {
			@SuppressWarnings("boxing")
			final double d = (double) ob;
			return true;
		} catch (final Exception nfe) {
			// nothing to do
		}

		try {
			@SuppressWarnings("boxing")
			final long l = (long) ob;
			return true;
		} catch (final Exception nfe) {
			// nothing to do
		}

		try {
			final BigInteger bigInteger = (BigInteger) ob;
			return true;
		} catch (final Exception nfe) {
			// nothing to do
		}

		return false;
	}

	/**
	 * @brief method that returns the numeric representation of a string input
	 * @param (String) numericValue
	 * @return numeric representation of {@param numericValue}, -1 if input can't be
	 *         parsed.
	 */
	public static int getNumericValue(final String numericValue) {

		if (Convert.isEmpty(numericValue)) {
			return Integer.MAX_VALUE;
		}

		int result = -1;

		try {
			result = Integer.parseInt(numericValue);
		} catch (NumberFormatException ex) {
			Convert.logger.error(String.format("can't convert '%s' to a numeric value.", numericValue), ex);
		}

		return result;
	}
}
