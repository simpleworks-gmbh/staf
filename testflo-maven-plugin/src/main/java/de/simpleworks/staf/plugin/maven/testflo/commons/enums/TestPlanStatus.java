package de.simpleworks.staf.plugin.maven.testflo.commons.enums;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public enum TestPlanStatus implements ITestFloEnum {
	Open("open"), InProgress("inProgress"), Acceptance("acceptance"), Closed("closed");

	private static final String FORMAT = "testflo.testplan.status.%s";

	private final String testFloName;

	TestPlanStatus(final String key) {
		testFloName = Configuration.getInstance().get(String.format(TestPlanStatus.FORMAT, key));
	}

	@Override
	public String getTestFloName() {
		return testFloName;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s]", Convert.getClassName(TestPlanStatus.class),
				UtilsFormat.format("testFloName", testFloName));
	}
}
