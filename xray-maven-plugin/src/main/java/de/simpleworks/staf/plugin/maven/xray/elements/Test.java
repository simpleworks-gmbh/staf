package de.simpleworks.staf.plugin.maven.xray.elements;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.plugin.maven.xray.enums.StatusEnum;

public class Test {

	private String issueId;
	private String testKey;
	private String comment;
	private StatusEnum status;
	private Step[] steps;

	public Test() {
		this.testKey = Convert.EMPTY_STRING;
		this.comment = Convert.EMPTY_STRING;
		this.status = StatusEnum.TODO;
		this.steps = new Step[0];
	}

	public String getIssueId() {
		return issueId;
	}

	public void setIssueId(final String issueId) {
		this.issueId = issueId;
	}

	public String getTestKey() {
		return testKey;
	}

	public void setTestKey(final String testKey) {
		this.testKey = testKey;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(final String comment) {
		this.comment = comment;
	}

	public StatusEnum getStatus() {
		return status;
	}

	public void setStatus(final StatusEnum status) {
		this.status = status;
	}

	public Step[] getSteps() {
		return steps;
	}

	public void setSteps(final Step[] steps) {
		this.steps = steps;
	}
}
