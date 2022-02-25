package de.simpleworks.staf.framework.util.assertion;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import de.simpleworks.staf.commons.api.Assertion;
import de.simpleworks.staf.commons.api.HttpResponse;
import de.simpleworks.staf.commons.api.ResponseEntity;
import de.simpleworks.staf.commons.enums.AllowedValueEnum;
import de.simpleworks.staf.commons.enums.ValidateMethodEnum;
import de.simpleworks.staf.commons.mapper.Mapper;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsCollection;

public class ResponseBodyAssertionValidator extends AssertionUtils<HttpResponse> {
	private static final Logger logger = LogManager.getLogger(ResponseBodyAssertionValidator.class);

	private static String convertEntities(final Object[] entities) {
		if (Convert.isEmpty(entities)) {
			throw new IllegalArgumentException("entities can't be null or empty.");
		}

		final ObjectMapper mapper = new ObjectMapper();

		final ArrayNode arrayNode = mapper.createArrayNode();

		for (Object entity : UtilsCollection.toList(entities)) {
			arrayNode.addPOJO(entity);
		}

		String result = Convert.EMPTY_STRING;

		try {
			result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(arrayNode);
		} catch (JsonProcessingException ex) {
			ResponseBodyAssertionValidator.logger.error("can't parse json from entities.", ex);
			result = Convert.EMPTY_STRING;
		}

		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static String fetchEntitesFromResponseBodyFile(final ResponseEntity entity, final File responsebodyFile) {

		String result = Convert.EMPTY_STRING;

		try {
			Mapper<?> mapper = entity.getMapper();

			List list = new ArrayList<>();

			for (Object entitye : mapper.readAll(responsebodyFile)) {

				Class clazz = entity.getEntityClass();
				Object castedObject = clazz.cast(entitye);
				list.add(castedObject);
			}

			Object[] entities = UtilsCollection.toArray(Object.class, list);
			result = convertEntities(entities);
		} catch (Exception ex) {
			ResponseBodyAssertionValidator.logger.error(String.format(
					"can't fetch entities from responsebodyFile '%s' will return empty string.", responsebodyFile), ex);
		}

		return result;
	}

	@Override
	public Map<String, String> validateAssertion(final HttpResponse response, final Assertion assertion) {
		AssertionUtils.check(response, assertion, ValidateMethodEnum.RESPONSEBODY);

		final AllowedValueEnum allowedValueEnum = assertion.getAllowedValue();
		if (ResponseBodyAssertionValidator.logger.isDebugEnabled()) {
			ResponseBodyAssertionValidator.logger
					.debug(String.format("using allowedValue '%s' to validate response.", allowedValueEnum.getValue()));
		}

		final String assertionId = assertion.getId();
		if (ResponseBodyAssertionValidator.logger.isDebugEnabled()) {
			ResponseBodyAssertionValidator.logger.debug(String.format("assertion id '%s'.", assertionId));
		}

		String assertionValue = Convert.EMPTY_STRING;

		if (!Convert.isEmpty(assertion.getValue())) {
			assertionValue = assertion.getValue();
			if (ResponseBodyAssertionValidator.logger.isDebugEnabled()) {
				ResponseBodyAssertionValidator.logger.debug(String.format("assertion value '%s'.", assertionValue));
			}
		} else {

			final ResponseEntity entity = response.getEntity();

			if (entity == null) {
				final String msg = "entity can't be null.";
				ResponseBodyAssertionValidator.logger.error(msg);
				throw new RuntimeException(msg);
			}

			assertionValue = fetchEntitesFromResponseBodyFile(entity, assertion.getResponsebodyAsFile());
		}

		final ResponseEntity entity = response.getEntity();

		final String json = convertEntities(entity.getEntities());

		if (Convert.isEmpty(json)) {
			throw new RuntimeException("json can't be null or empty string.");
		}

		switch (allowedValueEnum) {

		case STRICT_ORDER:
			try {

				JSONAssert.assertEquals(assertionValue, json, JSONCompareMode.STRICT);
			} catch (final JSONException ex) {
				final String msg = String.format(
						"The assertion \"%s\" was not met. Expected value '%s', but it was '%s'.", assertionId,
						assertionValue, json);
				ResponseBodyAssertionValidator.logger.error(msg, ex);
				throw new RuntimeException(msg);
			}
			break;
		case ANY_ORDER:
			try {
				JSONAssert.assertEquals(assertionValue, json, JSONCompareMode.LENIENT);
			} catch (final JSONException ex) {
				final String msg = String.format(
						"The assertion \"%s\" was not met. Expected value '%s', but it was '%s'.", assertionId,
						assertionValue, json);
				ResponseBodyAssertionValidator.logger.error(msg, ex);
				throw new RuntimeException(msg);
			}
			break;
		default:
			throw new IllegalArgumentException(
					String.format("The allowedValueEnum '%s' is not implemented yet.", allowedValueEnum.getValue()));
		}
		final Map<String, String> result = new HashMap<>();
		if (ResponseBodyAssertionValidator.logger.isDebugEnabled()) {
			ResponseBodyAssertionValidator.logger
					.debug(String.format("result: assertion id: '%s', content: '%s'.", assertion.getId(), json));
		}
		result.put(assertionId, json);
		return result;
	}
}