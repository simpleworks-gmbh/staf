package de.simpleworks.staf.module.kafka.api;

import java.text.SimpleDateFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.interfaces.IPojo;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;
import de.simpleworks.staf.module.kafka.enums.TimestampAllowedValueEnum;

public class KafkaConsumeRequestTimestamp implements IPojo {

	private static final Logger logger = LogManager.getLogger(KafkaConsumeRequestTimestamp.class);
	
	private TimestampAllowedValueEnum allowedValue;
	private String timezone;
	private String format;
	private String value;
	
	public KafkaConsumeRequestTimestamp() {
		allowedValue = TimestampAllowedValueEnum.UNKNOWN;
		timezone = Convert.EMPTY_STRING;
		format = Convert.EMPTY_STRING;
		value = Convert.EMPTY_STRING;
	}

	public void setAllowedValue(TimestampAllowedValueEnum allowedValue) {
		this.allowedValue = allowedValue;
	}
	
	public TimestampAllowedValueEnum getAllowedValue() {
		return allowedValue;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}
	
	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public boolean validate() {
		
		if (KafkaConsumeRequestTimestamp.logger.isTraceEnabled()) {
			KafkaConsumeRequestTimestamp.logger.trace("validate KafkaConsumeRequestTimestamp...");
		}
		
		boolean result = true;
		
		if (Convert.isEmpty(format)) {
			KafkaConsumeRequestTimestamp.logger.error("format can't be null or empty string.");
			result = false;
		}
		else {
			
			final SimpleDateFormat sdf = new SimpleDateFormat();
			
			try {
				sdf.applyPattern(format);
			}
			catch(Exception ex) {
				KafkaConsumeRequestTimestamp.logger.error(String.format("format '%s' is invalid.", format), ex);
				result = false;
			}
		}
		
		if (Convert.isEmpty(timezone)) {
			KafkaConsumeRequestTimestamp.logger.error("timezone can't be null or empty string.");
			result = false;
		}
		
		if (Convert.isEmpty(value)) {
			KafkaConsumeRequestTimestamp.logger.error("value can't be null or empty string.");
			result = false;
		}

		if (allowedValue == TimestampAllowedValueEnum.UNKNOWN) {
			KafkaConsumeRequestTimestamp.logger
					.error(String.format("can't use allowedValue '%s'.", TimestampAllowedValueEnum.UNKNOWN));
			result = false;
		}
	
		return result;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s, %s, %s]", Convert.getClassName(KafkaConsumeRequestTimestamp.class),
				UtilsFormat.format("format", format), UtilsFormat.format("timezone", timezone),  UtilsFormat.format("value", value),
				UtilsFormat.format("allowedValue", allowedValue));
	}
}
