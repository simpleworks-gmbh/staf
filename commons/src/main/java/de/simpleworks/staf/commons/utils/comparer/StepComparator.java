package de.simpleworks.staf.commons.utils.comparer;

import java.util.Comparator;

import de.simpleworks.staf.commons.annotation.Step;

public class StepComparator implements Comparator<Step> {
	@Override
	public int compare(final Step step1, final Step step2) {
		if (step1 == null) {
			throw new IllegalArgumentException("step1 can't be null.");
		}

		if (step2 == null) {
			throw new IllegalArgumentException("step2 can't be null.");
		}

		final int result = step1.order() - step2.order();
		if (result == 0) {
			throw new RuntimeException(
					String.format("Step '%s' and Step '%s' share the same order %d, which is not provided.",
							step1.description(), step2.description(), Integer.valueOf(step1.order())));
		}
		return result;
	}
}