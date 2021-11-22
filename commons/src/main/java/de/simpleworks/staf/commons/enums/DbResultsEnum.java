package de.simpleworks.staf.commons.enums;

import java.util.List;

import de.simpleworks.staf.commons.consts.DbResultsValue;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.IEnum;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public enum DbResultsEnum implements IEnum {
	QUEUED("QUEUED", DbResultsValue.QUEUED),;

	final private String name;
	final private String value;

	DbResultsEnum(final String name, final String value) {
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
		return UtilsCollection.toList(DbResultsEnum.values());
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s]", Convert.getClassName(DbResultsEnum.class),
				UtilsFormat.format("name", name), UtilsFormat.format("value", value));
	}
}
