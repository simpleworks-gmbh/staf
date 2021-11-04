package de.simpleworks.staf.module.jira.util.enums.linkedissues;

import java.util.List;

import de.simpleworks.staf.commons.utils.IEnum;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.module.jira.util.consts.linkedissues.LinkedIssuesDescriptionValue;

public enum LinkedIssuesDescription implements IEnum {

	BLOCKS("blocks", LinkedIssuesDescriptionValue.BLOCKS),
	IS_BLOCKED_BY("is blocked by", LinkedIssuesDescriptionValue.IS_BLOCKED_BY),

	CLONES("clones", LinkedIssuesDescriptionValue.CLONES),
	IS_CLONED_BY("is cloned by", LinkedIssuesDescriptionValue.IS_CLONED_BY),

	DUPLICATES("duplicates", LinkedIssuesDescriptionValue.DUPLICATES),
	IS_DUPLICATED_BY("is duplicated by", LinkedIssuesDescriptionValue.IS_DUPLICATED_BY),

	RELATES_TO("relates to", LinkedIssuesDescriptionValue.RELATES_TO);

	final private String name;
	final private String value;

	LinkedIssuesDescription(final String name, final String value) {
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
		return UtilsCollection.toList(LinkedIssuesDescription.values());
	}

	@Override
	public String toString() {
		return String.format("Instance of Class: %s, %s, %s", getClass().toString(), name, value);
	}
}
