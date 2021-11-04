package de.simpleworks.staf.module.jira.util.enums;

import java.util.List;

import de.simpleworks.staf.commons.utils.IEnum;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.module.jira.util.consts.StatusValue;

public enum Status implements IEnum {

	InProgress("In Progress", StatusValue.InProgress), InReview("In Review", StatusValue.InReview),
	ToDo("To Do", StatusValue.ToDo), Done("Done", StatusValue.Done);

	final private String name;
	final private String value;

	Status(final String name, final String value) {
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
		return UtilsCollection.toList(Status.values());
	}

	@Override
	public String toString() {
		return String.format("Instance of Class: %s, %s, %s", getClass().toString(), name, value);
	}
}
