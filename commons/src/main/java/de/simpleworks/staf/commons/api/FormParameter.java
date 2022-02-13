package de.simpleworks.staf.commons.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.simpleworks.staf.commons.interfaces.IPojo;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public class FormParameter implements IPojo {
	private static final Logger logger = LogManager.getLogger(FormParameter.class);
	private String name;
	private String value;

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	@Override
	public boolean validate() {
		if (FormParameter.logger.isDebugEnabled()) {
			FormParameter.logger.debug(String.format("validate: '%s'.", toString()));
		}
		boolean result = true;
		if (Convert.isEmpty(name)) {
			FormParameter.logger.error("name can't be null or empty string.");
			result = false;
		}
		if (Convert.isEmpty(value)) {
			FormParameter.logger.error("value can't be null or empty string.");
			result = false;
		}
		return result;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s].", Convert.getClassName(FormParameter.class),
				UtilsFormat.format("name", name), UtilsFormat.format("value", value));
	}
}