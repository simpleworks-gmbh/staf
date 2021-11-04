package de.simpleworks.staf.plugin.maven.xray.elements;

import de.simpleworks.staf.commons.utils.Convert;

public class XrayResult {

	private String testExecutionKey;
	private Test[] tests;

	public XrayResult() {
		this.testExecutionKey = Convert.EMPTY_STRING;
		this.tests = new Test[0];
	}

	public String getTestExecutionKey() {
		return testExecutionKey;
	}

	public void setTestExecutionKey(final String testExecutionKey) {
		this.testExecutionKey = testExecutionKey;
	}

	public Test[] getTests() {
		return tests;
	}

	public void setTests(final Test[] tests) {
		this.tests = tests;
	}
}
