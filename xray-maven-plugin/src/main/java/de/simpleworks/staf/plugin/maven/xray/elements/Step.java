package de.simpleworks.staf.plugin.maven.xray.elements;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.plugin.maven.xray.enums.StatusEnum;

public class Step {
	private String id;
	private String action;
	private StatusEnum status;
	private String data;
	private String actualResult;
	private Evidence[] evidences;

	public Step() {
		this.status = StatusEnum.TODO;
		this.actualResult = Convert.EMPTY_STRING;
		this.evidences = new Evidence[0];
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getAction() {
		return action;
	}

	public void setAction(final String action) {
		this.action = action;
	}

	public StatusEnum getStatus() {
		return status;
	}

	public void setStatus(final StatusEnum status) {
		this.status = status;
	}

	public String getData() {
		return data;
	}

	public void setData(final String data) {
		this.data = data;
	}

	public String getActualResult() {
		return actualResult;
	}

	public void setActualResult(final String actualResult) {
		this.actualResult = actualResult;
	}

	public Evidence[] getEvidences() {
		return evidences;
	}

	public void setEvidences(final Evidence[] evidences) {
		this.evidences = evidences;
	}

}
