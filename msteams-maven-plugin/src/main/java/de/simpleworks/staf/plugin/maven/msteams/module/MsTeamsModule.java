package de.simpleworks.staf.plugin.maven.msteams.module;

import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import de.simpleworks.staf.plugin.maven.msteams.consts.MsTeamsConsts;
import de.simpleworks.staf.plugin.maven.msteams.utils.MsTeamsProperties;
import okhttp3.OkHttpClient;

public class MsTeamsModule extends AbstractModule {

	private static final Logger logger = LogManager.getLogger(MsTeamsModule.class);

	private final MsTeamsProperties properties;

	public MsTeamsModule() {
		properties = MsTeamsProperties.getInstance();
	}

	@Override
	protected void configure() {
		try {
			bind(URL.class).annotatedWith(Names.named(MsTeamsConsts.TEAMS_WEBHOOK)).toInstance(properties.getWebhook());

			bind(OkHttpClient.class).toInstance(MsTeamsModule.getOkHttpClient());

		} catch (final Exception ex) {
			final String msg = String.format("Cannot configure Testcase, due \"%s\".", ex);
			MsTeamsModule.logger.error(msg);
			throw new RuntimeException(msg);
		}
	}

	private static OkHttpClient getOkHttpClient() {
		final OkHttpClient result = new OkHttpClient.Builder().build();
		return result;
	}

}
