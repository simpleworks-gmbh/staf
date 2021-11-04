package de.simpleworks.staf.module.jira.util.linkedissues;

import de.simpleworks.staf.module.jira.util.consts.linkedissues.LinkedIssuesDescriptionValue;
import de.simpleworks.staf.module.jira.util.consts.linkedissues.LinkedIssuesNamesValue;

public class IsBlockedBy extends LinkedIssueType {

	public IsBlockedBy() {
		super(LinkedIssuesNamesValue.BLOCKS, LinkedIssuesDescriptionValue.IS_CLONED_BY, Direction.INBOUND);
	}
}
