package de.simpleworks.staf.plugin.maven.testflo.module;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.module.jira.util.JiraProperties;
import de.simpleworks.staf.module.jira.util.consts.ClientConsts;
import de.simpleworks.staf.plugin.maven.testflo.commons.enums.Configuration;
import de.simpleworks.staf.plugin.maven.testflo.commons.enums.ITestFloEnum;
import de.simpleworks.staf.plugin.maven.testflo.commons.enums.ITestFloEnumWithId;
import de.simpleworks.staf.plugin.maven.testflo.commons.enums.TestCaseStatus;
import de.simpleworks.staf.plugin.maven.testflo.commons.enums.TestCaseTransition;
import de.simpleworks.staf.plugin.maven.testflo.commons.enums.TestFloTypes;
import de.simpleworks.staf.plugin.maven.testflo.commons.enums.TestPlanStatus;
import de.simpleworks.staf.plugin.maven.testflo.commons.enums.TestPlanTransition;
import de.simpleworks.staf.plugin.maven.testflo.commons.enums.TestStepStatus;
import de.simpleworks.staf.plugin.maven.testflo.consts.Consts;
import de.simpleworks.staf.plugin.maven.testflo.utils.TestFLOProperties;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.OkHttpClient.Builder;

public class TestfloModule extends AbstractModule {
	private static final Logger logger = LogManager.getLogger(TestfloModule.class);

	private final JiraProperties jira;
	private final TestFLOProperties testFlo;

	public TestfloModule() {
		this.jira = JiraProperties.getInstance();
		this.testFlo = TestFLOProperties.getInstance();

		TestfloModule.checkEnums();
	}

	private static <E extends Enum<E> & ITestFloEnum> void checkEnum(final Class<E> clazz) {
		if (TestfloModule.logger.isDebugEnabled()) {
			TestfloModule.logger.debug(String.format("check enum: '%s'.", Convert.getClassFullName(clazz)));
		}

		for (final E e : EnumSet.allOf(clazz)) {
			final String name = e.name();
			if (TestfloModule.logger.isDebugEnabled()) {
				TestfloModule.logger.debug(String.format("name: '%s', testFlo: '%s'.", name, e.getTestFloName()));
			}
		}
	}

	private static <E extends Enum<E> & ITestFloEnumWithId> void checkEnumWithId(final Class<E> clazz) {
		if (TestfloModule.logger.isDebugEnabled()) {
			TestfloModule.logger
					.debug(String.format("enum: '%s' has following fields:", Convert.getClassFullName(clazz)));
		}

		for (final E e : EnumSet.allOf(clazz)) {
			final String name = e.name();
			if (TestfloModule.logger.isDebugEnabled()) {
				TestfloModule.logger.debug(String.format("name: '%s', testFloName: '%s', testFloId: %d.", name,
						e.getTestFloName(), Integer.valueOf(e.getTestFloId())));
			}
		}
	}

	private static void checkEnums() {
		Configuration.getInstance();

		if (TestfloModule.logger.isDebugEnabled()) {
			TestfloModule.logger.debug("check enums start..");
		}

		TestfloModule.checkEnum(TestFloTypes.class);

		TestfloModule.checkEnum(TestCaseStatus.class);
		TestfloModule.checkEnumWithId(TestCaseTransition.class);

		TestfloModule.checkEnum(TestPlanStatus.class);
		TestfloModule.checkEnumWithId(TestPlanTransition.class);

		TestfloModule.checkEnum(TestStepStatus.class);

		if (TestfloModule.logger.isDebugEnabled()) {
			TestfloModule.logger.debug("check enums DONE.");
		}
	}

	private static URL getUrl(final String url) throws SystemException {
		Assert.assertFalse("url can't be null or empty string.", Convert.isEmpty(url));

		try {
			return new URL(url);
		} catch (final MalformedURLException ex) {
			final String message = String.format("can't convert '%s' to URL.", url);
			TestfloModule.logger.error(message, ex);
			throw new SystemException(message);
		}
	}

	private void bindURL(final String name, final URL url) {
		Assert.assertFalse("name can't be null or empty string.", Convert.isEmpty(name));
		Assert.assertNotNull("url can't be null.", url);

		if (TestfloModule.logger.isDebugEnabled()) {
			TestfloModule.logger.debug(String.format("binding name: '%s' to value: '%s'.", name, url));
		}

		bind(URL.class).annotatedWith(Names.named(name)).toInstance(url);
	}

	private void bindURL(final String name, final String url) throws SystemException {
		bindURL(name, TestfloModule.getUrl(url));
	}

	private Builder getBuilder(int timeout) {

		if (timeout < 0) {
			throw new IllegalArgumentException(
					String.format("timeout can't be less than zero, but was \"%s\".", Integer.toString(timeout)));
		}

		Builder result = new Builder();

		result.connectTimeout(timeout * 1000, TimeUnit.MILLISECONDS).writeTimeout(timeout * 1000, TimeUnit.MILLISECONDS)
				.readTimeout(timeout * 1000, TimeUnit.MILLISECONDS);

		return result;
	}

	private OkHttpClient getBasicAuthHttpClient() {

		Builder builder = getBuilder(this.testFlo.getTimeout());

		final OkHttpClient result = builder.addInterceptor(chain -> {
			final Request request = chain.request();
			final Request authenticatedRequest = request.newBuilder()
					.header("Authorization", Credentials.basic(jira.getUsername(), jira.getPassword())).build();
			return chain.proceed(authenticatedRequest);
		}).build();

		return result;
	}

	@Override
	protected void configure() {
		try {
			bindURL(ClientConsts.URL, jira.getUrl());
			bindURL(Consts.JIRA_REST_API, testFlo.getApi());
			bindURL(Consts.JIRA_REST_TMS, testFlo.getTms());

			bind(OkHttpClient.class).annotatedWith(Names.named(Consts.BASIC_AUTHENTICATED_CLIENT))
					.toInstance(getBasicAuthHttpClient());
		} catch (final Exception ex) {
			final String msg = "can't configure -> stop work.";
			TestfloModule.logger.error(msg, ex);
			throw new RuntimeException(msg);
		}
	}
}
