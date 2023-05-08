package de.simpleworks.staf.framework.util.assertion;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import de.simpleworks.staf.commons.utils.JSONUtils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

public class JSONPATHAssertionValidator extends AssertionUtils<HttpResponse> {
	private static final Logger logger = LogManager.getLogger(JSONPATHAssertionValidator.class);

	@SuppressWarnings("unchecked")
	public static String executeJsonPath(final String body, final String path) {
		if (Convert.isEmpty(body)) {
			throw new IllegalArgumentException("body can't be null or empty string.");
		}
		if (Convert.isEmpty(path)) {
			throw new IllegalArgumentException("path can't be null or empty string.");
		}

		if (JSONPATHAssertionValidator.logger.isInfoEnabled()) {
			JSONPATHAssertionValidator.logger.info(String.format("run jsonpath '%s' on '%s'.", path, body));
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
		} else if (Convert.isBoolean(response)) {
			result = response.toString();
		} else if (response instanceof LinkedHashMap) {
			result = JSONObject.toJSONString((Map<String, ? extends Object>) response);
		} else {
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
		String content = JSONPATHAssertionValidator.executeJsonPath(response.getJsonBody(), jsonpath);
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

		final String originalContent = content;

		switch (allowedValueEnum) {
		case EVERYTHING:
			if (JSONPATHAssertionValidator.logger.isDebugEnabled()) {
				JSONPATHAssertionValidator.logger
						.debug(String.format("jsonpath '%s' returned '%s'.", jsonpath, content));
			}
			break;

		case RANDOM:

			if (!JSONUtils.isJSONArray(content)) {
				throw new RuntimeException(String.format(
						"The assertion \"%s\" was not met. Value can't be fetched randomly, because content is no json array [\"%s\"].",
						assertionId, content));
			}

			final JSONArray arrayToFetchRandomElement = JSONUtils.transformToJSONArray(content);

			if (arrayToFetchRandomElement == null) {
				throw new RuntimeException(
						String.format("The assertion \"%s\" was not met. JSONArray can't be fetched from [\"%s\"].",
								assertionId, content));
			}

			if (arrayToFetchRandomElement.size() == 0) {
				throw new RuntimeException(String.format(
						"The assertion \"%s\" was not met. JSONArray is empty [\"%s\"].", assertionId, content));
			}

			Object ob = arrayToFetchRandomElement
					.get((new Random()).nextInt(Math.max(arrayToFetchRandomElement.size(), 1)));

			content = ob.toString();

			if (JSONPATHAssertionValidator.logger.isDebugEnabled()) {
				JSONPATHAssertionValidator.logger
						.debug(String.format("fetched '%s' randomly from '%s'.", content, originalContent));
			}
			break;

		case MAX:

			if (!JSONUtils.isJSONArray(content)) {
				throw new RuntimeException(String.format(
						"The assertion \"%s\" was not met. Value can't be fetched randomly, because content is no json array [\"%s\"].",
						assertionId, content));
			}

			final JSONArray arrayToFetchMaxElement = JSONUtils.transformToJSONArray(originalContent);

			if (arrayToFetchMaxElement == null) {
				throw new RuntimeException(
						String.format("The assertion \"%s\" was not met. JSONArray can't be fetched from [\"%s\"].",
								assertionId, content));
			}

			if (arrayToFetchMaxElement.size() == 0) {
				throw new RuntimeException(String.format(
						"The assertion \"%s\" was not met. JSONArray is empty [\"%s\"].", assertionId, content));
			}

			Integer max = new Integer(0);

			final Iterator<Object> iterator = arrayToFetchMaxElement.iterator();

			Object nextElement = null;

			while (iterator.hasNext()) {

				Integer tmp = null;

				try {

					nextElement = iterator.next();

					if (nextElement instanceof Integer) {
						tmp = (Integer) nextElement;
					} else if (nextElement instanceof Integer) {
						tmp = new Integer((String) nextElement);
					} else {
						throw new Exception(String.format("type '%s' is not implemented yet.", nextElement.getClass()));
					}

					if (tmp.compareTo(max) > 0) {
						max = tmp;
					}
				} catch (Exception ex) {
					final String msg = String.format(
							"The assertion \"%s\" was not met. can't fetch value from [\"%s\"].", assertionId,
							nextElement);
					JSONPATHAssertionValidator.logger.error(msg, ex);
					throw new RuntimeException(msg);
				}
			}

			content = max.toString();

			if (JSONPATHAssertionValidator.logger.isDebugEnabled()) {
				JSONPATHAssertionValidator.logger
						.debug(String.format("'%s' is max value from '%s'.", content, originalContent));
			}
			break;

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
		case PART_OF:
			final List<String> elements = Arrays.asList(assertionValue.split("#"));
			
			if(elements.indexOf(content) < 0) {
				final String msg = String.format(
						"The assertion \"%s\" was not met. Expected value '%s', is not part of ['%s'].", assertionId,
						assertionValue, content);
				JSONPATHAssertionValidator.logger.error(msg);
				throw new RuntimeException(msg);
			}
			
			break;
		case REGEX:
			final Pattern pattern = Pattern.compile(assertionValue);
			final Matcher matcher = pattern.matcher(content);

			final Map<String, String> result = new HashMap<>();
			if (matcher.find()) {
				if (logger.isDebugEnabled()) {
					logger.debug(
							String.format("Regex found %d capture groups.", Integer.valueOf(matcher.groupCount())));
				}

				for (int i = 1; i <= matcher.groupCount(); i++) {
					final String assertionIdGroup = String.format("%s_G%d", assertionId, Integer.valueOf(i));
					result.put(assertionIdGroup, matcher.group(i));
				}
			}

			if (result.isEmpty()) {
				throw new RuntimeException(String.format(
						"The assertion  \"%s\" was not met. Fetched value '%s' does not match the regular expression '%s'.",
						assertionId, content, assertionValue));
			}
			return result;
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