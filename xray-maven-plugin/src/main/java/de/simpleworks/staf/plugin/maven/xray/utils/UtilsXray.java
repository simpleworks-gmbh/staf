package de.simpleworks.staf.plugin.maven.xray.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.enums.ArtefactEnum;
import de.simpleworks.staf.commons.enums.Result;
import de.simpleworks.staf.commons.report.StepReport;
import de.simpleworks.staf.commons.report.artefact.Artefact;
import de.simpleworks.staf.commons.report.artefact.Screenshot;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.plugin.maven.xray.elements.Evidence;
import de.simpleworks.staf.plugin.maven.xray.elements.Step;
import de.simpleworks.staf.plugin.maven.xray.enums.StatusEnum;

public class UtilsXray {

	private static final Logger logger = LogManager.getLogger(UtilsXray.class);

	public static StatusEnum convert(final Result result) {

		if (result == null) {
			throw new IllegalArgumentException("result can't be null.");
		}

		StatusEnum status = StatusEnum.FAILED;

		switch (result) {

		case SUCCESSFULL:
			status = StatusEnum.PASSED;
			break;

		case FAILURE:
			status = StatusEnum.FAILED;
			break;

		case UNKNOWN:
			status = StatusEnum.TODO;
			break;

		default:
			throw new IllegalArgumentException(
					String.format("the result \"%s\" has not been implemented yet.", result.getValue()));
		}

		return status;
	}

	public static Step[] convert(final List<StepReport> stepreports) {

		if (Convert.isEmpty(stepreports)) {
			throw new IllegalArgumentException("stepreports can't be null or empty.");
		}

		final List<Step> steps = new ArrayList<>();

		for (final StepReport stepreport : stepreports) {

			final Step step = new Step();
			step.setStatus(UtilsXray.convert(stepreport.getResult()));

			if (Result.FAILURE.equals(stepreport.getResult())) {

				final Exception error = stepreport.getError();
				if (error == null) {
					UtilsXray.logger.error(
							String.format("can't fetch error object from step \"%S\".", stepreport.getDescription()));
					continue;
				}
				step.setActualResult(error.getMessage());
			}

			@SuppressWarnings("rawtypes")
			final Artefact artefact = stepreport.getArtefact();

			if (artefact != null) {

				final ArtefactEnum type = artefact.getType();

				switch (type) {
				case SCREENSHOT:

					final Evidence evidence = new Evidence();

					final Screenshot screenshot = (Screenshot) artefact;
					evidence.setData(screenshot.getArtefact());
					// FIXME: set filetype on the fly
					evidence.setFilename(
							String.format("%s-%s.%s", stepreport.getDescription().replace(Convert.BLANK_STRING, "_"),
									Convert.getDate(new Date()), "png"));

					// FIXME: better to use an enum here!
					evidence.setContentType("image/png");

					step.setEvidences(new Evidence[] { evidence });
					break;

				default:
					if (UtilsXray.logger.isDebugEnabled()) {
						UtilsXray.logger
								.debug(String.format("the type \"%s\" has not been implemented yet.", type.getValue()));
					}
				}
			}

			steps.add(step);
		}

		if (steps.size() != stepreports.size()) {
			UtilsXray.logger.error("a previous error happend, will return an empty list.");
			steps.clear();
		}

		final Step[] result = UtilsCollection.toArray(Step.class, steps);

		return result;
	}

}
