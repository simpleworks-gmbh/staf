package de.simpleworks.staf.module.jira.util;

import java.util.Optional;

import de.simpleworks.staf.commons.report.StepReport;
import de.simpleworks.staf.commons.report.TestcaseReport;
import de.simpleworks.staf.commons.utils.Convert;

public class UtilsTestcaseReport {
	private static JiraProperties configuration = JiraProperties.getInstance();

	public UtilsTestcaseReport() {
		throw new IllegalStateException("Utility class..");
	}

	public static final boolean wasScreenshotEnabled(final TestcaseReport report) {
		if (report == null) {
			throw new IllegalArgumentException("report can't be null.");
		}

		report.validate();

		final Optional<StepReport> screenshot = report.getSteps().stream().filter(step -> step.getArtefact() != null)
				.findAny();
		return screenshot.isPresent();
	}

	public static final String getScreenshotName(final String screenshotName) {
		if (Convert.isEmpty(screenshotName)) {
			throw new IllegalArgumentException("screenshotName can't be null or empty.");
		}

		return String.format("%s.%s", screenshotName, UtilsTestcaseReport.configuration.getScreenshotFormat());
	}
}
