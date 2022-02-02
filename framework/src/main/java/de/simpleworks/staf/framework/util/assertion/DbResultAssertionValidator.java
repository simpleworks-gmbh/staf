package de.simpleworks.staf.framework.util.assertion;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.api.Assertion;
import de.simpleworks.staf.commons.database.QueuedDbResult;
import de.simpleworks.staf.commons.enums.AllowedValueEnum;
import de.simpleworks.staf.commons.enums.ValidateMethodEnum;
import de.simpleworks.staf.commons.utils.Convert;

public class DbResultAssertionValidator extends AssertionUtils<QueuedDbResult> {

	private static final Logger logger = LogManager.getLogger(DbResultAssertionValidator.class);

	@Override
	public Map<String, String> validateAssertion(final QueuedDbResult response, final Assertion assertion) {
		AssertionUtils.check(response, assertion, ValidateMethodEnum.DB_RESULT);

		final AllowedValueEnum allowedValueEnum = assertion.getAllowedValue();
		if (DbResultAssertionValidator.logger.isDebugEnabled()) {
			DbResultAssertionValidator.logger
					.debug(String.format("using allowedValue '%s'.", allowedValueEnum.getValue()));
		}

		final String assertionValue = assertion.getValue();
		if (DbResultAssertionValidator.logger.isDebugEnabled()) {
			DbResultAssertionValidator.logger.debug(String.format("assertion value '%s'.", assertionValue));
		}

		final String assertionAtribute = assertion.getAttribute();
		if (DbResultAssertionValidator.logger.isDebugEnabled()) {
			DbResultAssertionValidator.logger.debug(String.format("assertion attribute '%s'.", assertionAtribute));
		}

		String content = Convert.EMPTY_STRING;

		switch (allowedValueEnum) {

		case NOT:

			for (Map<String, String> map : response.getResult()) {
				content = map.get(assertionAtribute);

				if (content == null) {
					throw new RuntimeException(String
							.format("Can't validate assertion '%s'. The fetched value was null.", assertion.getId()));
				}

				if (content.equals(assertionValue)) {
					throw new RuntimeException(String.format(
							"The assertion '%s' was not met. Fetched value '%s' does match the expected one '%s'.",
							assertion.getId(), content, assertionValue));
				}
			}
			break;

		case NON_EMPTY:
			if (Convert.isEmpty(response.getResult())) {
				throw new RuntimeException(String.format(
						"The assertion '%s' was not met. Value can't be fetched, but a \"non empty vaue\" was expected.",
						assertion.getId()));
			}
			break;

		case CONTAINS_VALUE:

			for (Map<String, String> map : response.getResult()) {
				content = map.get(assertionAtribute);

				if (content == null) {
					throw new RuntimeException("Can't validate assertion. The fetched value was null.");
				}

				if (!content.contains(assertionValue)) {
					throw new RuntimeException(
							String.format("The assertion '%s' was not met. Fetched value '%s' does not contain '%s'.",
									assertion.getId(), content, assertionValue));
				}
			}

			break;

		case EXACT_VALUE:

			for (Map<String, String> map : response.getResult()) {
				content = map.get(assertionAtribute);

				if (content == null) {
					throw new RuntimeException("Can't validate assertion. The fetched value was null.");
				}

				if (!content.equals(assertionValue)) {
					throw new RuntimeException(String.format(
							"The assertion '%s' was not met. Fetched value '%s' does not match the expected one '%s'.",
							assertion.getId(), content, assertionValue));
				}
			}
			break;

		default:
			throw new IllegalArgumentException(
					String.format("The allowedValueEnum '%s' is not implemented yet.", allowedValueEnum.getValue()));
		}

		final Map<String, String> result = new HashMap<>();

		if (DbResultAssertionValidator.logger.isDebugEnabled()) {
			DbResultAssertionValidator.logger
					.debug(String.format("result: assertion id: '%s', content: '%s'.", assertion.getId(), content));
		}

		result.put(assertion.getId(), content);
		return result;
	}
}
