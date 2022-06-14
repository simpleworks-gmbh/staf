package de.simpleworks.staf.module.kafka.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.interfaces.IPojo;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;
import de.simpleworks.staf.module.kafka.enums.SerializerTypeEnum;

public class KafkaProduceRequestKey implements IPojo {

	private static final Logger logger = LogManager.getLogger(KafkaProduceRequestKey.class);

	private SerializerTypeEnum serializer;
 
	private String value;

	public KafkaProduceRequestKey() {
		serializer = SerializerTypeEnum.UNKNOWN;
		value = Convert.EMPTY_STRING;
	}

	public SerializerTypeEnum getSerializer() {
		return serializer;
	}

	public void setSerializer(SerializerTypeEnum serializer) {
		this.serializer = serializer;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public boolean validate() {
		if (KafkaProduceRequestKey.logger.isDebugEnabled()) {
			KafkaProduceRequestKey.logger.debug("validate KafkaProduceRequestKey...");
		}

		boolean result = true;

		if (serializer == null) {
			KafkaProduceRequestKey.logger.error("serializer can't be null.");
			result = false;
		}

		if (serializer == SerializerTypeEnum.UNKNOWN) {
			KafkaProduceRequestKey.logger
					.error(String.format("can't use serializer '%s'.", SerializerTypeEnum.UNKNOWN));
			result = false;
		}

		try {
			Class.forName(serializer.getValue());
		} catch (Exception ex) {
			KafkaProduceRequestKey.logger
					.error(String.format("can't find serializer '%s' on the classpath.", serializer));
			result = false;
		}
 
		if (Convert.isEmpty(value)) {
			KafkaProduceRequestKey.logger.error("value can't be null or empty string.");
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
		return String.format("[%s: %s, %s]", Convert.getClassName(KafkaProduceRequestKey.class),
				UtilsFormat.format("serializer", serializer), UtilsFormat.format("value", value));
	}

}
