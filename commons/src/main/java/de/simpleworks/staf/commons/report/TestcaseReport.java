package de.simpleworks.staf.commons.report;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.enums.Result;
import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public class TestcaseReport {
	private static final Logger logger = LogManager.getLogger(TestcaseReport.class);

	private long startTime = -1;
	private long stopTime = -1;

	private final String id;
	private final List<StepReport> steps;

	public TestcaseReport(final String id) {
		if (id == null) {
			throw new IllegalArgumentException("id can't be null or empty string.");
		}

		this.id = id;
		this.steps = new ArrayList<>();
	}

	public String getId() {
		return id;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(final long startTime) {
		this.startTime = startTime;
	}

	public long getStopTime() {
		return stopTime;
	}

	public void setStopTime(final long stopTime) {
		this.stopTime = stopTime;
	}

	public void addStep(final StepReport step) throws SystemException {
		if (step == null) {
			throw new IllegalArgumentException("step can't be null.");
		}

		if (!step.validate()) {
			throw new SystemException("step is invalid .");
		}

		steps.add(step);
	}

	public boolean validate() {
		if (TestcaseReport.logger.isDebugEnabled()) {
			TestcaseReport.logger.debug(String.format("validate: '%s'.", toString()));
		}

		boolean result = true;

		if (Convert.isEmpty(id)) {
			TestcaseReport.logger.error("id can't be null or empty string.");
			result = false;
		}

		if (Convert.isEmpty(steps)) {
			TestcaseReport.logger.error("steps can't be null or empty.");
			result = false;
		}

		if (!steps.stream().filter(step -> !step.validate()).collect(Collectors.toList()).isEmpty()) {
			TestcaseReport.logger.error("at least one step is invalid.");
			result = false;
		}

		return result;
	}

	public List<StepReport> getSteps() {
		return steps;
	}

	public Result getResult() {
		if (Convert.isEmpty(steps)) {
			if (TestcaseReport.logger.isWarnEnabled()) {
				TestcaseReport.logger.warn(String.format("Testcase '%s' has no steps defined", id));
			}

			return Result.UNKNOWN;
		}

		if ((steps.stream().anyMatch(step -> step.getResult().equals(Result.FAILURE)))) {
			return Result.FAILURE;
		}
		if ((steps.stream().anyMatch(step -> step.getResult().equals(Result.UNKNOWN)))) {
			return Result.UNKNOWN;
		}

		return Result.SUCCESSFULL;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s, %s, %s]", Convert.getClassName(TestcaseReport.class),
				UtilsFormat.format("id", id), UtilsFormat.format("startTime", Long.valueOf(startTime)),
				UtilsFormat.format("stopTime", Long.valueOf(stopTime)), UtilsFormat.format("steps",
						String.join(",", steps.stream().map(step -> step.toString()).collect(Collectors.toList()))));
	}
}
