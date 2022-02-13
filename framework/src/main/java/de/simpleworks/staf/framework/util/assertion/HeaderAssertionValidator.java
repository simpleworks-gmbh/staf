package de.simpleworks.staf.framework.util.assertion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.simpleworks.staf.commons.api.Assertion;
import de.simpleworks.staf.commons.api.Header;
import de.simpleworks.staf.commons.api.HttpResponse;
import de.simpleworks.staf.commons.enums.AllowedValueEnum;
import de.simpleworks.staf.commons.enums.ValidateMethodEnum;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsCollection;

public class HeaderAssertionValidator extends AssertionUtils<HttpResponse> {
	private static final Logger logger = LogManager.getLogger(HeaderAssertionValidator.class);

	/**
	 * @brief method to fetch the Header from {@param headers} that has the name
	 *        {@param name}
	 * @param (Header[]) headers , (String) name
	 * @return (String) value of the matching Header
	 * @throws RuntimeException if a matching header can't be determined (zero or
	 *                          multiple matching instances)
	 */
	private static String fetchHeaderValue(final Header[] headers, final String name) {
		if (Convert.isEmpty(headers)) {
			throw new IllegalArgumentException("headers can't be null or empty.");
		}
		List<Header> headerList = UtilsCollection.toList(headers);
		if (!Convert.isEmpty(headerList.stream().filter(header -> !header.validate()).collect(Collectors.toList()))) {
			throw new IllegalArgumentException(String.format("not all headers [\"%s\"]  are valid.", String.join(",",
					headerList.stream().map(header -> header.toString()).collect(Collectors.toList()))));
		}
		if (Convert.isEmpty(name)) {
			throw new IllegalArgumentException("name can't be null or empty string.");
		}
		List<Header> matchingHeaders = headerList.stream().filter(header -> name.equals(header.getName()))
				.collect(Collectors.toList());
		if (matchingHeaders.size() == 0) {
			throw new RuntimeException((String.format("The header \"%s\" can't be found.", name)));
		} else if (matchingHeaders.size() > 1) {
			throw new RuntimeException(String.format("The header \"%s\" is found times \"%s\", expected is only one.",
					name, Integer.toString(matchingHeaders.size())));
		}
		final Header matchingHeader = matchingHeaders.get(0);
		final String result = matchingHeader.getValue();
		return result;
	}

	@Override
	public Map<String, String> validateAssertion(final HttpResponse response, final Assertion assertion) {
		AssertionUtils.check(response, assertion, ValidateMethodEnum.HEADER);
		final String headername = assertion.getHeadername();
		if (HeaderAssertionValidator.logger.isDebugEnabled()) {
			HeaderAssertionValidator.logger.debug(String.format("using headername '%s'.", headername));
		}
		final AllowedValueEnum allowedValueEnum = assertion.getAllowedValue();
		if (HeaderAssertionValidator.logger.isDebugEnabled()) {
			HeaderAssertionValidator.logger
					.debug(String.format("using allowedValue '%s'.", allowedValueEnum.getValue()));
		}
		final String content = HeaderAssertionValidator.fetchHeaderValue(response.getHeaders(), headername);
		if (HeaderAssertionValidator.logger.isDebugEnabled()) {
			HeaderAssertionValidator.logger.debug(String.format("content '%s'.", content));
		}
		final String assertionId = assertion.getId();
		if (HeaderAssertionValidator.logger.isDebugEnabled()) {
			HeaderAssertionValidator.logger.debug(String.format("assertion id '%s'.", assertionId));
		}
		final String assertionValue = assertion.getValue();
		if (HeaderAssertionValidator.logger.isDebugEnabled()) {
			HeaderAssertionValidator.logger.debug(String.format("assertion value '%s'.", assertionValue));
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
		default:
			throw new IllegalArgumentException(
					String.format("The allowedValueEnum '%s' is not implemented yet.", allowedValueEnum.getValue()));
		}
		final Map<String, String> result = new HashMap<>();
		if (HeaderAssertionValidator.logger.isDebugEnabled()) {
			HeaderAssertionValidator.logger
					.debug(String.format("result: assertion id: '%s', content: '%s'.", assertion.getId(), content));
		}
		result.put(assertionId, content);
		return result;
	}
}