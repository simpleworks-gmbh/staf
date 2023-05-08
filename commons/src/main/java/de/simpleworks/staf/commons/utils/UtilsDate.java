package de.simpleworks.staf.commons.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.simpleworks.staf.commons.consts.CommonsConsts;
import de.simpleworks.staf.commons.exceptions.SystemException;

public class UtilsDate {

	private static final Logger logger = LogManager.getLogger(UtilsDate.class);

	private UtilsDate() {
		throw new IllegalStateException("utility class.");
	}

	public static Date getCurrentDateTime() {
		return Calendar.getInstance().getTime();
	}

	public static String getCurrentTimestamp() {
		return Long.toString((new Date()).getTime());
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

	public static Date getCurrentTimeFormatted(final String date, final SimpleDateFormat sdf) {

		if (Convert.isEmpty(date)) {
			throw new IllegalArgumentException("date can't be null or empty string.");
		}

		if (sdf == null) {
			throw new IllegalArgumentException("sdf can't be null.");
		}

		Date result = null;

		try {
			result = sdf.parse(date);
		} catch (ParseException ex) {
			UtilsDate.logger.error(String.format("Can't parse '%s'.", date), ex);
		}

		return result;
	}

	public static long getCurrentTimeInMilliSeonds(final Date date) {
		if (date == null) {
			throw new IllegalArgumentException("date can't be null.");
		}
		return date.getTime();
	}

	/*
	 * @brief method to validate if calendar1 is equal to calendar2.
	 * 
	 * @param date1 (Calendar), calendar2 (Calendar)
	 * 
	 * @return true if date1 is equal to date2 or before
	 * 
	 * @throws SystemException if the timezones of the method arguments do not match
	 */
	public static boolean sameDate(final Calendar calendar1, final Calendar calendar2) throws SystemException {

		if (calendar1 == null) {
			throw new IllegalArgumentException("calendar1 can't be null.");
		}

		if (calendar2 == null) {
			throw new IllegalArgumentException("calendar2 can't be null.");
		}

		final TimeZone timezone1 = calendar1.getTimeZone();
		final ZoneId timeZoneId1 = timezone1.toZoneId();

		final TimeZone timezone2 = calendar2.getTimeZone();
		final ZoneId timeZoneId2 = timezone2.toZoneId();

		if (doTimezonesDiffer(timeZoneId1, timeZoneId2)) {
			throw new SystemException(String.format("dates have different timezones ['%s', '%s'].", timeZoneId1.getId(),
					timeZoneId2.getId()));
		}

		boolean result = calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
				&& calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH)
				&& calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH);

		return result;
	}

	/*
	 * @brief method to validate if calendar1 is before calendar2.
	 * 
	 * @param date1 (Calendar), calendar2 (Calendar)
	 * 
	 * @return true if date1 is equal to date2 or before
	 * 
	 * @throws SystemException if the timezones of the method arguments do not match
	 */
	public static boolean before(final Calendar calendar1, final Calendar calendar2) throws SystemException {

		if (sameDate(calendar1, calendar2)) {
			return false;
		}

		if ((calendar1.getTimeInMillis() > calendar2.getTimeInMillis())) {
			return false;
		}

		return true;
	}

	/*
	 * @brief method to validate if calendar1 is after calendar2.
	 * 
	 * @param date1 (Calendar), calendar2 (Calendar)
	 * 
	 * @return true if date1 is equal to date2 or before
	 * 
	 * @throws SystemException if the timezones of the method arguments do not match
	 */
	public static boolean after(final Calendar calendar1, final Calendar calendar2) throws SystemException {
		return !before(calendar1, calendar2);
	}

	/*
	 * @brief method to validate if timezones zone1 and zone2 lead to different
	 * times
	 * 
	 * @param zone1 (ZoneId), zone2 (ZoneId)
	 * 
	 * @return true if zone1 and zone2 differ
	 */
	public static boolean doTimezonesDiffer(final ZoneId zone1, final ZoneId zone2) {

		if (zone1 == null) {
			throw new IllegalArgumentException("zone1 can't be null.");
		}

		if (zone2 == null) {
			throw new IllegalArgumentException("zone2 can't be null.");
		}

		final LocalDateTime dateTime = LocalDateTime.now();
		final ZonedDateTime zone1DateTime = ZonedDateTime.of(dateTime, zone1);
		final ZonedDateTime deltaDateTime = zone1DateTime.withZoneSameInstant(zone2);

		final boolean result = deltaDateTime.getOffset().getTotalSeconds() > 0;

		return result;
	}
}