package de.simpleworks.staf.module.kafka.api.kafkaclient.utils;

import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.module.kafka.api.KafkaProduceRequestContent;
import de.simpleworks.staf.module.kafka.api.KafkaProduceRequestHeader;
import de.simpleworks.staf.module.kafka.api.KafkaProduceRequestKey;

public class KafkaClientUtils {

	private static final Logger logger = LogManager.getLogger(KafkaClientUtils.class);

	public static Object transformKey(KafkaProduceRequestKey key) {

		if (key == null) {
			throw new IllegalArgumentException("key can't be null.");
		}

		if (!key.validate()) {
			throw new IllegalArgumentException(String.format("key '%s' is invalid.", key));
		}

		Object result = null;

		if (KafkaClientUtils.logger.isInfoEnabled()) {
			KafkaClientUtils.logger.info(String.format("start transforming key '%s' ...", key));
		}

		switch (key.getSerializer()) {

		case STRING_SERIALIZER:
			result = key.getValue();
			break;

		case LONG_SERIALIZER:
			result = Long.parseLong(key.getValue());
			break;
		default:
			throw new IllegalArgumentException(
					String.format("Serializer '%s' not implemented yet.", key.getSerializer()));
		}

		if (KafkaClientUtils.logger.isInfoEnabled()) {
			KafkaClientUtils.logger.info(String.format("end transforming key '%s' to '%s'.", key, result));
		}

		return result;
	}

	public static Object transformContent(KafkaProduceRequestContent content) {

		if (content == null) {
			throw new IllegalArgumentException("content can't be null.");
		}

		if (!content.validate()) {
			throw new IllegalArgumentException(String.format("content '%s' is invalid.", content));
		}

		Object result = null;

		if (KafkaClientUtils.logger.isInfoEnabled()) {
			KafkaClientUtils.logger.info(String.format("start transforming content '%s' ...", content));
		}

		switch (content.getSerializer()) {

		case STRING_SERIALIZER:
			result = content.getContent();
			break;
		default:
			throw new IllegalArgumentException(
					String.format("Serializer '%s' not implemented yet.", content.getSerializer()));
		}

		if (KafkaClientUtils.logger.isInfoEnabled()) {
			KafkaClientUtils.logger.info(String.format("end transforming key '%s' to '%s'.", content, result));
		}

		return result;
	}

	public static Header transformHeader(KafkaProduceRequestHeader header) {

		if (header == null) {
			throw new IllegalArgumentException("header can't be null.");
		}

		if (!header.validate()) {
			throw new IllegalArgumentException(String.format("header '%s' is invalid.", header));
		}

		Header result = new RecordHeader(header.getKey(), header.getValue().getBytes());

		return result;
	}
}
