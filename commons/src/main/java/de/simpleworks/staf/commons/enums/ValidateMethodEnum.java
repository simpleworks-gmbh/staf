package de.simpleworks.staf.commons.enums;

import java.util.List;

import de.simpleworks.staf.commons.consts.ValidateMethodValue;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.IEnum;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public enum ValidateMethodEnum implements IEnum {
	UNKNOWN("unkown", ValidateMethodValue.UNKNOWN), HEADER("HEADER", ValidateMethodValue.HEADER),
	XPATH("XPATH", ValidateMethodValue.XPATH), JSONPATH("JSONPATH", ValidateMethodValue.JSONPATH),
	DB_RESULT("DB_RESULT", ValidateMethodValue.DB_RESULT),
	FILE_COMPARER("FILE_COMPARER", ValidateMethodValue.FILE_COMPARER);

	final private String name;
	final private String value;

	ValidateMethodEnum(final String name, final String value) {
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
		return UtilsCollection.toList(ValidateMethodEnum.values());
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s]", Convert.getClassName(ValidateMethodEnum.class),
				UtilsFormat.format("name", name), UtilsFormat.format("value", value));
	}
}
