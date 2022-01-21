package de.simpleworks.staf.plugin.maven.msteams.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.enums.ArtefactEnum;
import de.simpleworks.staf.commons.enums.Result;
import de.simpleworks.staf.commons.report.StepReport;
import de.simpleworks.staf.commons.report.TestcaseReport;
import de.simpleworks.staf.commons.report.artefact.Artefact;
import de.simpleworks.staf.commons.report.artefact.Screenshot;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.data.exception.InvalidDataConstellationExcpetion;
import de.simpleworks.staf.plugin.maven.msteams.elements.Fact;
import de.simpleworks.staf.plugin.maven.msteams.elements.HeroImage;
import de.simpleworks.staf.plugin.maven.msteams.elements.Section;
import net.minidev.json.JSONObject;

public class UtilsMsTeams {

	private static final Logger logger = LogManager.getLogger(UtilsMsTeams.class);

	/**
	 * @brief method to convert the {@param report} to an instance of Section
	 * @param {@param TestcaseReport} report
	 * @return converted Section instance, null if an error happens
	 */
	public static Section convert(final TestcaseReport report) {

		if (report == null) {
			throw new IllegalArgumentException("report can't be null.");
		}

		if (!report.validate()) {
			throw new IllegalArgumentException(String.format("report \"%s\" is invalid.", report));
		}

		Section result = new Section();

		try {
			result.setActivityTitle(report.getId());
			result.setActivitySubtitle(report.getResult().getValue());

			final List<Fact> facts = new ArrayList<>();

			for (final StepReport stepreport : report.getSteps()) {
				final Fact fact = new Fact();

				if (!Result.SUCCESSFULL.equals(stepreport.getResult())) {

					if (stepreport.getError() == null) {
						UtilsMsTeams.logger.error("error can't be null, will return the failed result value.");
						continue;
					}

					@SuppressWarnings("rawtypes")
					final Artefact artefact = stepreport.getArtefact();
					if (artefact != null) {

						if (ArtefactEnum.SCREENSHOT.equals(artefact.getType())) {

							final Screenshot screenshot = (Screenshot) artefact;
							final String base64ConvertedArtefact = screenshot.getArtefact();
							final String convertedHeroImage = String.format("data:image/png;base64,%s",
									base64ConvertedArtefact);

							final HeroImage heroimage = new HeroImage();
							heroimage.setImage(convertedHeroImage);

							result.setHeroImage(heroimage);
						} else {
							if (UtilsMsTeams.logger.isDebugEnabled()) {
								UtilsMsTeams.logger.debug(String.format("artefact type %s is not implemented yet.",
										artefact.getType().getValue()));
							}
						}
					}
				}

				fact.setName(stepreport.getDescription());
				fact.setValue(stepreport.getResult().getValue());

				facts.add(fact);

			}

			result.setFacts(UtilsCollection.toArray(Fact.class, facts));

			result.validate();

		} catch (final Exception ex) {
			UtilsMsTeams.logger.error(String.format("can't convert section from report %s.", report), ex);
			result = null;
		}

		return result;
	}

	/**
	 * @brief method to convert the {@param fact} to an instance of Fact
	 * @param {@param Fact} fact
	 * @return converted Fact instance, null if an error happens
	 */
	private static JSONObject convert(final Fact fact) {

		if (fact == null) {
			throw new IllegalArgumentException("fact can't be null.");
		}

		try {
			fact.validate();
		} catch (final InvalidDataConstellationExcpetion ex) {
			UtilsMsTeams.logger.error(String.format("fact %s is invalid.", fact), ex);
			return null;
		}

		final JSONObject result = new JSONObject();

		result.put("name", fact.getName());
		result.put("value", fact.getValue());

		return result;
	}

	public static JSONObject convert(final Section section) {

		if (section == null) {
			throw new IllegalArgumentException("section can't be null.");
		}

		try {
			section.validate();
		} catch (final InvalidDataConstellationExcpetion ex) {
			UtilsMsTeams.logger.error(String.format("section %s is invalid.", section), ex);
			return null;
		}

		final JSONObject result = new JSONObject();

		result.put("activityTitle", section.getActivityTitle());
		result.put("activityText", section.getActivityText());
		result.put("activitySubtitle", section.getActivitySubtitle());
		result.put("markdown", Boolean.valueOf(section.isMarkdown()));

		final List<JSONObject> factObjects = UtilsCollection.toList(section.getFacts()).stream()
				.map(UtilsMsTeams::convert).collect(Collectors.toList());

		result.put("facts", UtilsCollection.toArray(JSONObject.class, factObjects));
		return result;
	}

}