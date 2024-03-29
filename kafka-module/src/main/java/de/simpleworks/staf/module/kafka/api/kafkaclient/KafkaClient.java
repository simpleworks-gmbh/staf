package de.simpleworks.staf.module.kafka.api.kafkaclient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.module.kafka.api.KafkaConsumeRecord;
import de.simpleworks.staf.module.kafka.api.KafkaConsumeRequest;
import de.simpleworks.staf.module.kafka.api.KafkaConsumeRequestKey;
import de.simpleworks.staf.module.kafka.api.KafkaConsumeResponse;
import de.simpleworks.staf.module.kafka.api.KafkaProduceRequest;
import de.simpleworks.staf.module.kafka.api.KafkaProduceRequestContent;
import de.simpleworks.staf.module.kafka.api.KafkaProduceRequestHeader;
import de.simpleworks.staf.module.kafka.api.KafkaProduceRequestKey;
import de.simpleworks.staf.module.kafka.api.KafkaProduceResponse;
import de.simpleworks.staf.module.kafka.api.kafkaclient.utils.KafkaClientUtils;
import de.simpleworks.staf.module.kafka.api.properties.KafkaProperties;
import de.simpleworks.staf.module.kafka.enums.ConumeMessagesDirectionEnum;
import de.simpleworks.staf.module.kafka.enums.DeserializerTypeEnum;
import de.simpleworks.staf.module.kafka.enums.SerializerTypeEnum;
import de.simpleworks.staf.module.kafka.util.UtilsKafkaConsumer;
public class KafkaClient {
    private static final Logger logger = LogManager.getLogger(KafkaClient.class);
    private static final KafkaProperties kafkaProperties = KafkaProperties.getInstance();
    @SuppressWarnings("rawtypes")
    private static Consumer createConsumer(final String bootstrapServer, final String groupId,
            final DeserializerTypeEnum keyDeserializerClass, final DeserializerTypeEnum valueDeserializerClass) {
        if (Convert.isEmpty(bootstrapServer)) {
            throw new IllegalArgumentException("bootstrapServer can't be null or empty string.");
        }
        if (Convert.isEmpty(groupId)) {
            throw new IllegalArgumentException("groupId can't be null or empty string.");
        }
        if (keyDeserializerClass == null) {
            throw new IllegalArgumentException("keyDeserializerClass can't be null.");
        }
        if (valueDeserializerClass == null) {
            throw new IllegalArgumentException("valueDeserializerClass can't be null.");
        }
        final Properties props = System.getProperties();
        if (props.getOrDefault(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, null) == null) {
            throw new RuntimeException(
                    String.format("PROPERTY: '%s' was not set.", ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        }
        if (props.getOrDefault(ConsumerConfig.GROUP_ID_CONFIG, null) == null) {
            throw new RuntimeException(String.format("PROPERTY: '%s' was not set.", ConsumerConfig.GROUP_ID_CONFIG));
        }
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializerClass.getValue());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializerClass.getValue());
        // Create the consumer using props.
        final Consumer<Long, String> result = new KafkaConsumer<>(props);
        return result;
    }
    @SuppressWarnings("rawtypes")
    private static Producer createProducer(final String bootstrapServer, final String clientId,
            final SerializerTypeEnum keySerializerClass, final SerializerTypeEnum valueSerializerClass) {
        if (Convert.isEmpty(bootstrapServer)) {
            throw new IllegalArgumentException("bootstrapServer can't be null or empty string.");
        }
        if (Convert.isEmpty(clientId)) {
            throw new IllegalArgumentException("clientId can't be null or empty string.");
        }
        if (keySerializerClass == null) {
            throw new IllegalArgumentException("keySerializerClass can't be null or empty string.");
        }
        if (valueSerializerClass == null) {
            throw new IllegalArgumentException("valueSerializerClass can't be null or empty string.");
        }
        Properties props = System.getProperties();
        if (props.getOrDefault(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, null) == null) {
            throw new RuntimeException(
                    String.format("PROPERTY: '%s' was not set.", ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        }
        props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializerClass.getValue());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializerClass.getValue());
        final KafkaProducer result = new KafkaProducer<>(props);
        return result;
    }
    public static KafkaConsumeResponse consumeMessage(final KafkaConsumeRequest request) throws SystemException {
        if (request == null) {
            throw new IllegalArgumentException("request can't be null.");
        }
        if (!request.validate()) {
            throw new IllegalArgumentException(String.format("request '%s' is invalid.", request));
        }
        final KafkaConsumeRequestKey key = request.getKey();
        final DeserializerTypeEnum keyDeserializer = key.getDeserializer();
        final DeserializerTypeEnum contentDeserializer = request.getContent();
        @SuppressWarnings("rawtypes")
        Consumer consumer = createConsumer(kafkaProperties.getBootstrapServers(), kafkaProperties.getConsumerGroupId(),
                keyDeserializer, contentDeserializer);
        final String topic = request.getTopic();
        final String keyValue = key.getValue();
        final KafkaConsumeResponse result = getMessages(consumer, topic, keyValue);
        
        return result;
    }
    public static KafkaProduceResponse produceMessage(final KafkaProduceRequest request) throws SystemException {
        if (request == null) {
            throw new IllegalArgumentException("request can't be null.");
        }
        if (!request.validate()) {
            throw new IllegalArgumentException(String.format("request '%s' is invalid.", request));
        }
        final String topic = request.getTopic();
        // fix me
        final Integer partition = null;
        final KafkaProduceRequestKey key = request.getKey();
        final Object transformedKey = KafkaClientUtils.transformKey(key);
        final KafkaProduceRequestContent content = request.getContent();
        final Object transformedContent = KafkaClientUtils.transformContent(content);
        List<Header> headers = new ArrayList<Header>();
        for (KafkaProduceRequestHeader header : request.getHeaders()) {
            final Header transformedHeader = KafkaClientUtils.transformHeader(header);
            headers.add(transformedHeader);
        }
        final SerializerTypeEnum keySerializer = key.getSerializer();
        final SerializerTypeEnum contentSerializer = request.getContent().getSerializer();
        @SuppressWarnings("rawtypes")
        Producer producer = createProducer(kafkaProperties.getBootstrapServers(),
                String.format("%s-%s", kafkaProperties.getConsumerGroupId(), UUID.randomUUID().toString()),
                keySerializer, contentSerializer);
        if (KafkaClient.logger.isInfoEnabled()) {
            KafkaClient.logger.info("produce record...");
            KafkaClient.logger.info(String.format("topic: '%s'", topic));
            KafkaClient.logger.info(String.format("key: '%s'", transformedKey));
            KafkaClient.logger.info(String.format("content: '%s'", transformedContent));
            KafkaClient.logger.info(String.format("headers: '%s'",
                    String.join(",", headers.stream().map(header -> header.key()).collect(Collectors.toList()))));
        }
        @SuppressWarnings({ "unchecked", "rawtypes" })
        ProducerRecord record = new ProducerRecord(request.getTopic(), partition, transformedKey, transformedContent,
                headers);
        @SuppressWarnings({ "unchecked", "rawtypes" })
        // implement me, put a callback here
        Future future = producer.send(record);
        producer.close();
        KafkaProduceResponse result = null;
        RecordMetadata metadata = null;
        try {
            Object ob = future.get();
            if (!(ob instanceof RecordMetadata)) {
                throw new Exception(String.format("can't cast to an Object of type '%s'.", RecordMetadata.class));
            }
            metadata = (RecordMetadata) future.get();
            result = new KafkaProduceResponse();
            result.setTopic(metadata.topic());
            result.setPartition(metadata.partition());
            if (metadata.hasTimestamp()) {
                result.setTimestamp(metadata.timestamp());
            }
            if (metadata.hasOffset()) {
                result.setOffset(metadata.offset());
            }
        } catch (Exception ex) {
            final String msg = String.format("can't fetch RecordMetadata for request: '%S'.", request);
            KafkaClient.logger.error(msg, ex);
            throw new SystemException(msg);
        }
        return result;
    }
    @SuppressWarnings({ "rawtypes", "unchecked" })
    /**
     * 
     * @brief gets messages from Kafka-Broker. The consumer instance will (likely) be closed afterwards.
     * @return respecting instance of KafkaConsumeResponse, null if an error occurs.
     */
    private static KafkaConsumeResponse getMessages(Consumer consumer, String topic, String key) {
        if (consumer == null) {
            throw new IllegalArgumentException("consumer can't be null.");
        }
        if (Convert.isEmpty(topic)) {
            throw new IllegalArgumentException("topic can't be null or empty string.");
        }
        if (Convert.isEmpty(key)) {
            throw new IllegalArgumentException("key can't be null or empty string.");
        }
        List<PartitionInfo> partitions = new ArrayList<>();
        try {
            if (KafkaClient.logger.isInfoEnabled()) {
                KafkaClient.logger.info(String.format("fetch partitions for '%s'.", topic));
            }
            partitions = consumer.partitionsFor(topic);
            if (KafkaClient.logger.isDebugEnabled()) {
                KafkaClient.logger.debug(String.format("fetched '%s' partitions for topic '%s' ['%s'].",
                        partitions.size(), topic, String.join(",", partitions.stream()
                                .map(p -> Integer.toString(p.partition())).collect(Collectors.toList()))));
            }
        } catch (Exception ex) {
            final String msg = String.format("no partitions have been fetched for '%s'.", topic);
            KafkaClient.logger.error(msg, ex);
            return null;
        }
        final List<KafkaConsumeRecord> fetchedRecords = new ArrayList<>();
        try {
            for (PartitionInfo partition : partitions) {
                final TopicPartition tp = new TopicPartition(topic, partition.partition());
                final List<TopicPartition> topicPartition = Arrays.asList(tp);
                try {
                    if (KafkaClient.logger.isInfoEnabled()) {
                        KafkaClient.logger.info(String.format("assign to '%s'.", topicPartition));
                    }
                    consumer.assign(topicPartition);
                } catch (Exception ex) {
                    final String msg = String.format("can't assign customer to topics '%s'.", String.join(",",
                            topicPartition.stream().map(t -> t.toString()).collect(Collectors.toList())));
                    KafkaClient.logger.error(msg, ex);
                    consumer.unsubscribe();
                    continue;
                }
                long totalLengthofPartition = -1;
                long startOffset = -1;
                try {
                    // seek to end of the topic
                    consumer.seekToEnd(topicPartition);
                    // the position is the latest offset
                    totalLengthofPartition = consumer.position(tp);
                    if (totalLengthofPartition < 1) {
                        throw new RuntimeException(
                                String.format("totalLengthofPartition can't be zero or negative, but was \"%s\".",
                                        totalLengthofPartition));
                    }
                    // seek to end of the topic
                    consumer.seekToBeginning(topicPartition);
                    // the position is the smallest offset
                    startOffset = consumer.position(tp);
                    if (startOffset < 0) {
                        throw new RuntimeException(
                                String.format("startOffset can't be less than zero, but was \"%s\".", startOffset));
                    }
                } catch (Exception ex) {
                    final String msg = String.format("can't assign customer to topics '%s'.", String.join(",",
                            topicPartition.stream().map(t -> t.toString()).collect(Collectors.toList())));
                    KafkaClient.logger.error(msg, ex);
                    consumer.unsubscribe();
                    continue;
                }
                final ConumeMessagesDirectionEnum direction = kafkaProperties.getConsumerConsumeMessagesDirection();
                
                switch (direction) {
                case ASCENDING:
                    if (!fetchedRecords.addAll(UtilsKafkaConsumer.consumeMessagesAscendingOrder(consumer, partition, tp,
                            key, startOffset, totalLengthofPartition))) {
                        KafkaClient.logger.error(String.format(
                                "can't add new records of partition '%s' from topic '%s' in asecneding order",
                                Integer.toString(partition.partition()), tp.topic()));
                    }
                    break;
                case DESCENDING:
                    if (!fetchedRecords.addAll(UtilsKafkaConsumer.consumeMessagesDescendingOrder(consumer, partition,
                            tp, key, totalLengthofPartition, startOffset))) {
                        KafkaClient.logger.error(String.format(
                                "can't add new records of partition '%s' from topic '%s' in desecending order",
                                Integer.toString(partition.partition()), tp.topic()));
                    }
                    break;
                default:
                    throw new IllegalArgumentException(String.format("type '%s' is not implemented yet.", direction));
                }
                if (KafkaClient.logger.isInfoEnabled()) {
                    KafkaClient.logger.info(String
                            .format("start polling for messages from topic '%s' at partition '%s'.", topic, partition));
                }
                consumer.unsubscribe();
            }
        } catch (Exception ex) {
            final String msg = String.format("can't fetch messages from Kafka at '%s'.",
                    kafkaProperties.getBootstrapServers());
            KafkaClient.logger.error(msg, ex);
            return null;
        }
        try {
            consumer.close();
        } catch (Exception ex) {
            KafkaClient.logger.error("can't close consumer.");
        }
        final KafkaConsumeResponse result = new KafkaConsumeResponse();
        result.setRecords(UtilsCollection.toArray(KafkaConsumeRecord.class, fetchedRecords));
        return result;
    }
}