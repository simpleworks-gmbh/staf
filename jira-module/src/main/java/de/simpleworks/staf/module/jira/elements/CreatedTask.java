package de.simpleworks.staf.module.jira.elements;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.utils.Convert;

public class CreatedTask extends Task {
	private static final long serialVersionUID = -3337416780572256807L;

	private final Logger logger = LogManager.getLogger(CreatedTask.class);

	@Override
	public void validate() throws SystemException {
		if (logger.isInfoEnabled()) {
			logger.info(String.format("validate %s..", this));
		}

		final String projectKey = getProjectKey();
		if (Convert.isEmpty(projectKey)) {
			throw new SystemException("projectKey can't be empty.");
		}

		final String summary = getSummary();
		if (Convert.isEmpty(summary)) {
			throw new SystemException("summary can't be empty.");
		}
	}

	@Override
	public boolean checkLabels() throws SystemException {
		// TODO Auto-generated method stub
		return false;
	}
}