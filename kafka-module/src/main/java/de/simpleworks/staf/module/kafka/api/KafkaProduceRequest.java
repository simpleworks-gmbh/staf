package de.simpleworks.staf.module.kafka.api;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public class KafkaProduceRequest implements IKafkaRequest<KafkaProduceRequest> {

	private static final Logger logger = LogManager.getLogger(KafkaProduceRequest.class);

	private String topic;
	private KafkaProduceRequestKey key;
	private KafkaProduceRequestContent content;
	private KafkaProduceRequestHeader[] headers;

	public KafkaProduceRequest() {
		topic = Convert.EMPTY_STRING;
		key = new KafkaProduceRequestKey();
		content = new KafkaProduceRequestContent();
		headers = new KafkaProduceRequestHeader[0];
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public KafkaProduceRequestKey getKey() {
		return key;
	}

	public void setKey(KafkaProduceRequestKey key) {
		this.key = key;
	}

	public KafkaProduceRequestContent getContent() {
		return content;
	}

	public void setContent(KafkaProduceRequestContent content) {
		this.content = content;
	}

	public KafkaProduceRequestHeader[] getHeaders() {
		return headers;
	}

	public void setHeaders(KafkaProduceRequestHeader[] headers) {
		this.headers = headers;
	}

	@Override
	public boolean validate() {
		if (KafkaProduceRequest.logger.isDebugEnabled()) {
			KafkaProduceRequest.logger.debug("validate KafkaProduceRequest...");
		}

		boolean result = true;

		if (Convert.isEmpty(topic)) {
			KafkaProduceRequest.logger.error("topic can't be null or empty string.");
			result = false;
		}

		if (key == null) {
			KafkaProduceRequest.logger.error("key can't be null or empty string.");
			result = false;
		}

		if (!key.validate()) {
			KafkaProduceRequest.logger.error(String.format("key '%s' is invalid.", key));
			result = false;
		}

		if (content == null) { 
			KafkaProduceRequest.logger.error("content can't be null or empty string.");
			result = false;
		}

		if (!content.validate()) {
			KafkaProduceRequest.logger.error(String.format("content '%s' is invalid.", key));
			result = false;
		}

		if (!Convert.isEmpty(headers)) {
			List<KafkaProduceRequestHeader> currentHeaders = Arrays.asList(headers);

			if (currentHeaders.stream().filter(a -> a.validate()).collect(Collectors.toList()).isEmpty()) {
				KafkaProduceRequest.logger.error(String.format("headers are invalid [%s].", String.join(",",
						Arrays.asList(headers).stream().map(a -> a.toString()).collect(Collectors.toList()))));
				result = false;
			}

			for (KafkaProduceRequestHeader header : headers) {
				if (currentHeaders.indexOf(header) != currentHeaders.lastIndexOf(header)) {
					KafkaProduceRequest.logger.error(String
							.format("assertion \"%s\" is used at last two times, which is not supported.", header));
					result = false;
					break;
				}
			}

		}

		return result;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s, %s, %s]", Convert.getClassName(KafkaProduceRequest.class),
				UtilsFormat.format("topic", topic), UtilsFormat.format("key", key),
				UtilsFormat.format("content", content), UtilsFormat.format("headers", String.join(",",
						Arrays.asList(headers).stream().map(a -> a.toString()).collect(Collectors.toList())) ) );
	}

	@Override
	public Class<KafkaProduceRequest> getType() {
		return KafkaProduceRequest.class;
	}
}
