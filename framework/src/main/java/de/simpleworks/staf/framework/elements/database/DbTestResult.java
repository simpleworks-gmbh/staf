package de.simpleworks.staf.framework.elements.database;

import java.util.Map;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public class DbTestResult {

	private boolean successfull;
	private String errormessage;

	private Map<String, String> extractedValues;

	public boolean isSuccessfull() {
		return successfull;
	}

	public void setSuccessfull(final boolean successfull) {
		this.successfull = successfull;
	}

	public String getErrormessage() {
		return errormessage;
	}

	public void setErrormessage(final String errormessage) {
		this.errormessage = errormessage;
	}

	public Map<String, String> getExtractedValues() {
		return extractedValues;
	}

	public void setExtractedValues(Map<String, String> extractedValues) {
		this.extractedValues = extractedValues;
	}

	@Override
	public String toString() {

		return String.format("[%s: %s, %s]", Convert.getClassName(DbTestResult.class),
				UtilsFormat.format("successfull", successfull), UtilsFormat.format("errormessage", errormessage),
				UtilsFormat.format("extractedValues", extractedValues));
	}
}
