package de.simpleworks.staf.module.kafka.api;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;
import de.simpleworks.staf.module.kafka.enums.DeserializerTypeEnum;

public class KafkaConsumeRequest implements IKafkaRequest<KafkaConsumeRequest> {

	private static final Logger logger = LogManager.getLogger(KafkaConsumeRequest.class);

	private String topic;
	private KafkaConsumeRequestKey key;
	private KafkaProduceRequestHeader[] headers;
	private DeserializerTypeEnum content;

	public KafkaConsumeRequest() {
		topic = Convert.EMPTY_STRING;
		key = new KafkaConsumeRequestKey();
		headers = new KafkaProduceRequestHeader[0];
		content = DeserializerTypeEnum.UNKNOWN;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public KafkaConsumeRequestKey getKey() {
		return key;
	}

	public void setKey(KafkaConsumeRequestKey key) {
		this.key = key;
	}

	public KafkaProduceRequestHeader[] getHeaders() {
		return headers;
	}

	public void setHeaders(KafkaProduceRequestHeader[] headers) {
		this.headers = headers;
	}

	public DeserializerTypeEnum getContent() {
		return content;
	}

	public void setContent(DeserializerTypeEnum content) {
		this.content = content;
	}

	@Override
	public boolean validate() {
		if (KafkaConsumeRequest.logger.isDebugEnabled()) {
			KafkaConsumeRequest.logger.debug("validate KafkaProduceRequest...");
		}

		boolean result = true;

		if (Convert.isEmpty(topic)) {
			KafkaConsumeRequest.logger.error("topic can't be null or empty string.");
			result = false;
		}

		if (key == null) {
			KafkaConsumeRequest.logger.error("key can't be null or empty string.");
			result = false;
		}

		if (!key.validate()) {
			KafkaConsumeRequest.logger.error(String.format("key '%s' is invalid.", key));
			result = false;
		}

		final DeserializerTypeEnum deserializer = key.getDeserializer();

		try {
			Class.forName(deserializer.getValue());
		} catch (Exception ex) {
			KafkaConsumeRequest.logger
					.error(String.format("can't find deserializer '%s' on the classpath.", deserializer.getValue()));
			result = false;
		}

		if (!Convert.isEmpty(headers)) {
			List<KafkaProduceRequestHeader> currentHeaders = Arrays.asList(headers);

			if (currentHeaders.stream().filter(a -> a.validate()).collect(Collectors.toList()).isEmpty()) {
				KafkaConsumeRequest.logger.error(String.format("headers are invalid [%s].", String.join(",",
						Arrays.asList(headers).stream().map(a -> a.toString()).collect(Collectors.toList()))));
				result = false;
			}

			for (KafkaProduceRequestHeader header : headers) {
				if (currentHeaders.indexOf(header) != currentHeaders.lastIndexOf(header)) {
					KafkaConsumeRequest.logger.error(String
							.format("assertion \"%s\" is used at last two times, which is not supported.", header));
					result = false;
					break;
				}
			}

		}
		if (content == null) {
			KafkaConsumeRequest.logger.error("content can't be null or empty string.");
			result = false;
		}

		if (content == DeserializerTypeEnum.UNKNOWN) {
			KafkaConsumeRequest.logger
					.error(String.format("can't use deserializer '%s'.", DeserializerTypeEnum.UNKNOWN));
			result = false;
		}

		try {
			Class.forName(content.getValue());
		} catch (Exception ex) {
			KafkaConsumeRequest.logger
					.error(String.format("can't find deserializer '%s' on the classpath.", content.getValue()));
			result = false;
		}

		return result;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s, %s, %s]", Convert.getClassName(KafkaProduceRequestKey.class),
				UtilsFormat.format("topic", topic), UtilsFormat.format("key", key),
				UtilsFormat.format("headers",
						String.join(",",
								Arrays.asList(headers).stream().map(a -> a.toString()).collect(Collectors.toList()))),
				UtilsFormat.format("content", content));
	}

	@Override
	public Class<KafkaConsumeRequest> getType() {
		return KafkaConsumeRequest.class;
	}

}
