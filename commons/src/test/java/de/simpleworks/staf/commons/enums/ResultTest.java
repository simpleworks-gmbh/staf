package de.simpleworks.staf.commons.enums;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResultTest {

	@SuppressWarnings("static-method")
	@Test
	void toString_SUCCESSFULL() {
		Assertions.assertEquals("[Result: name: \"SUCCESSFULL\", value: \"SUCCESSFULL\"]",
				Result.SUCCESSFULL.toString());
	}

	@SuppressWarnings("static-method")
	@Test
	void toString_FAILURE() {
		Assertions.assertEquals("[Result: name: \"FAILURE\", value: \"FAILURE\"]", Result.FAILURE.toString());
	}

	@SuppressWarnings("static-method")
	@Test
	void toString_UNKNOWN() {
		Assertions.assertEquals("[Result: name: \"UNKNOWN\", value: \"UNKNOWN\"]", Result.UNKNOWN.toString());
	}
}
