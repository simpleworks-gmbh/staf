package de.simpleworks.staf.module.kafka.api.properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.annotation.Property;
import de.simpleworks.staf.commons.annotation.Property.Default;
import de.simpleworks.staf.commons.utils.PropertiesReader;
import de.simpleworks.staf.module.kafka.consts.KafkaConsts;

public class KafkaProperties extends PropertiesReader {
	private static final Logger logger = LogManager.getLogger(KafkaProperties.class);

	private static KafkaProperties instance = null;

	@Property(KafkaConsts.BOOTSTRAP_SERVERS)
	private String bootstrapServers;

	@Default("STAF")
	@Property(KafkaConsts.CONSUMER_GROUP_ID)
	private String consumerGroupId;

	@Property(KafkaConsts.LISTENERS)
	private String listeners;

	public String getBootstrapServers() {
		return bootstrapServers;
	}

	public String getConsumerGroupId() {
		return consumerGroupId;
	}

	public String getListeners() {
		return listeners;
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
