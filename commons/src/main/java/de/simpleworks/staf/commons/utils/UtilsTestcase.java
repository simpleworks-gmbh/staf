package de.simpleworks.staf.commons.utils;

import java.util.ArrayList;
import java.util.List;

import de.simpleworks.staf.commons.api.APITeststep;
import de.simpleworks.staf.commons.elements.TestCase;

public class UtilsTestcase {

	public static List<APITeststep> convert(TestCase testcase) {

		if (testcase == null) {
			throw new IllegalArgumentException("testcase can't be null.");
		}

		final List<APITeststep> result = new ArrayList<>();

		testcase.getTestSteps().forEach(teststep -> {
			APITeststep apiteststep = new APITeststep();

			apiteststep.setName(teststep.getSummary());
			apiteststep.setOrder(teststep.getOrder());

			result.add(apiteststep);
		});

		return result;
	}
}