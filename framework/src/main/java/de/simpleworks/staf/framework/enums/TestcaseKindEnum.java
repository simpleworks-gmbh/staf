package de.simpleworks.staf.framework.enums;

import java.util.List;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.IEnum;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.commons.utils.UtilsFormat;
import de.simpleworks.staf.framework.consts.TestcaseKindValue;

public enum TestcaseKindEnum implements IEnum {
	GUI_TESTCASE("GUI_TESTCASE", TestcaseKindValue.GUI_TESTCASE),
	API_TESTCASE("API_TESTCASE", TestcaseKindValue.API_TESTCASE);

	private final String name;
	private final String value;

	TestcaseKindEnum(final String name, final String value) {
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
		return UtilsCollection.toList(TestcaseKindEnum.values());
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s].", Convert.getClassName(TestcaseKindEnum.class),
				UtilsFormat.format("name", name), UtilsFormat.format("value", value));
	}
}
