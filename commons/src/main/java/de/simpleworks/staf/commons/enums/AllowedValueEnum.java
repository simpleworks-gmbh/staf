
package de.simpleworks.staf.commons.enums;

import java.util.List;
import de.simpleworks.staf.commons.consts.AllowedValueValue;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.IEnum;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public enum AllowedValueEnum implements IEnum {
	UNKNOWN("UNKNOWN", AllowedValueValue.UNKNOWN), NOT("NOT", AllowedValueValue.NOT), MAX("MAX", AllowedValueValue.MAX),
	RANDOM("RANDOM", AllowedValueValue.RANDOM), EVERYTHING("EVERYTHING", AllowedValueValue.EVERYTHING),
	NON_EMPTY("NON_EMPTY", AllowedValueValue.NON_EMPTY),
	CONTAINS_VALUE("CONTAINS_VALUE", AllowedValueValue.CONTAINS_VALUE),
	ANY_ORDER("ANY_ORDER", AllowedValueValue.ANY_ORDER), STRICT_ORDER("STRICT_ORDER", AllowedValueValue.STRICT_ORDER),
	PART_OF("PART_OF", AllowedValueValue.PART_OF),
	EXACT_VALUE("EXACT_VALUE", AllowedValueValue.EXACT_VALUE),
	REGEX("REGEX", AllowedValueValue.REGEX),
	AMOUNT_EQUALS_TO("AMOUNT_EQUALS_TO", AllowedValueValue.AMOUNT_EQUALS_TO),
	AMOUNT_MORE_THAN("AMOUNT_MORE_THAN", AllowedValueValue.AMOUNT_MORE_THAN);

	final private String name;
	final private String value;

	AllowedValueEnum(final String name, final String value) {
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
		return UtilsCollection.toList(AllowedValueEnum.values());
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s]", Convert.getClassName(AllowedValueEnum.class),
				UtilsFormat.format("name", name), UtilsFormat.format("value", value));
	}
}