package de.simpleworks.staf.module.kafka.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.interfaces.IPojo;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;
import de.simpleworks.staf.module.kafka.enums.DeserializerTypeEnum;

public class KafkaConsumeRequestKey implements IPojo {

	private static final Logger logger = LogManager.getLogger(KafkaConsumeRequestKey.class);

	private DeserializerTypeEnum deserializer;
	private String value;

	public KafkaConsumeRequestKey() {
		deserializer = DeserializerTypeEnum.UNKNOWN;
		value = Convert.EMPTY_STRING;
	}

	public DeserializerTypeEnum getDeserializer() {
		return deserializer;
	}

	public void setDeserializer(DeserializerTypeEnum deserializer) {
		this.deserializer = deserializer;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public boolean validate() {
		if (KafkaConsumeRequestKey.logger.isDebugEnabled()) {
			KafkaConsumeRequestKey.logger.debug("validate KafkaConsumeRequestKey...");
		}

		boolean result = true;

		if (deserializer == null) {
			KafkaConsumeRequestKey.logger.error("deserializer can't be null.");
			result = false;
		}

		if (deserializer == DeserializerTypeEnum.UNKNOWN) {
			KafkaConsumeRequestKey.logger
					.error(String.format("can't use deserializer '%s'.", DeserializerTypeEnum.UNKNOWN));
			result = false;
		}

		try {
			Class.forName(deserializer.getValue());
		} catch (Exception ex) {
			KafkaConsumeRequestKey.logger
					.error(String.format("can't find deserializer '%s' on the classpath.", deserializer));
			result = false;
		}

		if (Convert.isEmpty(value)) {
			KafkaConsumeRequestKey.logger.error("value can't be null or empty string.");
			result = false;
		}

		/*
		 * if (Convert.isEmpty(content) && Convert.isEmpty(contentFile)) {
		 * KafkaProduceRequest.logger.error(
		 * "content and contentFile can't be null or empty string, at the same time, one needs to b set."
		 * ); result = false; }
		 * 
		 * else if ((!Convert.isEmpty(content)) && (!Convert.isEmpty(contentFile))) {
		 * KafkaProduceRequest.logger.error(
		 * "content and contentFile are both null or empty string, at the same time, one needs to b set."
		 * ); result = false; }
		 * 
		 * if (!Convert.isEmpty(contentFile)) { File file = new File(contentFile);
		 * 
		 * if (!file.exists()) { KafkaProduceRequest.logger.error(String.
		 * format("the file at '%s' does not exist.", contentFile)); }
		 * 
		 * result = false; }
		 */

		return result;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s]", Convert.getClassName(KafkaConsumeRequestKey.class),
				UtilsFormat.format("deserializer", deserializer), UtilsFormat.format("value", value));
	}

}
