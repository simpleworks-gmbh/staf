package de.simpleworks.staf.framework.enums;

import java.util.List;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.IEnum;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.commons.utils.UtilsFormat;
import de.simpleworks.staf.framework.consts.CreateArtefactValue;

public enum CreateArtefactEnum implements IEnum {
	ON_FAILURE("ON_FAILURE", CreateArtefactValue.ON_FAILURE), ON_SUCCESS("ON_SUCCESS", CreateArtefactValue.ON_SUCCESS),
	EVERYTIME("EVERYTIME ", CreateArtefactValue.EVERYTIME);

	private final String name;
	private final String value;

	CreateArtefactEnum(final String name, final String value) {
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
		return UtilsCollection.toList(CreateArtefactEnum.values());
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s].", Convert.getClassName(CreateArtefactEnum.class),
				UtilsFormat.format("name", name), UtilsFormat.format("value", value));
	}
}
