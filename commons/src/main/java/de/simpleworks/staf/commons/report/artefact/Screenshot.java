package de.simpleworks.staf.commons.report.artefact;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import de.simpleworks.staf.commons.enums.ArtefactEnum;
import de.simpleworks.staf.commons.utils.Convert;


public class Screenshot extends Artefact<String> {
	private static final Logger logger = LogManager.getLogger(Screenshot.class);

	public Screenshot(final TakesScreenshot screenshot) {
		if (screenshot == null) {
			throw new IllegalArgumentException("screenshot can't be null.");
		}

		String encodedString = Convert.EMPTY_STRING;

		try {
			File SrcFile = screenshot.getScreenshotAs(OutputType.FILE);
			byte[] fileContent = FileUtils.readFileToByteArray(SrcFile);
			encodedString = Base64.getEncoder().encodeToString(fileContent);
		} catch (IOException ex) {
			Screenshot.logger.error(
					"can't create Base64 String from the Screenshot, will return empty string, as artefact.", ex);
		}

		this.artefact = encodedString;
		this.type = ArtefactEnum.SCREENSHOT;
	}

	@Override
	public String getArtefact() {
		return this.artefact;
	}
}
