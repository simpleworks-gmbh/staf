package de.simpleworks.staf.commons.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.consts.CommonsConsts;

public class UtilsDate {
	private static final Logger logger = LogManager.getLogger(UtilsDate.class);

	private UtilsDate() {
		throw new IllegalStateException("utility class.");
	}

	public static Date getCurrentDateTime() {
		return Calendar.getInstance().getTime();
	}

	public static String getCurrentTime() {
		return UtilsDate.getCurrentTimeFormatted(UtilsDate.getCurrentDateTime());
	}

	public static String getCurrentTimeFormatted(final Date date) {
		if (date == null) {
			throw new IllegalArgumentException("date can't be null.");
		}

		final String result = new SimpleDateFormat(CommonsConsts.DATE_TIME_FORMATTER).format(date);

		if (UtilsDate.logger.isDebugEnabled()) {
			UtilsDate.logger.debug(String.format("Return current TimeStamp: '%s'.", result));
		}

		return result;
	}

	public static String getCurrentTimeFormatted(final Date date, final SimpleDateFormat sdf) {
		if (date == null) {
			throw new IllegalArgumentException("date can't be null.");
		}

		if (sdf == null) {
			throw new IllegalArgumentException("sdf can't be null.");
		}

		final String result = sdf.format(date);

		if (UtilsDate.logger.isDebugEnabled()) {
			UtilsDate.logger.debug(String.format("Return current TimeStamp: '%s'.", result));
		}

		return result;
	}

	public static long getCurrentTimeInMilliSeonds(final Date date) {
		if (date == null) {
			throw new IllegalArgumentException("date can't be null.");
		}

		return date.getTime();
	}
}
