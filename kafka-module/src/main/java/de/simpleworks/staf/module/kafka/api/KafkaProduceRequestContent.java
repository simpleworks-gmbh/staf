package de.simpleworks.staf.module.kafka.api;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.interfaces.IPojo;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;
import de.simpleworks.staf.commons.utils.UtilsIO;
import de.simpleworks.staf.module.kafka.enums.SerializerTypeEnum;

public class KafkaProduceRequestContent implements IPojo {

	private static final Logger logger = LogManager.getLogger(KafkaProduceRequestContent.class);

	private SerializerTypeEnum serializer;

	private String value;
	private String contentFile;

	public KafkaProduceRequestContent() {
		serializer = SerializerTypeEnum.UNKNOWN;
		value = Convert.EMPTY_STRING;
		contentFile = Convert.EMPTY_STRING;
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

	public String getContentFile() {
		return contentFile;
	}

	public void setContentFile(String contentFile) {
		this.contentFile = contentFile;
	}

	@Override
	public boolean validate() {
		if (KafkaProduceRequestContent.logger.isDebugEnabled()) {
			KafkaProduceRequestContent.logger.debug("validate KafkaProduceRequestContent...");
		}

		boolean result = true;

		if (serializer == null) {
			KafkaProduceRequestContent.logger.error("serializer can't be null.");
			result = false;
		}

		if (serializer == SerializerTypeEnum.UNKNOWN) {
			KafkaProduceRequestContent.logger
					.error(String.format("can't use serializer '%s'.", SerializerTypeEnum.UNKNOWN));
			result = false;
		}

		try {
			Class.forName(serializer.getValue());
		} catch (Exception ex) {
			KafkaProduceRequestContent.logger
					.error(String.format("can't find serializer '%s' on the classpath.", serializer));
			result = false;
		}

		if (Convert.isEmpty(value) && Convert.isEmpty(contentFile)) {
			KafkaProduceRequestContent.logger.error(
					"content and contentFile can't be null or empty string, at the same time, one needs to b set.");
			result = false;
		}

		else if ((!Convert.isEmpty(value)) && (!Convert.isEmpty(contentFile))) {
			KafkaProduceRequestContent.logger.error(
					"content and contentFile are both null or empty string, at the same time, one needs to b set.");
			result = false;
		}

		if (!Convert.isEmpty(contentFile)) {
			File file = new File(contentFile);

			if (!file.exists()) {
				KafkaProduceRequestContent.logger.error(String.format("the file at '%s' does not exist.", contentFile));
				result = false;
			}
		}

		/*
		 * if (Convert.isEmpty(content) && Convert.isEmpty(contentFile)) {
		 * KafkaProduceRequestContent.logger.error(
		 * "content and contentFile can't be null or empty string, at the same time, one needs to b set."
		 * ); result = false; }
		 * 
		 * else if ((!Convert.isEmpty(content)) && (!Convert.isEmpty(contentFile))) {
		 * KafkaProduceRequestContent.logger.error(
		 * "content and contentFile are both null or empty string, at the same time, one needs to b set."
		 * ); result = false; }
		 * 
		 * if (!Convert.isEmpty(contentFile)) { File file = new File(contentFile);
		 * 
		 * if (!file.exists()) { KafkaProduceRequestContent.logger.error(String.
		 * format("the file at '%s' does not exist.", contentFile)); }
		 * 
		 * result = false; }
		 */

		return result;
	}

	public String getContent() {

		String result = Convert.EMPTY_STRING;

		if (!Convert.isEmpty(value)) {
			result = value;
		}

		else {
			if (!Convert.isEmpty(contentFile)) {
				File file = new File(contentFile);

				if (file.exists()) {
					try {
						result = UtilsIO.getAllContentFromFile(file);
					} catch (SystemException ex) {
						KafkaProduceRequestContent.logger.error("can't fetch content, will return empty string.", ex);
						result = Convert.EMPTY_STRING;
					}
				}
			}
		}

		return result;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s, %s]", Convert.getClassName(KafkaProduceRequestContent.class),
				UtilsFormat.format("serializer", serializer), UtilsFormat.format("value", value),
				UtilsFormat.format("contentFile", contentFile));
	}

}
