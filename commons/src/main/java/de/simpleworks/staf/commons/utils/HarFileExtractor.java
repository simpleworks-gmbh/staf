package de.simpleworks.staf.commons.utils;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.exceptions.SystemException;

public class HarFileExtractor {
	private final static Logger logger = LogManager.getLogger(HarFileExtractor.class);

	public static boolean createHarFile(final File file, final String content) {
		if (file == null) {
			throw new IllegalArgumentException("file can't be null.");
		}

		if (Convert.isEmpty(content)) {
			throw new IllegalArgumentException("content can't be null or empty string.");
		}

		boolean result = false;
		try {
			UtilsIO.putAllContentToFile(file, content);
			result = true;
		} catch (final SystemException ex) {
			HarFileExtractor.logger.error("can't fetch HarFile.", ex);
		}

		return result;
	}
}
