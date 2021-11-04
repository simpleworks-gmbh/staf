package de.simpleworks.staf.plugin.maven.xray.elements;

import de.simpleworks.staf.commons.utils.Convert;

public class Evidence {

	private String data;
	private String filename;
	private String contentType;

	public Evidence() {
		this.data = Convert.EMPTY_STRING;
		this.filename = Convert.EMPTY_STRING;
		this.contentType = Convert.EMPTY_STRING;
	}

	public String getData() {
		return data;
	}

	public void setData(final String data) {
		this.data = data;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(final String filename) {
		this.filename = filename;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(final String contentType) {
		this.contentType = contentType;
	}

}
