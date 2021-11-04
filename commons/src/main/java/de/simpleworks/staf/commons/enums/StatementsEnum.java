package de.simpleworks.staf.commons.enums;

import java.util.List;

import de.simpleworks.staf.commons.consts.StatementsValue;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.IEnum;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public enum StatementsEnum implements IEnum {
	QUERY("QUERY", StatementsValue.QUERY);

	final private String name;
	final private String value;

	StatementsEnum(final String name, final String value) {
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
		return UtilsCollection.toList(StatementsEnum.values());
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s]", Convert.getClassName(StatementsEnum.class),
				UtilsFormat.format("name", name), UtilsFormat.format("value", value));
	}
}
