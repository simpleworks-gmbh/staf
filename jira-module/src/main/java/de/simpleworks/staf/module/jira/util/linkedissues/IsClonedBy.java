package de.simpleworks.staf.module.jira.util.linkedissues;

import de.simpleworks.staf.module.jira.util.consts.linkedissues.LinkedIssuesDescriptionValue;
import de.simpleworks.staf.module.jira.util.consts.linkedissues.LinkedIssuesNamesValue;

public class IsClonedBy extends LinkedIssueType {

	public IsClonedBy() {
		super(LinkedIssuesNamesValue.CLONERS, LinkedIssuesDescriptionValue.IS_CLONED_BY, Direction.INBOUND);
	}
}
