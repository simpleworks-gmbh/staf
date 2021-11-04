package de.simpleworks.staf.commons.report;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public class TestplanReport {
	private static final Logger logger = LogManager.getLogger(TestplanReport.class);

	private final String issueKey;
	private final List<TestcaseReport> testcases;

	public TestplanReport(final String issueKey) {
		if (Convert.isEmpty(issueKey)) {
			throw new IllegalArgumentException("issueKey can't be null or empty.");
		}

		this.issueKey = issueKey;
		this.testcases = new ArrayList<>();
	}

	public String getIssueKey() {
		return issueKey;
	}

	public void validate() throws SystemException {
		if (TestplanReport.logger.isDebugEnabled()) {
			TestplanReport.logger.debug(String.format("validate: '%s'.", toString()));
		}

		if (Convert.isEmpty(issueKey)) {
			throw new SystemException("issueKey can't be null or empty.");
		}
	}

	public void addTestcase(final TestcaseReport testcase) {
		if (testcase == null) {
			throw new IllegalArgumentException("testcase can't be null");
		}

		testcase.validate();

		this.testcases.add(testcase);
	}

	public List<TestcaseReport> getTestcases() {
		return testcases;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s]", Convert.getClassName(TestplanReport.class),
				UtilsFormat.format("issueKey", issueKey), UtilsFormat.format("testcases", String.join(",",
						testcases.stream().map(step -> step.toString()).collect(Collectors.toList()))));
	}
}
