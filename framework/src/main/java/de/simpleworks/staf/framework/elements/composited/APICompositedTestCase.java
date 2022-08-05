package de.simpleworks.staf.framework.elements.composited;

import de.simpleworks.staf.framework.enums.TestcaseKindEnum;

public class APICompositedTestCase extends ACompositedTestCase {
	@Override
	public TestcaseKindEnum getTestcasekind() {
		return TestcaseKindEnum.API_TESTCASE;
	}

}
