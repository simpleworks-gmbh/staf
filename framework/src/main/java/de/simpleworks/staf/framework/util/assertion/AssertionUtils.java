package de.simpleworks.staf.framework.util.assertion;

import java.util.Map;

import de.simpleworks.staf.commons.api.Assertion;
import de.simpleworks.staf.commons.enums.ValidateMethodEnum;
import de.simpleworks.staf.commons.interfaces.IPojo;

public abstract class AssertionUtils<Response extends IPojo> {
	protected final static void check(final IPojo response, final Assertion assertion,
			final ValidateMethodEnum expected) {
		if (response == null) {
			throw new IllegalArgumentException("response can't be null.");
		}

		if (assertion == null) {
			throw new IllegalArgumentException("assertion can't be null.");
		}

		if (expected == null) {
			throw new IllegalArgumentException("expected can't be null.");
		}

		if (!response.validate()) {
			throw new IllegalArgumentException(String.format("response is invalid '%s'.", response));
		}

		if (!assertion.validate()) {
			throw new IllegalArgumentException(String.format("assertion is invalid '%s'.", assertion));
		}

		if (!expected.equals(assertion.getValidateMethod())) {
			throw new IllegalArgumentException(String.format("assertion is '%s', but '%s' was expected.",
					assertion.getValidateMethod().getValue(), expected.getValue()));
		}
	}

	public abstract Map<String, String> validateAssertion(Response response, Assertion assertion);
}
