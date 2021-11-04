package de.simpleworks.staf.plugin.maven.msteams.elements;

import de.simpleworks.staf.commons.utils.Convert;

public class HeroImage {

	private String image;

	public HeroImage() {
		image = Convert.EMPTY_STRING;
	}

	public String getImage() {
		return image;
	}

	public void setImage(final String image) {
		this.image = image;
	}
}
