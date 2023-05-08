package de.simpleworks.staf.module.kafka.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.simpleworks.staf.commons.enums.AllowedValueEnum;
import de.simpleworks.staf.commons.enums.ValidateMethodEnum;
import de.simpleworks.staf.commons.interfaces.IPojo;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;
import de.simpleworks.staf.module.kafka.enums.DeserializerTypeEnum;

public class KafkaConsumeRequestContent implements IPojo {

    private static final Logger logger = LogManager.getLogger(KafkaConsumeRequestContent.class);

    private String jsonpath;
    private AllowedValueEnum allowedValue;
    private ValidateMethodEnum validateMethod;
    private String value;
    private DeserializerTypeEnum deserializer;

    public KafkaConsumeRequestContent() {
        deserializer = DeserializerTypeEnum.UNKNOWN;
        value = Convert.EMPTY_STRING;
        jsonpath = Convert.EMPTY_STRING;
        allowedValue = AllowedValueEnum.NON_EMPTY;
        validateMethod = ValidateMethodEnum.UNKNOWN;
    }

    public String getJsonpath() {
        return jsonpath;
    }

    public void setJsonpath(String jsonpath) {
        this.jsonpath = jsonpath;
    }

    public AllowedValueEnum getAllowedValue() {
        return allowedValue;
    }

    public void setAllowedValue(AllowedValueEnum allowedValue) {
        this.allowedValue = allowedValue;
    }

    public ValidateMethodEnum getValidateMethod() {
        return validateMethod;
    }

    public void setValidateMethod(ValidateMethodEnum validateMethod) {
        this.validateMethod = validateMethod;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public DeserializerTypeEnum getDeserializer() {
        return deserializer;
    }

    public void setDeserializer(DeserializerTypeEnum deserializer) {
        this.deserializer = deserializer;
    }

    @Override
    public boolean validate() {
        if (KafkaConsumeRequestContent.logger.isTraceEnabled()) {
            KafkaConsumeRequestContent.logger.trace("validate KafkaConsumeRequestContent...");
        }

        boolean result = true;

        if (deserializer == null) {
            KafkaConsumeRequestContent.logger.error("deserializer can't be null.");
            result = false;
        }

        if (deserializer == DeserializerTypeEnum.UNKNOWN) {
            KafkaConsumeRequestContent.logger
                    .error(String.format("can't use deserializer '%s'.", DeserializerTypeEnum.UNKNOWN));
            result = false;
        }

        try {
            Class.forName(deserializer.getValue());
        } catch (Exception ex) {
            KafkaConsumeRequestContent.logger
                    .error(String.format("can't find deserializer '%s' on the classpath.", deserializer));
            result = false;
        }

        if (validateMethod == null) {
            KafkaConsumeRequestContent.logger.error("validateMethod can't be null.");
            result = false;
        } else {
            switch (validateMethod) {
                case UNKNOWN:
                    result = true;
                    break;
                case JSONPATH:
                    if (Convert.isEmpty(jsonpath)) {
                        KafkaConsumeRequestContent.logger.error("jsonpath can't be null or empty string.");
                        result = false;
                    }
                    break;
                default:
                    KafkaConsumeRequestContent.logger
                            .error(String.format("validateMethod '%s' not implemented yet.", validateMethod.getValue()));
                    result = false;
                    break;
            }

        }

        if (allowedValue == null) {
            KafkaConsumeRequestContent.logger.error("allowedValue can't be null.");
            result = false;
        }

        return result;
    }

    @Override
    public String toString() {
        return String.format("[%s: %s, %s, %s, %s, %s]",
                Convert.getClassName(KafkaConsumeRequestContent.class),
                UtilsFormat.format("jsonpath", jsonpath),
                UtilsFormat.format("allowedValue", allowedValue),
                UtilsFormat.format("validateMethod", validateMethod),
                UtilsFormat.format("value", value),
                UtilsFormat.format("deserializer", deserializer));
    }

}