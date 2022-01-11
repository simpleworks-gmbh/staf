package de.simpleworks.staf.framework.elements.gui;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;
import de.simpleworks.staf.framework.elements.commons.ATestResult;

public class GUITestResult extends ATestResult {

	@Override
	public String toString() {
		return String.format("[%s: %s, %s]", Convert.getClassName(GUITestResult.class),
				UtilsFormat.format("successfull", isSuccessfull()),
				UtilsFormat.format("errormessage", getErrormessage()));
	}
}
