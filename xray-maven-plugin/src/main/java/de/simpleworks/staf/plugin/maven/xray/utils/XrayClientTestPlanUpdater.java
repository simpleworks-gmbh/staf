package de.simpleworks.staf.plugin.maven.xray.utils;

import java.net.URL;

import okhttp3.OkHttpClient;

public class XrayClientTestPlanUpdater {

	@SuppressWarnings("unused")
	private final URL baseurl;

	@SuppressWarnings("unused")
	private final OkHttpClient client;

	public XrayClientTestPlanUpdater(final URL baseurl, final OkHttpClient client) {

		if (baseurl == null) {
			throw new IllegalArgumentException("baseurl can't be null.");
		}

		this.baseurl = baseurl;

		if (client == null) {
			throw new IllegalArgumentException("client can't be null.");
		}

		this.client = client;
	}
}