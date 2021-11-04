package de.simpleworks.staf.plugin.maven.testflo.commons.enums;

import org.junit.Assert;

import de.simpleworks.staf.commons.enums.Result;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public enum TestStepStatus implements ITestFloEnum {
	To_do("toDo"), InProgress("inProgress"), Failed("failed"), Passed("passed"), Blocked("blocked"), NA("na");

	private static final String FORMAT = "testflo.teststep.status.%s";

	private final String testFloName;

	TestStepStatus(final String key) {
		testFloName = Configuration.getInstance().get(String.format(TestStepStatus.FORMAT, key));
	}

	@Override
	public String getTestFloName() {
		return testFloName;
	}

	public static final TestStepStatus get(final Result result) {
		Assert.assertNotNull("result can't be null.", result);

		final TestStepStatus status;
		switch (result) {
		case SUCCESSFULL:
			status = Passed;
			break;
		case FAILURE:
			status = Failed;
			break;
		default:
			status = NA;
			break;
		}

		return status;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s]", Convert.getClassName(TestStepStatus.class),
				UtilsFormat.format("testFloName", testFloName));
	}
}
