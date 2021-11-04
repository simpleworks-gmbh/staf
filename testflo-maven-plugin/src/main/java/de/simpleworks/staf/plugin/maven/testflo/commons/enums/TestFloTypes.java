package de.simpleworks.staf.plugin.maven.testflo.commons.enums;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public enum TestFloTypes implements ITestFloEnum {
	TestPlan("testplan"), TestCase("testcase");

	private static final String FORMAT = "testflo.type.%s";

	private final String testFloName;

	TestFloTypes(final String key) {
		testFloName = Configuration.getInstance().get(String.format(TestFloTypes.FORMAT, key));
	}

	@Override
	public String getTestFloName() {
		return testFloName;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s]", Convert.getClassName(TestFloTypes.class),
				UtilsFormat.format("testFloName", testFloName));
	}
}
