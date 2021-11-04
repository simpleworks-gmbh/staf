package de.simpleworks.staf.commons.elements;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public class TestRun extends BaseId {
	private final TestPlan testplan;

	public TestRun(final TestPlan testplan) {
		super();

		if (testplan == null) {
			throw new IllegalArgumentException("testplan can't be null.");
		}

		this.testplan = testplan;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s]", Convert.getClassName(TestRun.class), super.toString(),
				UtilsFormat.format("testplan", testplan));
	}
}