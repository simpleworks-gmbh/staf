package de.simpleworks.staf.framework.api.httpclient;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.annotation.Step;
import de.simpleworks.staf.commons.interfaces.ITeststep;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.comparer.StepComparator;
import de.simpleworks.staf.commons.utils.comparer.TeststepComparator;

public class TeststepProvider<Teststep extends ITeststep> {
	private static final Logger logger = LogManager.getLogger(TeststepProvider.class);

	private final List<Teststep> teststeps;
	private final List<Step> steps;

	public TeststepProvider(final List<Teststep> teststeps, final List<Step> steps) throws InstantiationException {
		if (Convert.isEmpty(teststeps)) {
			throw new IllegalArgumentException("teststeps can't be null or empty.");
		}

		if (Convert.isEmpty(steps)) {
			throw new IllegalArgumentException("steps can't be null or empty.");
		}

		this.teststeps = teststeps;
		this.steps = steps;
		TeststepProvider.init(teststeps, steps);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private final static <Teststep extends ITeststep> void init(final List<Teststep> teststeps, final List<Step> steps)
			throws InstantiationException {
		if (Convert.isEmpty(teststeps)) {
			throw new IllegalArgumentException("teststeps can't be null or empty.");
		}

		if (Convert.isEmpty(steps)) {
			throw new IllegalArgumentException("steps can't be null or empty.");
		}

		if (steps.size() != teststeps.size()) {
			final String msg = String.format(
					"The amount of 'Step'-annotated methods %d does not match the amount of the API Test Steps %d.",
					Integer.valueOf(steps.size()), Integer.valueOf(teststeps.size()));
			throw new InstantiationException(msg);
		}

		teststeps.sort(new TeststepComparator());
		steps.sort(new StepComparator());

		for (int i = 0; i < steps.size(); i++) {
			final Step step = steps.get(i);
			final Teststep teststep = teststeps.get(i);

			if (!teststep.validate()) {
				final String msg = String.format("The API Test Step '%s' is invalid.", teststep);
				throw new InstantiationException(msg);
			}

			if (step.order() != teststep.getOrder()) {
				final String msg = String.format(
						"The amount of 'Step'-annotated method '%s' does not match the order of the API Test Step '%s'.",
						step.description(), teststep.getName());
				throw new InstantiationException(msg);

			}

			if (!step.description().equals(teststep.getName())) {
				final String msg = String.format(
						"The 'Step'-annotated method '%s' does not match the API Test Step '%s'.", step.description(),
						teststep.getName());
				throw new InstantiationException(msg);
			}
		}
	}

	public Teststep get() {
		if (Convert.isEmpty(teststeps)) {
			return null;
		}

		if (TeststepProvider.logger.isDebugEnabled()) {
			TeststepProvider.logger.debug("sorting steps.");
		}
		teststeps.sort(new TeststepComparator<>());

		return teststeps.remove(0);
	}
}
