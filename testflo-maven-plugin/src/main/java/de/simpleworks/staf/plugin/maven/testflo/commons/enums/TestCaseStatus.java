package de.simpleworks.staf.plugin.maven.testflo.commons.enums;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public enum TestCaseStatus implements ITestFloEnum {
	Open("open"), InProgress("inProgress"), Pass("pass"), Fail("fail"), Retest("retest");

	private static final String FORMAT = "testflo.testcase.status.%s";

	private final String testFloName;

	TestCaseStatus(final String key) {
		this.testFloName = Configuration.getInstance().get(String.format(TestCaseStatus.FORMAT, key));
	}

	@Override
	public String getTestFloName() {
		return testFloName;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s]", Convert.getClassName(TestCaseStatus.class),
				UtilsFormat.format("testFloName", testFloName));
	}
}
