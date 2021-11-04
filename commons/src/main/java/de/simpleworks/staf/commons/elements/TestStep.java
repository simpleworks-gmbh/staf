package de.simpleworks.staf.commons.elements;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public class TestStep extends BaseId {
	private final int order;
	private final String summary;

	public TestStep(final int order, final String summary) {
		if (Convert.isEmpty(summary)) {
			throw new IllegalArgumentException("summary can't be null or empty string.");
		}

		this.order = order;
		this.summary = summary;
	}

	public int getOrder() {
		return order;
	}

	public String getSummary() {
		return summary;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + order;
		result = (prime * result) + ((summary == null) ? 0 : summary.hashCode());
		return result;
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
		final TestStep other = (TestStep) obj;
		if (order != other.order) {
			return false;
		}
		if (summary == null) {
			if (other.summary != null) {
				return false;
			}
		} else if (!summary.equals(other.summary)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s, %s]", Convert.getClassName(TestStep.class), super.toString(),
				UtilsFormat.format("order", order), UtilsFormat.format("summary", summary));
	}
}
