package de.simpleworks.staf.module.kafka.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.api.Assertion;
import de.simpleworks.staf.commons.enums.AllowedValueEnum;
import de.simpleworks.staf.commons.enums.ValidateMethodEnum;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.framework.util.assertion.AssertionUtils;
import de.simpleworks.staf.framework.util.assertion.JSONPATHAssertionValidator;
import de.simpleworks.staf.module.kafka.api.KafkaConsumeRecord;
import de.simpleworks.staf.module.kafka.api.KafkaConsumeResponse;

public class KAFKAMessageAssertionValidator extends AssertionUtils<KafkaConsumeResponse> {

	private static final Logger logger = LogManager.getLogger(KAFKAMessageAssertionValidator.class);

	@Override
	public Map<String, String> validateAssertion(KafkaConsumeResponse response, Assertion assertion) {

		AssertionUtils.check(response, assertion, ValidateMethodEnum.KAFKAMESSAGE_VALIDATION);

		final AllowedValueEnum allowedValueEnum = assertion.getAllowedValue();
		if (KAFKAMessageAssertionValidator.logger.isDebugEnabled()) {
			KAFKAMessageAssertionValidator.logger
					.debug(String.format("using allowedValue '%s' to validate response.", allowedValueEnum.getValue()));
		}

		final String assertionId = assertion.getId();
		if (KAFKAMessageAssertionValidator.logger.isDebugEnabled()) {
			KAFKAMessageAssertionValidator.logger.debug(String.format("assertion id '%s'.", assertionId));
		}
		final String assertionValue = assertion.getValue();

		if (KAFKAMessageAssertionValidator.logger.isDebugEnabled()) {
			KAFKAMessageAssertionValidator.logger.debug(String.format("assertion value '%s'.", assertionValue));
		}

		String content = Convert.EMPTY_STRING;

		switch (allowedValueEnum) {
		case AMOUNT_EQUALS_TO:

			int amountEqualsTo = -1;

			try {
				amountEqualsTo = Integer.parseInt(assertionValue);
			} catch (NumberFormatException ex) {
				throw new RuntimeException(String.format(
						"The assertion \"%s\" was not met. AssertionValue [\"%s\"] can't be parsed to an Integer.",
						assertionId, assertionValue));
			}

			final KafkaConsumeRecord[] amountEqualsToRecords = response.getRecords();

			if (amountEqualsTo != amountEqualsToRecords.length) {
				throw new RuntimeException(String.format(
						"The assertion \"%s\" was not met. The amount of records was expected to be '%s', but was '%s'.",
						assertionId, assertionValue, Integer.toString(amountEqualsToRecords.length)));
			}

			content = assertionValue;
			break;

		case AMOUNT_MORE_THAN:

			int amountMoreThan = -1;

			try {
				amountMoreThan = Integer.parseInt(assertionValue);
			} catch (NumberFormatException ex) {
				throw new RuntimeException(String.format(
						"The assertion \"%s\" was not met. AssertionValue [\"%s\"] can't be parsed to an Integer.",
						assertionId, assertionValue));
			}

			final KafkaConsumeRecord[] amountMoreThanRecords = response.getRecords();

			if (amountMoreThan < amountMoreThanRecords.length) {
				throw new RuntimeException(String.format(
						"The assertion \"%s\" was not met. The amount of records was expected to be more than '%s', but was '%s'.",
						assertionId, assertionValue, Integer.toString(amountMoreThanRecords.length)));
			}

			content = Integer.toString(amountMoreThanRecords.length);
			break;

		case EXACT_VALUE:

			for (KafkaConsumeRecord record : response.getRecords()) {

				final String jsonpath = assertion.getJsonpath();

				// fix me, no check if type is correct
				final String jsonBody = (String) record.getContent();
				content = JSONPATHAssertionValidator.executeJsonPath(jsonBody, jsonpath);

				if (!content.equals(assertionValue)) {
					throw new RuntimeException(String.format(
							"The assertion  \"%s\" was not met. Fetched value '%s' does not match the expected one '%s'.",
							assertionId, content, assertionValue));
				}
			}

			break;

		default:
			throw new IllegalArgumentException(
					String.format("The allowedValueEnum '%s' is not implemented yet.", allowedValueEnum.getValue()));
		}

		final Map<String, String> result = new HashMap<>();
		if (KAFKAMessageAssertionValidator.logger.isDebugEnabled()) {
			KAFKAMessageAssertionValidator.logger
					.debug(String.format("result: assertion id: '%s', content: '%s'.", assertion.getId(), content));
		}
		result.put(assertionId, content);
		return result;
	}
}
