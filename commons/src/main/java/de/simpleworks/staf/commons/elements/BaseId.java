package de.simpleworks.staf.commons.elements;

import de.simpleworks.staf.commons.utils.UtilsFormat;

public class BaseId {
	private String id;

	protected BaseId() {
		id = null;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return UtilsFormat.format("id", id);
	}
}
