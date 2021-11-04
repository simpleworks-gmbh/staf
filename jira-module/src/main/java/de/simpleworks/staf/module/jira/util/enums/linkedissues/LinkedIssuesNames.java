package de.simpleworks.staf.module.jira.util.enums.linkedissues;

import java.util.List;

import de.simpleworks.staf.commons.utils.IEnum;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.module.jira.util.consts.linkedissues.LinkedIssuesNamesValue;

public enum LinkedIssuesNames implements IEnum {

	BLOCKS("Blocks", LinkedIssuesNamesValue.BLOCKS), CLONERS("Cloners", LinkedIssuesNamesValue.CLONERS),
	DUPLICATES("Duplicate", LinkedIssuesNamesValue.DUPLICATE), Relates("Relates", LinkedIssuesNamesValue.RELATES);

	final private String name;
	final private String value;

	LinkedIssuesNames(final String name, final String value) {
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
		return UtilsCollection.toList(LinkedIssuesNames.values());
	}

	@Override
	public String toString() {
		return String.format("Instance of Class: %s, %s, %s", getClass().toString(), name, value);
	}
}
