package de.simpleworks.staf.commons.utils.comparer;

import java.util.Comparator;

import de.simpleworks.staf.commons.interfaces.ITeststep;

public class TeststepComparator<Teststep extends ITeststep> implements Comparator<Teststep> {
	@Override
	public int compare(final Teststep teststep1, final Teststep teststep2) {
		if (teststep1 == null) {
			throw new IllegalArgumentException("teststep1 can't be null.");
		}

		if (teststep2 == null) {
			throw new IllegalArgumentException("teststep2 can't be null.");
		}

		final int result = teststep1.getOrder() - teststep2.getOrder();
		if (result == 0) {
			throw new RuntimeException(
					String.format("Method '%s' and Method '%s' share the same order %d, which is not provided.",
							teststep1.getName(), teststep2.getName(), Integer.valueOf(teststep1.getOrder())));
		}
		return result;
	}
}