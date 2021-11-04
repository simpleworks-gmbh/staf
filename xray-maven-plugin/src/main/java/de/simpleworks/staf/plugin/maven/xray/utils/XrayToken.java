package de.simpleworks.staf.plugin.maven.xray.utils;

public class XrayToken {

	private String bearerToken = "";

	public XrayToken(final String bearerToken) {
		this.bearerToken = bearerToken;
	}

	public String getBearerToken() {
		return bearerToken;
	}

	public void setBearerToken(final String bearerToken) {
		this.bearerToken = bearerToken;
	}
}