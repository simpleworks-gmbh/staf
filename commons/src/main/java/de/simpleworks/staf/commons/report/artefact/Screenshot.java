package de.simpleworks.staf.commons.report.artefact;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import de.simpleworks.staf.commons.enums.ArtefactEnum;

public class Screenshot extends Artefact<String> {

	public Screenshot(final TakesScreenshot screenshot) {
		if (screenshot == null) {
			throw new IllegalArgumentException("screenshot can't be null.");
		}

		this.artefact = screenshot.getScreenshotAs(OutputType.BASE64);
		this.type = ArtefactEnum.SCREENSHOT;
	}

	@Override
	public String getArtefact() {
		return this.artefact;
	}
}
