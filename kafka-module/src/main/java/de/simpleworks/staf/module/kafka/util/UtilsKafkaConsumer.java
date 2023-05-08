package de.simpleworks.staf.module.kafka.util;

import de.simpleworks.staf.commons.enums.AllowedValueEnum;
import de.simpleworks.staf.commons.enums.ValidateMethodEnum;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.commons.utils.UtilsDate;
import de.simpleworks.staf.framework.util.assertion.JSONPATHAssertionValidator;
import de.simpleworks.staf.module.kafka.api.KafkaConsumeRecord;
import de.simpleworks.staf.module.kafka.api.KafkaConsumeRequestContent;
import de.simpleworks.staf.module.kafka.api.KafkaConsumeRequestTimestamp;
import de.simpleworks.staf.module.kafka.api.KafkaProduceRequestHeader;
import de.simpleworks.staf.module.kafka.api.properties.KafkaProperties;
import de.simpleworks.staf.module.kafka.enums.TimestampAllowedValueEnum;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class UtilsKafkaConsumer {

    private static final Logger logger = LogManager.getLogger(UtilsKafkaConsumer.class);

    private static final KafkaProperties kafkaProperties = KafkaProperties.getInstance();

    public static List<KafkaConsumeRecord> consumeMessagesAscendingOrder(
            @SuppressWarnings("rawtypes") Consumer consumer, final KafkaConsumeRequestTimestamp consumedRequestTimestamp,
            final KafkaConsumeRequestContent content, PartitionInfo partition, TopicPartition tp, String key, long startOffset, long endOffset) {

        if (consumer == null) {
            throw new IllegalArgumentException("consumer can't be null.");
        }

        if (consumedRequestTimestamp != null) {
            if (!consumedRequestTimestamp.validate()) {
                throw new IllegalArgumentException(
                        String.format("consumedRequestTimestamp is invalid '%s'.", consumedRequestTimestamp));
            }
        }

        if (partition == null) {
            throw new IllegalArgumentException("partition can't be null.");
        }

        if (tp == null) {
            throw new IllegalArgumentException("tp can't be null.");
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

            List<KafkaConsumeRecord> consumedMessages = consumeMessages(new Consumer[]{consumer}, partition, tp, key,
                    consumedRequestTimestamp, content, currentOffset, maxMessages);

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
            @SuppressWarnings("rawtypes") Consumer consumer, final KafkaConsumeRequestTimestamp consumedRequestTimestamp,
            final KafkaConsumeRequestContent content, PartitionInfo partition, TopicPartition tp, String key, long startOffset, long endOffset) {

        if (consumer == null) {
            throw new IllegalArgumentException("consumer can't be null.");
        }

        if (consumedRequestTimestamp != null) {
            if (!consumedRequestTimestamp.validate()) {
                throw new IllegalArgumentException(
                        String.format("consumedRequestTimestamp is invalid '%s'.", consumedRequestTimestamp));
            }
        }

        if (partition == null) {
            throw new IllegalArgumentException("partition can't be null.");
        }

        if (tp == null) {
            throw new IllegalArgumentException("tp can't be null.");
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

        for (long currentOffset = startOffset; ((currentOffset >= Math.max((startOffset - maxOffset), 0))
                && (results.size() <= maxMessages)); currentOffset -= 1) {

            List<KafkaConsumeRecord> consumedMessages = consumeMessages(new Consumer[]{consumer}, partition, tp, key,
                    consumedRequestTimestamp, content, currentOffset, maxMessages);

            if (results.addAll(consumedMessages)) {
                if (UtilsKafkaConsumer.logger.isDebugEnabled()) {
                    UtilsKafkaConsumer.logger
                            .debug(String.format("this messages were consumed [%s].", consumedMessages));
                }
            }
        }

        return results;
    }


    private static boolean shouldKeyBeRecorded(final ConsumerRecord<?, ?> record, final String key) {
        boolean result = false;

        if (!Convert.isEmpty(key)) {
            result = key.equals(record.key());
        }

        return result;
    }

    private static boolean shouldTimestampBeRecorded(final ConsumerRecord<?, ?> record,  final KafkaConsumeRequestTimestamp recordTimestamp) {
        boolean result = false;

        try {

            final String timeZoneValue = recordTimestamp.getTimezone();
            final TimeZone timeZone = TimeZone.getTimeZone(timeZoneValue);

            final Calendar expectedDateCalendar = Calendar.getInstance();
            expectedDateCalendar.setTimeZone(timeZone);

            final String format = recordTimestamp.getFormat();
            expectedDateCalendar.setTime((new SimpleDateFormat(format)).parse(recordTimestamp.getValue()));

            final Calendar recordDateCalendar = Calendar.getInstance();
            recordDateCalendar.setTimeZone(timeZone);
            recordDateCalendar.setTimeInMillis(record.timestamp());

            final TimestampAllowedValueEnum allowedValue = recordTimestamp.getAllowedValue();

            switch (allowedValue) {

                case EXACT_TIME:
                    result = UtilsDate.sameDate(expectedDateCalendar, recordDateCalendar);
                    break;

                case BEFORE_TIME:
                    result = UtilsDate.before(expectedDateCalendar, recordDateCalendar);
                    break;

                case AFTER_TIME:
                    result = UtilsDate.after(expectedDateCalendar, recordDateCalendar);
                    break;

                default:
                    throw new IllegalArgumentException(
                            String.format("allowedValue '%s' is not implemented yet.", allowedValue.getValue()));
            }
        } catch (Exception ex) {
            UtilsKafkaConsumer.logger.error("can't filter timestamp, will return false.", ex);
            result = false;
        }

        return result;
    }

    private static boolean shouldContentBeRecorded(final ConsumerRecord<?, ?> record, final KafkaConsumeRequestContent content) {
        boolean result = false;

        final ValidateMethodEnum validateMethodEnum = content.getValidateMethod();

        switch (validateMethodEnum) {

            case JSONPATH:
                final AllowedValueEnum allowedValueEnum = content.getAllowedValue();

                //FIXME: cast to specific type
                final String jsonBody = (String) record.value();

                final String extractedValue = JSONPATHAssertionValidator.executeJsonPath(jsonBody, content.getJsonpath());

                switch (allowedValueEnum) {

                    case CONTAINS_VALUE:
                        if (extractedValue.contains(content.getValue())) {
                            result = true;
                        }

                        break;
                    case EXACT_VALUE:
                        if (content.getValue().equals(extractedValue)) {
                            result = true;
                        }
                        break;

                    default:
                        logger.error(String.format("The allowedValueEnum '%s' is not implemented yet.", allowedValueEnum.getValue()));
                        result = false;
                }

                break;

            default:
                logger.error(String.format("The allowedValueEnum '%s' is not implemented yet.", validateMethodEnum.getValue()));
                result = false;
        }

        return result;
    }


    private static boolean shouldBeRecorded(final ConsumerRecord<?, ?> record, final String key,
                                                  final KafkaConsumeRequestTimestamp recordTimestamp, final KafkaConsumeRequestContent content) {

        boolean result = false;

        if (!Convert.isEmpty(key)) {

            boolean shouldKeyBeRecordedFlag = shouldKeyBeRecorded(record, key);

            if (shouldKeyBeRecordedFlag) {
                if (UtilsKafkaConsumer.logger.isDebugEnabled()) {
                    UtilsKafkaConsumer.logger.debug(String.format("key '%s' was found in the record.", key));
                }
            }

            result = shouldKeyBeRecordedFlag;
        }
       
        if (recordTimestamp != null) {

            boolean shouldTimestampBeRecordedFlag = shouldTimestampBeRecorded(record, recordTimestamp);

            if (shouldTimestampBeRecordedFlag) {
                if (UtilsKafkaConsumer.logger.isDebugEnabled()) {
                    UtilsKafkaConsumer.logger.debug(String.format("timestamp '%s' was found in the record.", recordTimestamp));
                }
            }

            result = (result && shouldTimestampBeRecordedFlag);
        }

        if (content != null) {

            boolean shouldContentBeRecordedFlag = shouldContentBeRecorded(record, content);

            if (shouldContentBeRecordedFlag) {
                if (UtilsKafkaConsumer.logger.isDebugEnabled()) {
                    UtilsKafkaConsumer.logger.debug(String.format("content '%s' was found in the record.", content));
                }
            }

            result = (result && shouldContentBeRecordedFlag);
        }

        return result;
    }

    private static List<KafkaConsumeRecord> consumeMessages(@SuppressWarnings("rawtypes") final Consumer[] consumer,
                                                            final PartitionInfo partition, final TopicPartition tp, final String key, final KafkaConsumeRequestTimestamp consumedTimestamp,
                                                            final KafkaConsumeRequestContent content, final long startOffset, final int maxMessages) {

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

                if (shouldBeRecorded(record, key, consumedTimestamp, content)) {

                    final KafkaConsumeRecord rec = new KafkaConsumeRecord();

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

                }
            }
        }

        return results;
    }
}