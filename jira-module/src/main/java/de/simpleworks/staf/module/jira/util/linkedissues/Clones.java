package de.simpleworks.staf.module.jira.util.linkedissues;

import de.simpleworks.staf.module.jira.util.consts.linkedissues.LinkedIssuesDescriptionValue;
import de.simpleworks.staf.module.jira.util.consts.linkedissues.LinkedIssuesNamesValue;

public class Clones extends LinkedIssueType {

	public Clones() {
		super(LinkedIssuesNamesValue.CLONERS, LinkedIssuesDescriptionValue.CLONES, Direction.OUTBOUND);
	}
}
