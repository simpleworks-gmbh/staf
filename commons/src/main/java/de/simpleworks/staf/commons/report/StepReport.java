package de.simpleworks.staf.commons.report;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.enums.Result;
import de.simpleworks.staf.commons.report.artefact.Artefact;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public class StepReport {
	private static final Logger logger = LogManager.getLogger(StepReport.class);

	private final String description;
	private final int order;
	private final long startTime;
	private final long stopTime;

	private final Result result;
	private final Exception error;

	@SuppressWarnings("rawtypes")
	private Artefact artefact;

	public StepReport(final String description) {
		this.result = Result.UNKNOWN;

		this.description = description;
		this.order = -1;
		this.error = null;

		this.startTime = -1;
		this.stopTime = -1;
		this.artefact = null;
	}

	public StepReport(final String description, final int order) {
		this(description, order, Result.SUCCESSFULL, null, -1, -1, null);
	}

	public StepReport(final String description, final int order, final long testStepStartTime,
			final long testStepStopTime, @SuppressWarnings("rawtypes") Artefact artefact) {
		this(description, order, Result.SUCCESSFULL, null, testStepStartTime, testStepStopTime, artefact);
	}

	public StepReport(final String description, final int order, final Exception error) {
		this(description, order, Result.FAILURE, error, -1, -1, null);
	}

	public StepReport(final String description, final int order, final Exception error, final long testStepStartTime,
			final long testStepStopTime, @SuppressWarnings("rawtypes") Artefact artefact) {
		this(description, order, Result.FAILURE, error, testStepStartTime, testStepStopTime, artefact);
	}

	public StepReport(final String description, final int order, final Result result, final Exception error,
			final long startTime, final long stopTime, @SuppressWarnings("rawtypes") Artefact artefact) {
		if (result == null) {
			if (StepReport.logger.isInfoEnabled()) {
				StepReport.logger.info(String.format("result is null, will return \"%s\".", Result.UNKNOWN.getValue()));
			}

			this.result = Result.UNKNOWN;
		} else {
			this.result = result;
		}

		this.description = description;
		this.order = order;
		this.error = error;

		this.startTime = startTime;
		this.stopTime = stopTime;

		this.artefact = artefact;
	}

	/**
	 *
	 * private final Result result; private final Exception error
	 */
	public boolean validate() {
		if (StepReport.logger.isDebugEnabled()) {
			StepReport.logger.debug(String.format("validate: '%s'.", toString()));
		}

		boolean valid = true;

		if (Convert.isEmpty(description)) {
			StepReport.logger.error("description can't be empty.");
			valid = false;
		}

		if (Result.FAILURE.equals(this.result)) {
			if (error == null) {
				StepReport.logger.error("error can't be empty.");
				valid = false;
			}
		}

		return valid;
	}

	public String getDescription() {
		return description;
	}

	public int getOrder() {
		return order;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getStopTime() {
		return stopTime;
	}

	public Result getResult() {
		return result;
	}

	public Exception getError() {
		return error;
	}

	@SuppressWarnings("rawtypes")
	public Artefact getArtefact() {
		return artefact;
	}

	@SuppressWarnings("rawtypes")
	public void setArtefact(final Artefact artefact) {
		this.artefact = artefact;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int res = 1;
		res = (prime * res) + ((artefact == null) ? 0 : artefact.hashCode());
		res = (prime * res) + ((description == null) ? 0 : description.hashCode());
		res = (prime * res) + ((error == null) ? 0 : error.hashCode());
		res = (prime * res) + order;
		res = (prime * res) + ((this.result == null) ? 0 : this.result.hashCode());
		return res;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final StepReport other = (StepReport) obj;
		if (artefact == null) {
			if (other.artefact != null) {
				return false;
			}
		} else if (!artefact.equals(other.artefact)) {
			return false;
		}
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (error == null) {
			if (other.error != null) {
				return false;
			}
		} else if (!error.equals(other.error)) {
			return false;
		}
		if (order != other.order) {
			return false;
		}

		if (startTime != other.getStartTime()) {
			return false;
		}

		if (stopTime != other.getStopTime()) {
			return false;
		}

		if (result != other.result) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s, %s, %s, %s, %s, %s]", Convert.getClassName(StepReport.class),
				UtilsFormat.format("description", description), UtilsFormat.format("order", order),
				UtilsFormat.format("startTime", Long.valueOf(startTime)),
				UtilsFormat.format("stopTime", Long.valueOf(stopTime)), UtilsFormat.format("result", result),
				UtilsFormat.format("error", error == null ? null : error.getClass()),
				UtilsFormat.format("artefact", artefact));
	}
}
