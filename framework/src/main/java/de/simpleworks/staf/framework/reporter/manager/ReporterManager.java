package de.simpleworks.staf.framework.reporter.manager;

import java.io.File;

import de.simpleworks.staf.commons.report.TestcaseReport;

public abstract class ReporterManager {

	/**
	 * @param TestcaseReport report, to be safed
	 * @return true is {@param report} was safed successfully.
	 */
	public abstract boolean saveReport(TestcaseReport report);

	/**
	 * @param TestcaseReport report, to be safed
	 * @return file to save the report to.
	 */
	public abstract File setUpFilePath(TestcaseReport report);
}
