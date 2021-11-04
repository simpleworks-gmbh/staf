package de.simpleworks.staf.module.jira.util.linkedissues;

import de.simpleworks.staf.module.jira.util.consts.linkedissues.LinkedIssuesDescriptionValue;
import de.simpleworks.staf.module.jira.util.consts.linkedissues.LinkedIssuesNamesValue;

public class IsDuplicatedBy extends LinkedIssueType {

	public IsDuplicatedBy() {
		super(LinkedIssuesNamesValue.DUPLICATE, LinkedIssuesDescriptionValue.IS_DUPLICATED_BY, Direction.INBOUND);
	}
}
