package de.simpleworks.staf.commons.api;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.enums.AllowedValueEnum;
import de.simpleworks.staf.commons.enums.ValidateMethodEnum;
import de.simpleworks.staf.commons.interfaces.IPojo;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public class Assertion implements IPojo {

	private static final Logger logger = LogManager.getLogger(Assertion.class);

	private String id;
	private String xpath;
	private String jsonpath;
	private String responsebodyfile;
	private String headername;
	private String attribute;
	private AllowedValueEnum allowedValue;
	private ValidateMethodEnum validateMethod;
	private String value;

	public Assertion() {
		id = Convert.EMPTY_STRING;
		xpath = Convert.EMPTY_STRING;
		jsonpath = Convert.EMPTY_STRING;
		responsebodyfile = Convert.EMPTY_STRING;
		headername = Convert.EMPTY_STRING;
		attribute = Convert.EMPTY_STRING;
		allowedValue = AllowedValueEnum.NON_EMPTY;
		validateMethod = ValidateMethodEnum.UNKNOWN;
		value = Convert.EMPTY_STRING;
	}

	public String getId() {
		return id;
	}

	public String getXpath() {
		return xpath;
	}

	public String getJsonpath() {
		return jsonpath;
	}

	public String getResponsebodyfile() {
		return responsebodyfile;
	}

	public File getResponsebodyAsFile() {
		return new File(responsebodyfile);
	}

	public String getHeadername() {
		return headername;
	}

	public String getAttribute() {
		return attribute;
	}

	public AllowedValueEnum getAllowedValue() {
		return allowedValue;
	}

	public ValidateMethodEnum getValidateMethod() {
		return validateMethod;
	}

	public String getValue() {
		return value;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public void setXpath(final String xpath) {
		this.xpath = xpath;
	}

	public void setJsonpath(final String jsonpath) {
		this.jsonpath = jsonpath;
	}

	public void setResponsebodyfile(String responsebodyfile) {
		this.responsebodyfile = responsebodyfile;
	}

	public void setHeadername(String headername) {
		this.headername = headername;
	}

	public void setAttribute(final String attribute) {
		this.attribute = attribute;
	}

	public void setAllowedValue(final AllowedValueEnum allowedValue) {
		this.allowedValue = allowedValue;
	}

	public void setValidateMethod(final ValidateMethodEnum validateMethod) {
		this.validateMethod = validateMethod;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	@Override
	public boolean validate() {
		if (Assertion.logger.isDebugEnabled()) {
			Assertion.logger.debug(String.format("validate: '%s'.", toString()));
		}

		boolean result = true;

		if (Convert.isEmpty(id)) {
			Assertion.logger.error("id can't be null or empty string.");
			result = false;
		}

		if (allowedValue == null) {
			Assertion.logger.error("allowedValue can't be null.");
			result = false;
		}

		if (validateMethod == null) {
			Assertion.logger.error("validateMethod can't be null.");
			result = false;
		} else {
			switch (validateMethod) {
			case UNKNOWN:
				result = true;
				break;
			case XPATH:
				if (Convert.isEmpty(xpath)) {
					Assertion.logger.error("xpath can't be null or empty string.");
					result = false;
				}
				break;
			case JSONPATH:
				if (Convert.isEmpty(jsonpath)) {
					Assertion.logger.error("jsonpath can't be null or empty string.");
					result = false;
				}
				
				if (allowedValue.equals(AllowedValueEnum.REGEX)) {
					if (Convert.isEmpty(value)) {
						Assertion.logger.error(String.format("value can't be null or empty string, if allowedValue is %s.", AllowedValueEnum.REGEX.getValue()));
						result = false;
					}
				}
				
				break;
			case HEADER:
				if (Convert.isEmpty(headername)) {
					Assertion.logger.error("headername can't be null or empty string.");
					result = false;
				}
				break;
			case FILE_COMPARER:
				if (Convert.isEmpty(value)) {
					Assertion.logger.error("value can't be null or empty string.");
					result = false;
				}
				break;
			case DB_RESULT:
				if (allowedValue.equals(AllowedValueEnum.NON_EMPTY)) {
					if (Convert.isEmpty(value)) {
						Assertion.logger.error("value can't be null or empty string.");
						result = false;
					}
				}
				break;
			case RESPONSEBODY:

				if (!Convert.isEmpty(responsebodyfile)) {
					File file = new File(responsebodyfile);

					if (!file.exists()) {
						Assertion.logger.error(String.format("the file at '%s does not exist.", responsebodyfile));
						result = false;
					}
				} else {
					if (Convert.isEmpty(value)) {
						Assertion.logger.error("value can't be null or empty string, if responsebodyfile is not set.");
						result = false;
					}
				}

			case KAFKAMESSAGE_VALIDATION:
				if (allowedValue.equals(AllowedValueEnum.EXACT_VALUE)) {
					if (Convert.isEmpty(jsonpath)) {
						Assertion.logger.error("jsonpath can't be null or empty string.");
						result = false;
					}
				}
				
				break;
			default:
				Assertion.logger
						.error(String.format("validateMethod '%s' not implemented yet.", validateMethod.getValue()));
				result = false;
				break;
			}

		}return result;

	}

	@Override
	public boolean equals(Object obj) {

		if (!(obj instanceof Assertion)) {
			return false;
		}

		Assertion assertion = (Assertion) obj;

		try {

			if (!id.equals(assertion.getId())) { 
				return false;
			}

			if (!xpath.equals(assertion.getXpath())) { 
				return false;
			}

			if (!jsonpath.equals(assertion.getJsonpath())) { 
				return false;
			}

			if (!headername.equals(assertion.getHeadername())) { 
				return false;
			}

			if (!attribute.equals(assertion.getAttribute())) {  
				return false;
			}

			if (!allowedValue.equals(assertion.getAllowedValue())) { 
						assertion.getAllowedValue();
				return false;
			}

			if (!validateMethod.equals(assertion.getValidateMethod())) { 
				return false;
			}

			
			if(Convert.isEmpty(value)) {
				if (!Convert.isEmpty(assertion.getValue())) { 
					return false;
				}
			}
			else {
				if (!value.equals(assertion.getValue())) { 
					return false;
				}
			}
			
		} catch (Exception ex) {
			Assertion.logger.error("can't compare assertions.", ex);
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s, %s, %s, %s, %s, %s, %s, %s]", Convert.getClassName(Assertion.class),
				UtilsFormat.format("id", id), UtilsFormat.format("xpath", xpath),
				UtilsFormat.format("jsonpath", jsonpath), UtilsFormat.format("responsebodyfile", responsebodyfile),
				UtilsFormat.format("attribute", attribute), UtilsFormat.format("headername", headername),
				UtilsFormat.format("allowedValue", allowedValue == null ? null : allowedValue.getValue()),
				UtilsFormat.format("validateMethod", validateMethod == null ? null : validateMethod.getValue()),
				UtilsFormat.format("value", value));
	}

}
