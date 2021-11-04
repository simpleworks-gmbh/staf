package de.simpleworks.staf.commons.enums;

import java.util.List;

import de.simpleworks.staf.commons.consts.ResultValue;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.IEnum;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public enum Result implements IEnum {
	SUCCESSFULL("SUCCESSFULL", ResultValue.SUCCESSFULL), FAILURE("FAILURE", ResultValue.FAILURE),
	UNKNOWN("UNKNOWN", ResultValue.UNKNOWN);

	final private String name;
	final private String value;

	Result(final String name, final String value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public List<IEnum> getValues() {
		return UtilsCollection.toList(Result.values());
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s]", Convert.getClassName(Result.class), UtilsFormat.format("name", name),
				UtilsFormat.format("value", value));
	}
}
