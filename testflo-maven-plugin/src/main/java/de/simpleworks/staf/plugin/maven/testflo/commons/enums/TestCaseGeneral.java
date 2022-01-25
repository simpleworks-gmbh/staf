package de.simpleworks.staf.plugin.maven.testflo.commons.enums;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public enum TestCaseGeneral implements ITestFloEnum {
	TEMPLATE("template");

	private static final String FORMAT = "testflo.testcase.general.%s";
	private final String testFloName;

	TestCaseGeneral(final String key) {
		this.testFloName = Configuration.getInstance().get(String.format(TestCaseGeneral.FORMAT, key));
	}

	@Override
	public String getTestFloName() {
		return testFloName;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s]", Convert.getClassName(TestCaseGeneral.class),
				UtilsFormat.format("testFloName", testFloName));
	}
}