package de.simpleworks.staf.module.kafka.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.interfaces.IPojo;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public class KafkaProduceRequestHeader implements IPojo {

	private static final Logger logger = LogManager.getLogger(KafkaProduceRequestHeader.class);

	private String key;
	private String value;

	public KafkaProduceRequestHeader() {
		key = Convert.EMPTY_STRING;
		value = Convert.EMPTY_STRING;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public boolean validate() {
		if (KafkaProduceRequestHeader.logger.isTraceEnabled()) {
			KafkaProduceRequestHeader.logger.trace("validate KafkaProduceRequestHeader...");
		}

		boolean result = true;

		if (Convert.isEmpty(key)) {
			KafkaProduceRequestHeader.logger.error("key can't be null or empty string.");
			result = false;
		}

		if (Convert.isEmpty(value)) {
			KafkaProduceRequestHeader.logger.error("value can't be null or empty string.");
			result = false;
		}

		return result;
	}

	@Override
	public boolean equals(Object obj) {

		if (!(obj instanceof KafkaProduceRequestHeader)) {
			return false;
		}

		KafkaProduceRequestHeader header = (KafkaProduceRequestHeader) obj;

		try {

			if (!key.equals(header.getKey())) {
				return false;
			}

			if (!value.equals(header.getValue())) {
				return false;
			}
		} catch (Exception ex) {
			KafkaProduceRequestHeader.logger.error("can't compare assertions.", ex);
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s]", Convert.getClassName(KafkaProduceRequestHeader.class),
				UtilsFormat.format("key", key), UtilsFormat.format("value", value));
	}

}
