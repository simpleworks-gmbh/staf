package de.simpleworks.staf.module.jira.util.linkedissues;

import de.simpleworks.staf.module.jira.util.consts.linkedissues.LinkedIssuesDescriptionValue;
import de.simpleworks.staf.module.jira.util.consts.linkedissues.LinkedIssuesNamesValue;

public class Duplicates extends LinkedIssueType {

	public Duplicates() {
		super(LinkedIssuesNamesValue.DUPLICATE, LinkedIssuesDescriptionValue.DUPLICATES, Direction.OUTBOUND);
	}
}
