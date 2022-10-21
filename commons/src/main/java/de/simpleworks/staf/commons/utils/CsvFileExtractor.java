package de.simpleworks.staf.commons.utils;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.exceptions.SystemException;

public class CsvFileExtractor {
	
	private final static Logger logger = LogManager.getLogger(CsvFileExtractor.class);

	public static boolean createCsvFile(final File file, final String content) {
		if (file == null) {
			throw new IllegalArgumentException("file can't be null.");
		}
 
		boolean result = false;
		try {
			UtilsIO.putAllContentToFile(file, content);
			result = true;
		} catch (final SystemException ex) {
			CsvFileExtractor.logger.error("can't fetch ArtefactFile.", ex);
		}

		return result;
	}
}
