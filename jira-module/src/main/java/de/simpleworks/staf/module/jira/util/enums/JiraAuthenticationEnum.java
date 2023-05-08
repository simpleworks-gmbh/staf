package de.simpleworks.staf.module.jira.util.enums;

import java.util.List;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.IEnum;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.commons.utils.UtilsFormat;
import de.simpleworks.staf.module.jira.util.consts.JiraAuthenticationValue;

public enum JiraAuthenticationEnum implements IEnum {
	
	BASIC_AUTHENTICATED_CLIENT("BASIC_AUTHENTICATED_CLIENT", JiraAuthenticationValue.BASIC_AUTHENTICATED_CLIENT),
	BEARER_TOKEN_CLIENT("BEARER_TOKEN_CLIENT ", JiraAuthenticationValue.BEARER_TOKEN_CLIENT);

	private final String name;
	private final String value;

	JiraAuthenticationEnum(final String name, final String value) {
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
		return UtilsCollection.toList(JiraAuthenticationEnum.values());
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s].", Convert.getClassName(JiraAuthenticationEnum.class),
				UtilsFormat.format("name", name), UtilsFormat.format("value", value));
	}
}