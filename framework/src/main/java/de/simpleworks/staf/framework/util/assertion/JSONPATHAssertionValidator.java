package de.simpleworks.staf.framework.util.assertion;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.jayway.jsonpath.JsonPath;

import de.simpleworks.staf.commons.api.Assertion;
import de.simpleworks.staf.commons.api.HttpResponse;
import de.simpleworks.staf.commons.enums.AllowedValueEnum;
import de.simpleworks.staf.commons.enums.ValidateMethodEnum;
import de.simpleworks.staf.commons.utils.Convert;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

public class JSONPATHAssertionValidator extends AssertionUtils<HttpResponse> {
	private static final Logger logger = LogManager.getLogger(JSONPATHAssertionValidator.class);

	@SuppressWarnings("unchecked")
	private static String executeJsonPath(final String body, final String path) {
		if (Convert.isEmpty(body)) {
			throw new IllegalArgumentException("body can't be null or empty string.");
		}

		if (Convert.isEmpty(path)) {
			throw new IllegalArgumentException("path can't be null or empty string.");
		}

		final Object response = JsonPath.read(body, path);
		if (response == null) {
			throw new RuntimeException(String.format("can't get path: '%s' from body: '%s'.", path, body));
		}

		String result = Convert.EMPTY_STRING;
		if (response instanceof String) {
			result = (String) response;
		} else if (response instanceof JSONArray) {
			final JSONArray jsonArray = (JSONArray) response;
			result = jsonArray.toJSONString();
		} else if (Convert.isNumeric(response)) {
			result = response.toString();
		} else if (response instanceof LinkedHashMap) {
			result = JSONObject.toJSONString((Map<String, ? extends Object>) response);
		}

		else {
			throw new RuntimeException(String.format("can't handle response of '%s'.", response.getClass().getName()));
		}

		return result;
	}

	@Override
	public Map<String, String> validateAssertion(final HttpResponse response, final Assertion assertion) {
		AssertionUtils.check(response, assertion, ValidateMethodEnum.JSONPATH);

		final String jsonpath = assertion.getJsonpath();
		if (JSONPATHAssertionValidator.logger.isDebugEnabled()) {
			JSONPATHAssertionValidator.logger
					.debug(String.format("using jsonpath '%s' to validate response.", jsonpath));
		}

		final AllowedValueEnum allowedValueEnum = assertion.getAllowedValue();
		if (JSONPATHAssertionValidator.logger.isDebugEnabled()) {
			JSONPATHAssertionValidator.logger
					.debug(String.format("using allowedValue '%s' to validate response.", allowedValueEnum.getValue()));
		}

		final String content = JSONPATHAssertionValidator.executeJsonPath(response.getJsonBody(), jsonpath);
		if (JSONPATHAssertionValidator.logger.isDebugEnabled()) {
			JSONPATHAssertionValidator.logger.debug(String.format("content '%s'.", content));
		}

		final String assertionId = assertion.getId();
		if (JSONPATHAssertionValidator.logger.isDebugEnabled()) {
			JSONPATHAssertionValidator.logger.debug(String.format("assertion id '%s'.", assertionId));
		}

		final String assertionValue = assertion.getValue();
		if (JSONPATHAssertionValidator.logger.isDebugEnabled()) {
			JSONPATHAssertionValidator.logger.debug(String.format("assertion value '%s'.", assertionValue));
		}

		switch (allowedValueEnum) {
		case NOT:
			if ((content.equals(assertionValue))) {
				throw new RuntimeException(String.format(
						"The assertion \"%s\" was not met. Fetched value '%s' does match the expected one '%s'.",
						assertionId, content, assertionValue));
			}
			break;

		case NON_EMPTY:
			if (Convert.isEmpty(content)) {
				throw new RuntimeException(String.format(
						"The assertion \"%s\" was not met. Value can't be fetched, but a \"non empty vaue\" was expected.",
						assertionId));
			}
			break;

		case CONTAINS_VALUE:
			if (!content.contains(assertionValue)) {
				throw new RuntimeException(
						String.format("The assertion \"%s\" was not met. Fetched value '%s' does not contain '%s'.",
								assertionId, content, assertionValue));
			}
			break;

		case EXACT_VALUE:
			if (!content.equals(assertionValue)) {
				throw new RuntimeException(String.format(
						"The assertion  \"%s\" was not met. Fetched value '%s' does not match the expected one '%s'.",
						assertionId, content, assertionValue));
			}
			break;

		case STRICT_ORDER:
			try {
				JSONAssert.assertEquals(assertionValue, content, JSONCompareMode.STRICT);
			} catch (final JSONException ex) {
				final String msg = String.format(
						"The assertion \"%s\" was not met. Expected value '%s', but it was '%s'.", assertionId,
						assertionValue, content);
				JSONPATHAssertionValidator.logger.error(msg, ex);
				throw new RuntimeException(msg);
			}
			break;

		case ANY_ORDER:
			try {
				JSONAssert.assertEquals(assertionValue, content, JSONCompareMode.LENIENT);
			} catch (final JSONException ex) {
				final String msg = String.format(
						"The assertion \"%s\" was not met. Expected value '%s', but it was '%s'.", assertionId,
						assertionValue, content);
				JSONPATHAssertionValidator.logger.error(msg, ex);
				throw new RuntimeException(msg);
			}
			break;

		default:
			throw new IllegalArgumentException(
					String.format("The allowedValueEnum '%s' is not implemented yet.", allowedValueEnum.getValue()));
		}

		final Map<String, String> result = new HashMap<>();

		if (JSONPATHAssertionValidator.logger.isDebugEnabled()) {
			JSONPATHAssertionValidator.logger
					.debug(String.format("result: assertion id: '%s', content: '%s'.", assertion.getId(), content));
		}

		result.put(assertionId, content);

		return result;
	}

}
