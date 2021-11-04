package de.simpleworks.staf.commons.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public class TestPlan extends BaseId {
	private final List<TestCase> testCases;
	private boolean isSorted;

	public TestPlan() {
		testCases = new ArrayList<>();
		isSorted = true;
	}

	public void add(final TestCase testCase) {
		if (testCase == null) {
			throw new IllegalArgumentException("testCase can't be null.");
		}

		testCases.add(testCase);
		isSorted = false;
	}

	public List<TestCase> getTestCases() {
		if (!isSorted) {
			Collections.sort(testCases, (o1, o2) -> o1.getId().compareTo(o2.getId()));

			isSorted = true;
		}

		return testCases;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s, %s]", Convert.getClassName(TestPlan.class), super.toString(),
				UtilsFormat.format("isSorted", isSorted), UtilsFormat.format("testCases", String.join(",",
						Arrays.asList(testCases).stream().map(a -> a.toString()).collect(Collectors.toList()))));
	}
}
