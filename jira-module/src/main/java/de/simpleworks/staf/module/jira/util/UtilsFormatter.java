package de.simpleworks.staf.module.jira.util;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import de.simpleworks.staf.commons.enums.Result;
import de.simpleworks.staf.commons.report.StepReport;
import de.simpleworks.staf.commons.report.TestcaseReport;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.IEnum;

public class UtilsFormatter {
	@SuppressWarnings("unchecked")
	public static final <ENUM extends IEnum> List<ENUM> createEnumList(final ENUM Enum, final List<String> labels) {
		if (Enum == null) {
			throw new IllegalArgumentException("Enum can't be null.");
		}

		if (Convert.isEmpty(Enum.getValues())) {
			throw new IllegalArgumentException(
					String.format("No Enums of type '%s', were declared.", Enum.getClass().getName()));
		}

		return labels.stream().map(label -> (ENUM) Enum.getEnumByValue(label)).collect(Collectors.toList());
	}

	public static final String createLink(final String text, final URL address) {
		if (Convert.isEmpty(text)) {
			throw new IllegalArgumentException("text can't be null or empty.");
		}

		if (address == null) {
			throw new IllegalArgumentException("address can't be null or empty.");
		}

		return String.format("[%s|%s]", text, address.toString());
	}

	public static final String createResult(final List<TestcaseReport> reports) {
		if (Convert.isEmpty(reports)) {
			throw new IllegalArgumentException("reports can't be null.");
		}

		final List<String> convertedResults = reports.stream()
				.map(report -> String.format("|%s|%s|", report.getId(), report.getResult().getValue()))
				.collect(Collectors.toList());

		return String.format("%s%s", "||Name||Ergebnis||\n", String.join("\n", convertedResults));
	}

	public static final String createFailedStepsComment(final TestcaseReport report) {
		if (report == null) {
			throw new IllegalArgumentException("report can't be null.");
		}

		report.validate();

		final List<StepReport> failedSteps = report.getSteps().stream()
				.filter(step -> Result.FAILURE.equals(step.getResult())).collect(Collectors.toList());
		if (Convert.isEmpty(failedSteps)) {
			return Convert.EMPTY_STRING;
		}

		if (UtilsTestcaseReport.wasScreenshotEnabled(report)) {
			final List<String> convertedResults = failedSteps.stream().map(step -> String.format("|%s|%s|%s|%s|",
					report.getId(), step.getDescription(), step.getError().getMessage().replace("\n", ""),
					String.format("!%s|%s!",
							(step.getArtefact() == null) ? Convert.EMPTY_STRING
									: UtilsTestcaseReport.getScreenshotName(step.getDescription()),
							"thumbnail")))
					.collect(Collectors.toList());

			return String.format("%s%s", "||Testcase||Step||Bemerkung||Screenshot||\n",
					String.join("\n", convertedResults));
		}

		final List<String> convertedResults = failedSteps.stream().map(step -> String.format("|%s|%s|%s|",
				report.getId(), step.getDescription(), step.getError().getMessage().replace("\n", "")))
				.collect(Collectors.toList());

		return String.format("%s%s", "||Testcase||Step||Bemerkung||\n", String.join("\n", convertedResults));
	}
}
