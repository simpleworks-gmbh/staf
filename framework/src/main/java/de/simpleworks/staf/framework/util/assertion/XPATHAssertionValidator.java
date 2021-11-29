package de.simpleworks.staf.framework.util.assertion;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import de.simpleworks.staf.commons.api.Assertion;
import de.simpleworks.staf.commons.api.HttpResponse;
import de.simpleworks.staf.commons.enums.AllowedValueEnum;
import de.simpleworks.staf.commons.enums.ValidateMethodEnum;
import de.simpleworks.staf.commons.utils.Convert;

public class XPATHAssertionValidator extends AssertionUtils<HttpResponse> {
	private static final Logger logger = LogManager.getLogger(XPATHAssertionValidator.class);

	@Override
	public Map<String, String> validateAssertion(final HttpResponse response, final Assertion assertion) {
		AssertionUtils.check(response, assertion, ValidateMethodEnum.XPATH);

		final String xpath = assertion.getXpath();
		if (XPATHAssertionValidator.logger.isDebugEnabled()) {
			XPATHAssertionValidator.logger.debug(String.format("using xpath '%s'.", xpath));
		}

		final AllowedValueEnum allowedValue = assertion.getAllowedValue();
		if (XPATHAssertionValidator.logger.isDebugEnabled()) {
			XPATHAssertionValidator.logger.debug(String.format("using allowedValue '%s'.", allowedValue.getValue()));
		}

		final HtmlCleaner cleaner = new HtmlCleaner();
		final TagNode node = cleaner.clean(response.getBody());

		Object[] tagnode = new Object[]{};

		try {
			tagnode = node.evaluateXPath(xpath);
		} catch (final Exception ex) {
			final String msg = String.format("can't validate XPATH '%s' on the response '%s'.", assertion.getXpath(),
					response);
			XPATHAssertionValidator.logger.error(msg, ex);
		}

		if ((tagnode.length == 0) && AllowedValueEnum.NON_EMPTY.equals(allowedValue)) {
			throw new RuntimeException(
					"The assertion was not met. Value can't be fetched, but a 'non empty vaue' was expected.");
		}

		final TagNode temp = (TagNode) tagnode[0];
		final String attribute = assertion.getAttribute();
		if (XPATHAssertionValidator.logger.isDebugEnabled()) {
			XPATHAssertionValidator.logger.debug(String.format("attribute: '%s'.", attribute));
		}

		String value = Convert.EMPTY_STRING;
		if (Convert.isEmpty(attribute)) {
			value = temp.getText().toString().trim();
			if (XPATHAssertionValidator.logger.isDebugEnabled()) {
				XPATHAssertionValidator.logger.debug(String.format("value: '%s'.", value));
			}

			if (!assertion.getValue().equals(value) && AllowedValueEnum.EXACT_VALUE.equals(allowedValue)) {
				throw new RuntimeException(
						String.format("The assertion was not met. Expected value '%s', but it was '%s'.",
								assertion.getValue(), value));
			}

			if (!value.contains(assertion.getValue()) && AllowedValueEnum.CONTAINS_VALUE.equals(allowedValue)) {
				throw new RuntimeException(String.format("The assertion was not met. Value '%s' must contain '%s'.",
						value, assertion.getValue()));
			}

		} else {
			value = temp.getAttributeByName(attribute);
			if (XPATHAssertionValidator.logger.isDebugEnabled()) {
				XPATHAssertionValidator.logger.debug(String.format("value: '%s'.", value));
			}

			if (Convert.isEmpty(value) && AllowedValueEnum.NON_EMPTY.equals(allowedValue)) {
				throw new RuntimeException(
						"The assertion was not met. Value is empty, but a 'non empty one' was expected.");
			}
		}

		final Map<String, String> result = new HashMap<>();

		final String id = assertion.getId();
		if (XPATHAssertionValidator.logger.isDebugEnabled()) {
			XPATHAssertionValidator.logger
					.debug(String.format("using id '%s' to store the fetched value '%s'.", id, value));
		}

		result.put(id, value);

		return result;
	}
}
