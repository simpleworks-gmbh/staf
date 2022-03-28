package de.simpleworks.staf.plugin.maven.testflo.mojo;

import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.junit.Assert;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.module.jira.util.consts.ClientConsts;
import de.simpleworks.staf.plugin.maven.testflo.commons.TestFlo;
import de.simpleworks.staf.plugin.maven.testflo.consts.Consts;
import okhttp3.OkHttpClient;

@Mojo(name = "resetTestPlan", defaultPhase = LifecyclePhase.INITIALIZE)
public class ResetTestPlanMojo extends TestfloMojo {
	private static final Logger logger = LogManager.getLogger(ResetTestPlanMojo.class);
	@Inject
	private IssueRestClient clientJira;

	@Inject
	@Named(Consts.BASIC_AUTHENTICATED_CLIENT)
	private OkHttpClient clientHttp;

	@Parameter(property = "id", required = true)
	private String testPlanId;

	@Inject
	@Named(Consts.JIRA_REST_TMS)
	private URL urlTms;

	@Inject
	@Named(ClientConsts.URL)
	private URL jiraUrl;

	private TestFlo testFlo;

	protected ResetTestPlanMojo() {
		super();
	}

	protected ResetTestPlanMojo(final String testPlanId, final String fileName, final URL urlTms, final URL jiraUrl) {
		this();
		if (Convert.isEmpty(testPlanId)) {
			throw new IllegalArgumentException("testPlanId can't be null or empty string.");
		}
		if (Convert.isEmpty(fileName)) {
			throw new IllegalArgumentException("fileName can't be null or empty string.");
		}
		this.testPlanId = testPlanId;
		this.urlTms = urlTms;
		this.jiraUrl = jiraUrl;
	}

	private void init() {
		if (ResetTestPlanMojo.logger.isDebugEnabled()) {
			ResetTestPlanMojo.logger.debug(String.format("testPlanId: '%s'.", testPlanId));
			ResetTestPlanMojo.logger.debug("clients:");
			ResetTestPlanMojo.logger.debug(String.format("urlTms: '%s'.", urlTms));
			ResetTestPlanMojo.logger.debug(String.format("client: '%s'.", clientJira));
			ResetTestPlanMojo.logger.debug(String.format("clientHttp: '%s'.", clientHttp));
		}
		Assert.assertFalse("testPlanId can't be null or empty string.", Convert.isEmpty(testPlanId));
		Assert.assertNotNull("urlTms can't be null.", urlTms);
		Assert.assertNotNull("client can't be null.", clientJira);
		Assert.assertNotNull("clientHttp can't be null.", clientHttp);
		testFlo = new TestFlo(clientJira, clientHttp, urlTms, jiraUrl);
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (ResetTestPlanMojo.logger.isInfoEnabled()) {
			ResetTestPlanMojo.logger.info(String.format("fetch test plan: '%s'.", testPlanId));
		}
		init();

		try {
			testFlo.testPlanReset(testPlanId);
		} catch (SystemException ex) {
			final String message = String.format("can't reset test plan: '%s'.", testPlanId);
			ResetTestPlanMojo.logger.error(message, ex);
			throw new MojoExecutionException(message);
		}
	}
}