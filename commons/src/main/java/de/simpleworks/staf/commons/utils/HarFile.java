package de.simpleworks.staf.commons.utils;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.report.artefact.Artefact;

public class HarFile extends Artefact<String> {
	private static final Logger logger = LogManager.getLogger(HarFile.class);

	private final String content;

	public HarFile(final File harFile) {
		if (harFile == null) {
			throw new IllegalArgumentException("harFile can't be null.");
		}

		String fileContent = Convert.EMPTY_STRING;
		try {
			fileContent = UtilsIO.getAllContentFromFile(harFile);
		} catch (final Exception ex) {
			final String msg = String.format("can't fetch content of harFile at '%s'.", harFile.getAbsolutePath());
			HarFile.logger.error(msg, ex);
			// FIXME throw an exception.
		}

		this.content = fileContent;
	}

	@Override
	public String getArtefact() {
		return this.content;
	}
}
