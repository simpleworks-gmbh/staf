package de.simpleworks.staf.module.jira.util.enums;

import java.util.List;

import de.simpleworks.staf.commons.utils.IEnum;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.module.jira.util.consts.TaskLabelValue;

public enum TaskLabel implements IEnum {

	Testplan("Testplan", TaskLabelValue.Testplan), Testcase("Testcase", TaskLabelValue.Testcase),
	Teststep("Teststep", TaskLabelValue.Teststep), Testrun("Testrun", TaskLabelValue.Testrun),

	Succeeded("Succeeded", TaskLabelValue.Succeeded), Failed("Failed", TaskLabelValue.Failed),
	Unknown("Unknown", TaskLabelValue.Unknown);

	final private String name;
	final private String value;

	TaskLabel(final String name, final String value) {
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
		return UtilsCollection.toList(TaskLabel.values());
	}

	@Override
	public String toString() {
		return String.format("Instance of Class: %s, %s, %s", getClass().toString(), name, value);
	}
}
