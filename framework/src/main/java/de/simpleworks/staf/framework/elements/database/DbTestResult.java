package de.simpleworks.staf.framework.elements.database;

import java.util.Map;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;
import de.simpleworks.staf.framework.elements.commons.ATestResult;

public class DbTestResult extends ATestResult {

	private Map<String, String> extractedValues;

	public Map<String, String> getExtractedValues() {
		return extractedValues;
	}

	public void setExtractedValues(Map<String, String> extractedValues) {
		this.extractedValues = extractedValues;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s]", Convert.getClassName(DbTestResult.class),
				UtilsFormat.format("successfull", isSuccessfull()),
				UtilsFormat.format("errormessage", getErrormessage()),
				UtilsFormat.format("extractedValues", extractedValues));
	}
}
