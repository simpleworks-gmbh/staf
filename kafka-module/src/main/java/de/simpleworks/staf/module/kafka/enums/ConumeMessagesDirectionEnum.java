package de.simpleworks.staf.module.kafka.enums;

import java.util.List;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.IEnum;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.commons.utils.UtilsFormat;
import de.simpleworks.staf.module.kafka.consts.ConumeMessagesDirectionValue;

public enum ConumeMessagesDirectionEnum implements IEnum {
	ASCENDING("ASCENDING", ConumeMessagesDirectionValue.ASCENDING),
	DESCENDING("DESCENDING", ConumeMessagesDirectionValue.DESCENDING);

	final private String name;
	final private String value;

	ConumeMessagesDirectionEnum(final String name, final String value) {
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
		return UtilsCollection.toList(ConumeMessagesDirectionEnum.values());
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s]", Convert.getClassName(ConumeMessagesDirectionEnum.class),
				UtilsFormat.format("name", name), UtilsFormat.format("value", value));
	}
}
