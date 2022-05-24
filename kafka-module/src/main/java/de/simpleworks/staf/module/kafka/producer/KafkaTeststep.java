package de.simpleworks.staf.module.kafka.producer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;
import de.simpleworks.staf.module.kafka.api.IKafkaRequest;
import de.simpleworks.staf.module.kafka.api.IKafkaTeststep;
import de.simpleworks.staf.module.kafka.api.KafkaProduceRequest;

public class KafkaTeststep implements IKafkaTeststep<KafkaProduceRequest> {

	private static final Logger logger = LogManager.getLogger(KafkaTeststep.class);

	private String name;
	private int order;
	private KafkaProduceRequest request;

	public KafkaTeststep() {
		name = Convert.EMPTY_STRING;
		order = -1;
		request = new KafkaProduceRequest();
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public IKafkaRequest<KafkaProduceRequest> getRequest() {
		return request;
	}

	@Override
	public void setRequest(KafkaProduceRequest request) {
		this.request = request;
	}

	@Override
	public boolean validate() {
		if (KafkaTeststep.logger.isDebugEnabled()) {
			KafkaTeststep.logger.debug("validate KafkaTeststep...");
		}

		boolean result = true;

		if (Convert.isEmpty(name)) {
			if (KafkaTeststep.logger.isDebugEnabled()) {
				KafkaTeststep.logger.debug("name can't be null or empty string.");
			}
			result = false;
		}

		if (order < 1) {
			KafkaTeststep.logger
					.error(String.format("order can't be less than 1, but was \"%s\".", Integer.toString(order)));
			result = false;
		}

		if (request == null) {
			KafkaTeststep.logger.error("produce can't be null.");
			result = false;
		}

		if (!request.validate()) {
			KafkaTeststep.logger.error(String.format("produce '%s' is invalid.", request));
			result = false;
		}

		return result;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s, %s]", Convert.getClassName(KafkaTeststep.class),
				UtilsFormat.format("name", name), UtilsFormat.format("order", order),
				UtilsFormat.format("produce", request));
	}

}
