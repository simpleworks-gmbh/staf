package de.simpleworks.staf.module.jira.elements;

import de.simpleworks.staf.commons.exceptions.SystemException;

public class TestResult extends Task {
	private static final long serialVersionUID = 6340118395007198363L;

	@Override
	public boolean checkLabels() throws SystemException {
		return false;
	}
}
