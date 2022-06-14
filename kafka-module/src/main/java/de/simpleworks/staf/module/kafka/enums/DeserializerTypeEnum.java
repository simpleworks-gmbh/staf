package de.simpleworks.staf.module.kafka.enums;

import java.util.List;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.IEnum;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.commons.utils.UtilsFormat;
import de.simpleworks.staf.module.kafka.consts.DeserializerTypeValue;

public enum DeserializerTypeEnum implements IEnum {
	UNKNOWN("UNKNOWN", DeserializerTypeValue.UNKNOWN),
	LONG_DESERIALIZER("LONG_DESERIALIZER", DeserializerTypeValue.LONG_DESERIALIZER),
	STRING_DESERIALIZER("STRING_DESERIALIZER", DeserializerTypeValue.STRING_DESERIALIZER);

	final private String name;
	final private String value;

	DeserializerTypeEnum(final String name, final String value) {
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
		return UtilsCollection.toList(DeserializerTypeEnum.values());
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s]", Convert.getClassName(DeserializerTypeEnum.class),
				UtilsFormat.format("name", name), UtilsFormat.format("value", value));
	}
}
