package de.simpleworks.staf.module.kafka.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public class KafkaProduceResponse implements IKafkaResponse<KafkaProduceResponse> {

	private static final Logger logger = LogManager.getLogger(KafkaProduceRequest.class);

	private String topic;
	private int partition;
	private long timestamp;
	private long offset;

	public KafkaProduceResponse() {
		topic = Convert.EMPTY_STRING;
		partition = -1;
		timestamp = -1;
		offset = -1;
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

	@Override
	public boolean validate() {
		if (KafkaProduceResponse.logger.isDebugEnabled()) {
			KafkaProduceResponse.logger.debug("validate KafkaProduceRequestKey...");
		}

		boolean result = true;

		if (Convert.isEmpty(topic)) {
			KafkaProduceResponse.logger.error("topic can't be null or empty string.");
			result = false;
		}

		if (partition < 0) {
			KafkaProduceResponse.logger.error(
					String.format("partition can't be less than zero, but was '%s'.", Integer.toString(partition)));
			result = false;
		}

		return result;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s, %s, %s]", Convert.getClassName(KafkaProduceRequestKey.class),
				UtilsFormat.format("topic", topic), UtilsFormat.format("partition", Integer.toString(partition)),
				UtilsFormat.format("timestamp", Long.toString(timestamp)),
				UtilsFormat.format("offset", Long.toString(offset))

		);
	}
	
	@Override
	public Class<KafkaProduceResponse> getType() {
		return KafkaProduceResponse.class;
	}

}
