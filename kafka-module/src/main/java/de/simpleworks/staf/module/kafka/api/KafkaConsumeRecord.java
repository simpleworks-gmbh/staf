package de.simpleworks.staf.module.kafka.api;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.interfaces.IPojo;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public class KafkaConsumeRecord implements IPojo {

	private static final Logger logger = LogManager.getLogger(KafkaConsumeRecord.class);

	private String topic;
	private int partition;
	private long timestamp;
	private long offset;
	private Object content;
	private KafkaProduceRequestHeader[] headers;

	public KafkaConsumeRecord() {
		topic = Convert.EMPTY_STRING;
		partition = -1;
		timestamp = -1;
		offset = -1;
		content = null;
		headers = new KafkaProduceRequestHeader[0];
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public int getPartition() {
		return partition;
	}

	public void setPartition(int partition) {
		this.partition = partition;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	public Object getContent() {
		return content;
	}

	public void setContent(Object content) {
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

		if (KafkaConsumeRecord.logger.isTraceEnabled()) {
			KafkaConsumeRecord.logger.trace("validate KafkaConsumeRecord...");
		}

		boolean result = true;

		if (Convert.isEmpty(topic)) {
			KafkaConsumeRecord.logger.error("topic can't be null or empty string.");
			result = false;
		}

		if (partition < 0) {
			KafkaConsumeRecord.logger.error(
					String.format("partition can't be less than zero, but was '%s'.", Integer.toString(partition)));
			result = false;
		}

		if (content == null) {
			KafkaConsumeRecord.logger.error("content can't be null.");
			result = false;
		}

		return result;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s, %s, %s, %s]", Convert.getClassName(KafkaConsumeRecord.class),
				UtilsFormat.format("topic", topic), UtilsFormat.format("partition", Integer.toString(partition)),
				UtilsFormat.format("offset", Long.toString(offset)), UtilsFormat.format("content", content),
				UtilsFormat.format("headers", String.join(",",
						Arrays.asList(headers).stream().map(a -> a.toString()).collect(Collectors.toList()))));
	}

}