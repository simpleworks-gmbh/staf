package de.simpleworks.staf.module.kafka.enums;

import java.util.List;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.IEnum;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.commons.utils.UtilsFormat;
import de.simpleworks.staf.module.kafka.consts.TimestampAllowedValueValue;

public enum TimestampAllowedValueEnum implements IEnum {
	UNKNOWN("UNKNOWN", TimestampAllowedValueValue.UNKNOWN),EXACT_TIME("EXACT_TIME", TimestampAllowedValueValue.EXACT_TIME),
	BEFORE_TIME("BEFORE_TIME", TimestampAllowedValueValue.BEFORE_TIME), AFTER_TIME("AFTER_TIME", TimestampAllowedValueValue.AFTER_TIME);

	final private String name;
	final private String value;

	TimestampAllowedValueEnum(final String name, final String value) {
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
		return UtilsCollection.toList(TimestampAllowedValueEnum.values());
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s]", Convert.getClassName(TimestampAllowedValueEnum.class),
				UtilsFormat.format("name", name), UtilsFormat.format("value", value));
	}
}
