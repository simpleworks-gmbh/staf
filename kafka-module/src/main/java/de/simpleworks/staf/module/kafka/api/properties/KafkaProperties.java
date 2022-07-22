package de.simpleworks.staf.module.kafka.api.properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.annotation.Property;
import de.simpleworks.staf.commons.annotation.Property.Default;
import de.simpleworks.staf.commons.utils.PropertiesReader;
import de.simpleworks.staf.module.kafka.consts.ConumeMessagesDirectionValue;
import de.simpleworks.staf.module.kafka.consts.KafkaConsts;
import de.simpleworks.staf.module.kafka.enums.ConumeMessagesDirectionEnum;

public class KafkaProperties extends PropertiesReader {
	private static final Logger logger = LogManager.getLogger(KafkaProperties.class);

	private static KafkaProperties instance = null;

	@Default("100")
	@Property(KafkaConsts.POLL_TIMEOUT_MS)
	private int polltimeoutMs;

	@Property(KafkaConsts.BOOTSTRAP_SERVERS)
	private String bootstrapServers;

	@Default("STAF-Consumer-Group")
	@Property(KafkaConsts.CONSUMER_GROUP_ID)
	private String consumerGroupId;

	@Default(ConumeMessagesDirectionValue.ASCENDING)
	@Property(KafkaConsts.CONSUMER_CONSUME_MESSAGES_DIRECTION)
	private ConumeMessagesDirectionEnum consumerConsumeMessagesDirection;

	@Property(KafkaConsts.CONSUMER_CONSUME_MESSAGES_MAX)
	private String consumerConsumeMessagesMax;
	
	@Property(KafkaConsts.CONSUMER_CONSUME_OFFSET_MAX)
	private String consumerConsumeOffsetMax;

	public int getPolltimeoutMs() {
		return polltimeoutMs;
	}

	public String getBootstrapServers() {
		return bootstrapServers;
	}

	public String getConsumerGroupId() {
		return consumerGroupId;
	}

	public ConumeMessagesDirectionEnum getConsumerConsumeMessagesDirection() {
		return consumerConsumeMessagesDirection;
	}

	public String getConsumerConsumeMessagesMax() {
		return consumerConsumeMessagesMax;
	}
	
	public String getConsumerConsumeOffsetMax() {
		return consumerConsumeOffsetMax;
	}
		
	@Override
	protected Class<?> getClazz() {
		return KafkaProperties.class;
	}

	public static final synchronized KafkaProperties getInstance() {
		if (KafkaProperties.instance == null) {
			if (KafkaProperties.logger.isDebugEnabled()) {
				KafkaProperties.logger.debug(String.format("create instance of type '%s'.", KafkaProperties.class));
			}

			KafkaProperties.instance = new KafkaProperties();
		}

		return KafkaProperties.instance;
	}
}
