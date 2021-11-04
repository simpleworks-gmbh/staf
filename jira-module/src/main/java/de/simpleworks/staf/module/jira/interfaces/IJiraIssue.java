package de.simpleworks.staf.module.jira.interfaces;

import java.io.Serializable;

//TODO: make IssueTypes configurable
public interface IJiraIssue extends Serializable {

	long getIssueType();
}