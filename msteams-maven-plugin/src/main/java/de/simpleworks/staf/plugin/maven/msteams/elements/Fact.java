package de.simpleworks.staf.plugin.maven.msteams.elements;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.data.exception.InvalidDataConstellationExcpetion;
import de.simpleworks.staf.data.utils.Data;

public class Fact extends Data {

	private static final Logger logger = LogManager.getLogger(Fact.class);

	private String name;
	private String value;

	public Fact() {
		name = Convert.EMPTY_STRING;
		value = Convert.EMPTY_STRING;
	}

	@Override
	public void validate() throws InvalidDataConstellationExcpetion {
		if (Fact.logger.isTraceEnabled()) {
			Fact.logger.trace("validate instance of class Fact..");
		}

		if (Convert.isEmpty(name)) {
			throw new InvalidDataConstellationExcpetion("name can't be null or empty string");
		}

		if (Convert.isEmpty(value)) {
			throw new InvalidDataConstellationExcpetion("value can't be null or empty string");
		}

	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}

}
