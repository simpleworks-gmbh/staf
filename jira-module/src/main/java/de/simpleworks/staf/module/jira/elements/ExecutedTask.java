package de.simpleworks.staf.module.jira.elements;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.utils.UtilsDate;

public class ExecutedTask extends Task {
	private static final long serialVersionUID = -3337416780572256807L;

	private final Logger logger = LogManager.getLogger(ExecutedTask.class);

	private final Task task;

	// TODO: build proper alternativ to "Copy Constructor"
	public ExecutedTask(final Task task) {
		if (task == null) {
			throw new IllegalArgumentException("task can't be null.");
		}

		setDescription(task.getDescription());
		setSummary(task.getSummary());
		setKey(task.getKey());
		setProjectKey(task.getProjectKey());
		setLabels(task.getLabels());

		this.task = task;
	}

	@Override
	public void validate() throws SystemException {
		if (logger.isInfoEnabled()) {
			logger.info(String.format("validate %s..", this));
		}

		this.task.validate();
	}

	@Override
	public String getSummary() {

		final String executionSummary = String.format("Execution of \"%s\" at \"%s\"", super.getSummary(),
				UtilsDate.getCurrentTime());

		return executionSummary;
	}

	@Override
	public boolean checkLabels() throws SystemException {
		return this.task.checkLabels();
	}
}