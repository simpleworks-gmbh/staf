package de.simpleworks.staf.commons.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public class TestCase extends BaseId {
	private List<TestStep> testSteps;
	private boolean isSorted;
	private String templateId;

	public TestCase() {
		testSteps = new ArrayList<>();
		isSorted = true;
		templateId = Convert.EMPTY_STRING;
	}

	public void add(final TestStep testStep) {
		if (testStep == null) {
			throw new IllegalArgumentException("testStep can't be null.");
		}

		testSteps.add(testStep);
		isSorted = false;
	}

	public List<TestStep> getTestSteps() {
		if (!isSorted) {
			Collections.sort(testSteps, (o1, o2) -> o1.getOrder() - o2.getOrder());

			isSorted = true;
		}

		return testSteps;
	}

	public void setTestSteps(final List<TestStep> testSteps) {
		this.testSteps = testSteps;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(final String templateId) {
		this.templateId = templateId;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s, %s, %s]", Convert.getClassName(TestCase.class), super.toString(),
				UtilsFormat.format("templateId", templateId), UtilsFormat.format("isSorted", isSorted),
				UtilsFormat.format("testSteps", String.join(",",
						Arrays.asList(testSteps).stream().map(a -> a.toString()).collect(Collectors.toList()))));
	}
}
