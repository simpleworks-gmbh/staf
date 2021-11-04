package de.simpleworks.staf.module.jira.util.linkedissues;

import de.simpleworks.staf.module.jira.util.consts.linkedissues.LinkedIssuesDescriptionValue;
import de.simpleworks.staf.module.jira.util.consts.linkedissues.LinkedIssuesNamesValue;

public class RelatesTo extends LinkedIssueType {

	public RelatesTo() {
		super(LinkedIssuesNamesValue.RELATES, LinkedIssuesDescriptionValue.RELATES_TO, Direction.INBOUND);
	}
}