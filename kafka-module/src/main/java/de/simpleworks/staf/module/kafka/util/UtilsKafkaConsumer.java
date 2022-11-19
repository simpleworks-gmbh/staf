package de.simpleworks.staf.module.kafka.util;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.module.kafka.api.KafkaConsumeRecord;
import de.simpleworks.staf.module.kafka.api.KafkaProduceRequestHeader;
import de.simpleworks.staf.module.kafka.api.properties.KafkaProperties;

public class UtilsKafkaConsumer {

	private static final Logger logger = LogManager.getLogger(UtilsKafkaConsumer.class);

	private static final KafkaProperties kafkaProperties = KafkaProperties.getInstance();

	public static List<KafkaConsumeRecord> consumeMessagesAscendingOrder(
			@SuppressWarnings("rawtypes") Consumer consumer, PartitionInfo partition, TopicPartition tp, String key,
			long startOffset, long endOffset) {

		if (consumer == null) {
			throw new IllegalArgumentException("consumer can't be null.");
		}

		if (partition == null) {
			throw new IllegalArgumentException("partition can't be null.");
		}

		if (tp == null) {
			throw new IllegalArgumentException("tp can't be null.");
		}

		if (Convert.isEmpty(key)) {
			throw new IllegalArgumentException("key can't be null or empty string.");
		}

		// initial seek
		consumer.seek(tp, startOffset);

		if (UtilsKafkaConsumer.logger.isInfoEnabled()) {
			UtilsKafkaConsumer.logger.info(String.format("start polling messages in ascending order."));
		}

		final int maxMessages = Convert.getNumericValue(kafkaProperties.getConsumerConsumeMessagesMax());

		if (maxMessages == -1) {
			throw new RuntimeException("maxMessages can't be -1.");
		}

		final int maxOffset = Convert.getNumericValue(kafkaProperties.getConsumerConsumeOffsetMax());

		if (maxOffset == -1) {
			throw new RuntimeException("maxOffset can't be -1.");
		}

		final List<KafkaConsumeRecord> results = new ArrayList<>();

		for (long currentOffset = startOffset; (currentOffset <= (currentOffset + maxOffset)
				&& (results.size() <= maxMessages)); currentOffset += 1) {

			List<KafkaConsumeRecord> consumedMessages = consumeMessages(new Consumer[] { consumer }, partition, tp, key,
					currentOffset, maxMessages);

			if (results.addAll(consumedMessages)) {
				if (UtilsKafkaConsumer.logger.isDebugEnabled()) {
					UtilsKafkaConsumer.logger
							.debug(String.format("this messages were consumed [%s].", consumedMessages));
				}
			}
		}

		return results;
	}

	public static List<KafkaConsumeRecord> consumeMessagesDescendingOrder(
			@SuppressWarnings("rawtypes") Consumer consumer, PartitionInfo partition, TopicPartition tp, String key,
			long startOffset, long endOffset) {

		if (consumer == null) {
			throw new IllegalArgumentException("consumer can't be null.");
		}

		if (partition == null) {
			throw new IllegalArgumentException("partition can't be null.");
		}

		if (tp == null) {
			throw new IllegalArgumentException("tp can't be null.");
		}

		if (Convert.isEmpty(key)) {
			throw new IllegalArgumentException("key can't be null or empty string.");
		}

		// initial seek
		consumer.seek(tp, startOffset);

		if (UtilsKafkaConsumer.logger.isInfoEnabled()) {
			UtilsKafkaConsumer.logger.info(String.format("start polling messages in descending order."));
		}

		final int maxMessages = Convert.getNumericValue(kafkaProperties.getConsumerConsumeMessagesMax());

		if (maxMessages == -1) {
			throw new RuntimeException("maxMessages can't be -1.");
		}

		final int maxOffset = Convert.getNumericValue(kafkaProperties.getConsumerConsumeOffsetMax());

		if (maxOffset == -1) {
			throw new RuntimeException("maxOffset can't be -1.");
		}

		final List<KafkaConsumeRecord> results = new ArrayList<>();

		for (long currentOffset = startOffset; ((currentOffset >=  Math.max((startOffset - maxOffset), 0)) 
                && (results.size() <= maxMessages)); currentOffset -= 1) {

			List<KafkaConsumeRecord> consumedMessages = consumeMessages(new Consumer[] { consumer }, partition, tp, key,
					currentOffset, maxMessages);

			if (results.addAll(consumedMessages)) {
				if (UtilsKafkaConsumer.logger.isDebugEnabled()) {
					UtilsKafkaConsumer.logger
							.debug(String.format("this messages were consumed [%s].", consumedMessages));
				}
			}
		}

		return results;
	}

	private static List<KafkaConsumeRecord> consumeMessages(@SuppressWarnings("rawtypes") Consumer[] consumer,
			PartitionInfo partition, TopicPartition tp, String key, long startOffset, int maxMessages) {

		final List<KafkaConsumeRecord> results = new ArrayList<>();

		consumer[0].seek(tp, startOffset);

		if (UtilsKafkaConsumer.logger.isDebugEnabled()) {
			UtilsKafkaConsumer.logger
					.debug(String.format("polling for messages from topic '%s' at partition '%s' on offset '%s'.",
							tp.topic(), partition, Long.toString(startOffset)));
		}

		ConsumerRecords<?, ?> records = consumer[0].poll(Duration.ofMillis(1 * kafkaProperties.getPolltimeoutMs()));

		if (records.count() != 0) {
			for (ConsumerRecord<?, ?> record : records) {
				if (key.equals(record.key())) {
					KafkaConsumeRecord rec = new KafkaConsumeRecord();

					final List<KafkaProduceRequestHeader> kafkaRequestHeader = new ArrayList<>();

					for (Header header : record.headers()) {
						KafkaProduceRequestHeader kafkaProduceRequestHeader = new KafkaProduceRequestHeader();
						kafkaProduceRequestHeader.setKey(header.key());
						kafkaProduceRequestHeader.setValue(new String(header.value(), StandardCharsets.UTF_8));

						kafkaRequestHeader.add(kafkaProduceRequestHeader);
					}

					rec.setHeaders(UtilsCollection.toArray(KafkaProduceRequestHeader.class, kafkaRequestHeader));
					rec.setTimestamp(record.timestamp());
					rec.setContent(record.value());
					rec.setPartition(record.partition());
					rec.setOffset(record.offset());
					rec.setTopic(record.topic());

					if (results.add(rec)) {
						if (UtilsKafkaConsumer.logger.isDebugEnabled()) {
							UtilsKafkaConsumer.logger.debug(rec);
						}

						if (UtilsKafkaConsumer.logger.isInfoEnabled()) {
							UtilsKafkaConsumer.logger.info(String.format("fetched '%s' messages.", results.size()));
						}
					}

					// FIXME: should be irrelevant, needs to be tested
//						if (currentOffset < record.offset()) {
//							currentOffset = record.offset() + increment;
//						}
				}
			}
		}

		return results;

	}
}