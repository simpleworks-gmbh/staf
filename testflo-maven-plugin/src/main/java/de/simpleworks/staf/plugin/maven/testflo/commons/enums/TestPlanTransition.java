package de.simpleworks.staf.plugin.maven.testflo.commons.enums;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public enum TestPlanTransition implements ITestFloEnumWithId {
	Start("start"), Acceptance("acceptance"), Accept("accept"), Retest("retest"), Stop("stop"), Close("close");

	private static final String FORMAT_NAME = "testflo.testplan.transition.%s.name";
	private static final String FORMAT_ID = "testflo.testplan.transition.%s.id";

	private final String testFloName;
	private final int testFloId;

	TestPlanTransition(final String key) {
		testFloName = Configuration.getInstance().get(String.format(TestPlanTransition.FORMAT_NAME, key));
		testFloId = Configuration.getInstance().getInt(String.format(TestPlanTransition.FORMAT_ID, key));
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
		return String.format("[%s: %s, %s]", Convert.getClassName(TestPlanTransition.class),
				UtilsFormat.format("testFloName", testFloName), UtilsFormat.format("testFloId", testFloId));
	}
}
