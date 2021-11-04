package de.simpleworks.staf.framework.util;

import de.simpleworks.staf.commons.utils.Convert;

/**
 * @brief custom implementation of org.junit.Assert
 */
public class AssertionUtils {

	/**
	 * Protect constructor since it is a static only class
	 */
	protected AssertionUtils() {
		throw new IllegalStateException("utility class.");
	}

	/**
	 * Asserts that a condition is true. If it isn't it throws an
	 * {@link AssertionError} with the given message.
	 *
	 * @param message   the identifying message for the {@link AssertionError}
	 *                  (<code>null</code> okay)
	 * @param condition condition to be checked
	 */
	public static void assertTrue(final String message, final boolean condition) {
		if (!condition) {
			AssertionUtils.fail(message);
		}
	}

	/**
	 * Fails a test with the given message.
	 *
	 * @param message the identifying message for the {@link AssertionError}
	 *                (<code>null</code> okay)
	 * @see AssertionError
	 */
	private static void fail(final String message) {
		if (Convert.isEmpty(message)) {
			throw new AssertionError();
		}
		throw new AssertionError(message);
	}
}