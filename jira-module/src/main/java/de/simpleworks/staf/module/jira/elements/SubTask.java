package de.simpleworks.staf.module.jira.elements;

import de.simpleworks.staf.module.jira.interfaces.IJiraIssue;

public interface SubTask extends IJiraIssue {

	@Override
	default long getIssueType() {
		// FIXME move it into configuration file.
		return 10102;
	}
}