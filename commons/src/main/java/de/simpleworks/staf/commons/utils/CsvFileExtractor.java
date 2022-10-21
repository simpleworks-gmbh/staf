package de.simpleworks.staf.commons.utils;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.exceptions.SystemException;

public class CsvFileExtractor {
	
	private final static Logger logger = LogManager.getLogger(CsvFileExtractor.class);

	private final static String NO_RESULT_ROW_WAS_FETCHED = "No result row was fecthed.";
	
	public static boolean createCsvFile(final File file, final String content) {
		if (file == null) {
			throw new IllegalArgumentException("file can't be null.");
		}
  
		try {
			
			if(Convert.isEmpty(content)) {
				UtilsIO.putAllContentToFile(file, NO_RESULT_ROW_WAS_FETCHED);
				return true;
			}
			
			UtilsIO.putAllContentToFile(file, content);	
		
			return true;
		} catch (final SystemException ex) {
			CsvFileExtractor.logger.error("can't fetch ArtefactFile.", ex);
		}

		return false;
	}
}
