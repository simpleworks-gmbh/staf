package de.simpleworks.staf.framework.elements.composited;
 
import de.simpleworks.staf.framework.enums.TestcaseKindEnum;

public class DBCompositedTestCase extends ACompositedTestCase {
	@Override
	public TestcaseKindEnum getTestcasekind() {
		return TestcaseKindEnum.DATABASE_TESTCASE;
	}
}
