package de.simpleworks.staf.plugin.maven.testflo.commons.enums;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public enum TestCaseTransition implements ITestFloEnumWithId {
	Test("test"), Pass("pass"), Fail("fail"), Retest("retest"), InProgress("inProgress"), RetestPass("retestPass"),
	RetestFail("retestFail"), Open("open");

	private static final String FORMAT_NAME = "testflo.testcase.transition.%s.name";
	private static final String FORMAT_ID = "testflo.testcase.transition.%s.id";

	private final String testFloName;
	private final int testFloId;

	TestCaseTransition(final String key) {
		testFloName = Configuration.getInstance().get(String.format(TestCaseTransition.FORMAT_NAME, key));
		testFloId = Configuration.getInstance().getInt(String.format(TestCaseTransition.FORMAT_ID, key));
	}

	@Override
	public String getTestFloName() {
		return testFloName;
	}

	@Override
	public int getTestFloId() {
		return testFloId;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s]", Convert.getClassName(TestCaseTransition.class),
				UtilsFormat.format("testFloName", testFloName), UtilsFormat.format("testFloId", testFloId));
	}
}
