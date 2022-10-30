package de.simpleworks.staf.plugin.maven.msteams.elements;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.data.exception.InvalidDataConstellationExcpetion;
import de.simpleworks.staf.data.utils.Data;

public class Section extends Data {

	private static final Logger logger = LogManager.getLogger(Section.class);

	private String activityTitle;
	private String activityText;
	private String activitySubtitle;
	private Fact[] facts;
	private HeroImage heroImage;
	private boolean markdown;

	public Section() {
		activityTitle = Convert.EMPTY_STRING;
		activityText = Convert.EMPTY_STRING;
		activitySubtitle = Convert.EMPTY_STRING;
		heroImage = null;
		facts = new Fact[0];
		markdown = true;
	}

	@Override
	public void validate() throws InvalidDataConstellationExcpetion {

		if (Section.logger.isTraceEnabled()) {
			Section.logger.trace("validate instance of class Section..");
		}

		if (Convert.isEmpty(activityTitle)) {
			throw new InvalidDataConstellationExcpetion("activityTitle can't be null or empty string");
		}

		if (Convert.isEmpty(activitySubtitle)) {
			throw new InvalidDataConstellationExcpetion("activitySubtitle can't be null or empty string");
		}

		for (final Fact fact : UtilsCollection.toList(facts)) {
			fact.validate();
		}
	}

	public String getActivityTitle() {
		return activityTitle;
	}

	public void setActivityTitle(final String activityTitle) {
		this.activityTitle = activityTitle;
	}

	public String getActivityText() {
		return activityText;
	}

	public void setActivityText(final String activityText) {
		this.activityText = activityText;
	}

	public String getActivitySubtitle() {
		return activitySubtitle;
	}

	public void setActivitySubtitle(final String activitySubtitle) {
		this.activitySubtitle = activitySubtitle;
	}

	public Fact[] getFacts() {
		return facts;
	}

	public void setFacts(final Fact[] facts) {
		this.facts = facts;
	}

	public boolean isMarkdown() {
		return markdown;
	}

	public void setMarkdown(final boolean markdown) {
		this.markdown = markdown;
	}

	public HeroImage getHeroImage() {
		return heroImage;
	}

	public void setHeroImage(final HeroImage heroImage) {
		this.heroImage = heroImage;
	}
}
