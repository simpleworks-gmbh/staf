package de.simpleworks.staf.module.kafka.enums;

import java.util.List;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.IEnum;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.commons.utils.UtilsFormat;
import de.simpleworks.staf.module.kafka.consts.SerializerTypeValue;

public enum SerializerTypeEnum implements IEnum {
	UNKNOWN("UNKNOWN", SerializerTypeValue.UNKNOWN),
	LONG_SERIALIZER("LONG_SERIALIZER", SerializerTypeValue.LONG_SERIALIZER),
	STRING_SERIALIZER("STRING_SERIALIZER", SerializerTypeValue.STRING_SERIALIZER);

	final private String name;
	final private String value;

	SerializerTypeEnum(final String name, final String value) {
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
		return UtilsCollection.toList(SerializerTypeEnum.values());
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s]", Convert.getClassName(SerializerTypeEnum.class),
				UtilsFormat.format("name", name), UtilsFormat.format("value", value));
	}
}
