package de.simpleworks.staf.plugin.maven.xray.enums;

import java.util.List;

import de.simpleworks.staf.commons.utils.IEnum;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.plugin.maven.xray.consts.StatusValue;

public enum StatusEnum implements IEnum {
	PASSED("PASSED", StatusValue.PASSED), FAILED("FAILED", StatusValue.FAILED),
	EXECUTING("EXECUTING", StatusValue.EXECUTING), TODO("TODO", StatusValue.TODO);

	private final String name;
	private final String value;

	StatusEnum(final String name, final String value) {
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
		return UtilsCollection.toList(StatusEnum.values());
	}

	@Override
	public String toString() {
		return String.format("Instance of Class: %s, %s, %s", StatusEnum.class.getName(), name, value);
	}
}
