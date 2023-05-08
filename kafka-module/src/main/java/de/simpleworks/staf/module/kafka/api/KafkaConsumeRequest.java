package de.simpleworks.staf.module.kafka.api;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;
import de.simpleworks.staf.module.kafka.enums.DeserializerTypeEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class KafkaConsumeRequest implements IKafkaRequest<KafkaConsumeRequest> {

    private static final Logger logger = LogManager.getLogger(KafkaConsumeRequest.class);

    private String topic;
    private KafkaConsumeRequestKey key;
    private KafkaProduceRequestHeader[] headers;
    private KafkaConsumeRequestTimestamp timestamp;
    private KafkaConsumeRequestContent content;

    public KafkaConsumeRequest() {
        topic = Convert.EMPTY_STRING;
        headers = new KafkaProduceRequestHeader[0];
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

    public KafkaConsumeRequestTimestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(KafkaConsumeRequestTimestamp timestamp) {
        this.timestamp = timestamp;
    }

    public KafkaConsumeRequestContent getContent() {
        return content;
    }

    public void setContent(KafkaConsumeRequestContent content) {
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

        if (key == null && timestamp == null && content == null) {
            KafkaConsumeRequest.logger.error("key as well as timestamp and content are null.");
            result = false;
        }

        if (key != null) {
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

        }

        if (timestamp != null) {
            if (!timestamp.validate()) {
                KafkaConsumeRequest.logger.error(String.format("timestamp '%s' is invalid.", timestamp));
                result = false;
            }
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

        if (content != null) {
            if (!content.validate()) {
                KafkaConsumeRequest.logger.error(String.format("content '%s' is invalid.", timestamp));
                result = false;
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return String.format("[%s: %s, %s, %s, %s, %s]",
                Convert.getClassName(KafkaProduceRequestKey.class),
                UtilsFormat.format("topic", topic),
                UtilsFormat.format("key", key),
                UtilsFormat.format("headers",
                        String.join(",",
                                Arrays.asList(headers).stream().map(a -> a.toString()).collect(Collectors.toList()))),
                UtilsFormat.format("timestamp", timestamp),
                UtilsFormat.format("content", content));

    }

    @Override
    public Class<KafkaConsumeRequest> getType() {
        return KafkaConsumeRequest.class;
    }

}
