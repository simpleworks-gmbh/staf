package de.simpleworks.staf.commons.utils;

import java.util.Date;

public final class UtilsFormat {
	private static final String NULL = "null";
	private static final String FORMAT_VARIABLE = "%s: %s";
	private static final String FORMAT_STRING = "'%s'";
	private static final String FORMAT_CUTTED = "%s...";
	public static final int MAX_STRING_LEGTH = 10 * 1024;

	private UtilsFormat() {
		throw new IllegalStateException("utility class.");
	}

	private static String format(final String variable, final Object value, final boolean isString) {
		final String text;
		if (value == null) {
			text = UtilsFormat.NULL;
		} else {
			String tmp = value.toString();
			if (UtilsFormat.MAX_STRING_LEGTH < tmp.length()) {
				tmp = String.format(UtilsFormat.FORMAT_CUTTED, tmp.substring(0, UtilsFormat.MAX_STRING_LEGTH));
			}
			text = isString ? String.format(UtilsFormat.FORMAT_STRING, tmp) : tmp;
		}

		return String.format(UtilsFormat.FORMAT_VARIABLE, variable, text);
	}

	public static String format(final String variable, final Date value) {
		return UtilsFormat.format(variable, value);
	}

	public static String format(final String variable, final Object value) {
		return UtilsFormat.format(variable, value, false);
	}

	public static String format(final String variable, final int value) {
		return UtilsFormat.format(variable, Integer.toString(value), false);
	}

	public static String format(final String variable, final String value) {
		return UtilsFormat.format(variable, value, true);
	}

	public static String format(final String variable, final boolean value) {
		return UtilsFormat.format(variable, value ? Boolean.TRUE : Boolean.FALSE, false);
	}
}
