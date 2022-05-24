package de.simpleworks.staf.module.kafka.consume;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.api.Assertion;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;
import de.simpleworks.staf.module.kafka.api.IKafkaRequest;
import de.simpleworks.staf.module.kafka.api.IKafkaTeststep;
import de.simpleworks.staf.module.kafka.api.KafkaConsumeRequest;

public class KafkaTeststep implements IKafkaTeststep<KafkaConsumeRequest> {

	private static final Logger logger = LogManager.getLogger(KafkaTeststep.class);

	private String name;
	private int order;
	private KafkaConsumeRequest request;
	private Assertion[] assertions;

	public KafkaTeststep() {
		name = Convert.EMPTY_STRING;
		order = -1;
		request = new KafkaConsumeRequest();
		assertions = new Assertion[0];
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
	public IKafkaRequest<KafkaConsumeRequest> getRequest() {
		return request;
	}

	@Override
	public void setRequest(KafkaConsumeRequest request) {
		this.request = request;
	}

	public Assertion[] getAssertions() {
		return assertions;
	}

	public void setAssertions(Assertion[] assertions) {
		this.assertions = assertions;
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

		if (!Convert.isEmpty(assertions)) {
			List<Assertion> currentAssertions = Arrays.asList(assertions);

			if (currentAssertions.stream().filter(a -> a.validate()).collect(Collectors.toList()).isEmpty()) {
				KafkaTeststep.logger.error(String.format("assertions are invalid [%s].", String.join(",",
						Arrays.asList(assertions).stream().map(a -> a.toString()).collect(Collectors.toList()))));
				result = false;
			}

			for (Assertion assertion : currentAssertions) {
				if (currentAssertions.indexOf(assertion) != currentAssertions.lastIndexOf(assertion)) {
					KafkaTeststep.logger.error(String
							.format("assertion \"%s\" is used at last two times, which is not supported.", assertion));
					result = false;
					break;
				}
			}

		}

		return result;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s, %s, %s]", Convert.getClassName(KafkaTeststep.class),
				UtilsFormat.format("name", name), UtilsFormat.format("order", order),
				UtilsFormat.format("produce", request), UtilsFormat.format("assertions", String.join(",",
						Arrays.asList(assertions).stream().map(a -> a.toString()).collect(Collectors.toList()))));
	}

}
