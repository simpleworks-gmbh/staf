package de.simpleworks.staf.commons.report;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.simpleworks.staf.commons.exceptions.SystemException;

class StepReportTest {
	@SuppressWarnings("static-method")
	@Test
	void toString_ErrorNull() {
		final StepReport report = new StepReport("lol", 1);
		Assertions.assertEquals(
				"[StepReport: description: 'lol', order: 1, result: [Result: name: 'SUCCESSFULL', value: 'SUCCESSFULL'], error: null, artefact: null]",
				report.toString());
	}

	@SuppressWarnings("static-method")
	@Test
	void toString_ErrorNotNull() {
		final StepReport report = new StepReport("lol", 1, new SystemException("lol"));
		Assertions.assertEquals(
				"[StepReport: description: 'lol', order: 1, result: [Result: name: 'FAILURE', value: 'FAILURE'], error: class de.simpleworks.commons.exceptions.SystemException, artefact: null]",
				report.toString());
	}
}
