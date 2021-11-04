package de.simpleworks.staf.module.jira.util.linkedissues;

import de.simpleworks.staf.module.jira.util.consts.linkedissues.LinkedIssuesDescriptionValue;
import de.simpleworks.staf.module.jira.util.consts.linkedissues.LinkedIssuesNamesValue;

public class Blockes extends LinkedIssueType {

	public Blockes() {
		super(LinkedIssuesNamesValue.BLOCKS, LinkedIssuesDescriptionValue.BLOCKS, Direction.OUTBOUND);
	}
}
