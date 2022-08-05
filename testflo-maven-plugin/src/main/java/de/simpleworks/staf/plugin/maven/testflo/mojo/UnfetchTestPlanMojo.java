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

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.plugin.maven.testflo.commons.TestFlo;
import de.simpleworks.staf.plugin.maven.testflo.commons.enums.TestPlanStatus;
import de.simpleworks.staf.plugin.maven.testflo.consts.Consts;
import okhttp3.OkHttpClient;

@Mojo(name = "unfetchTestPlan", defaultPhase = LifecyclePhase.INITIALIZE)
public class UnfetchTestPlanMojo extends TestfloMojo {
	private static final Logger logger = LogManager.getLogger(UnfetchTestPlanMojo.class);
	@Inject
	private IssueRestClient clientJira;

	@Inject
	@Named(Consts.BASIC_AUTHENTICATED_CLIENT)
	private OkHttpClient clientHttp;

	@Parameter(property = "id", required = true)
	private String testPlanId;
	
	@Parameter(property = "moveTo", required = true)
	private TestPlanStatus moveTo;

	@Inject
	@Named(Consts.JIRA_REST_TMS)
	private URL urlTms;

	private TestFlo testFlo;

	protected UnfetchTestPlanMojo() {
		super();
	}

	protected UnfetchTestPlanMojo(final String testPlanId, final TestPlanStatus moveTo, final URL urlTms) {
		this();

		this.testPlanId = testPlanId;
		this.urlTms = urlTms;
	}

	private void init() {
		if (UnfetchTestPlanMojo.logger.isDebugEnabled()) {
			UnfetchTestPlanMojo.logger.debug(String.format("testPlanId: '%s'.", testPlanId));
			UnfetchTestPlanMojo.logger.debug("clients:");
			UnfetchTestPlanMojo.logger.debug(String.format("urlTms: '%s'.", urlTms));
			UnfetchTestPlanMojo.logger.debug(String.format("client: '%s'.", clientJira));
			UnfetchTestPlanMojo.logger.debug(String.format("clientHttp: '%s'.", clientHttp));
		}
		Assert.assertFalse("testPlanId can't be null or empty string.", Convert.isEmpty(testPlanId));
		Assert.assertNotNull("urlTms can't be null.", urlTms);
		Assert.assertNotNull("client can't be null.", clientJira);
		Assert.assertNotNull("clientHttp can't be null.", clientHttp);
		testFlo = new TestFlo(clientJira, clientHttp, urlTms);
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (UnfetchTestPlanMojo.logger.isInfoEnabled()) {
			UnfetchTestPlanMojo.logger.info(String.format("fetch test plan: '%s'.", testPlanId));
		}
		init();

		try { 
			
			switch(moveTo) {
			
			case Open:
				// reset testcases
				testFlo.reOpenTestplan(testPlanId);
				break;
		
			default: 			
				throw new IllegalArgumentException(String.format("moveTo '%s' is not implemented yet.", moveTo));
			}
			
		} catch (final Exception ex) {
			final String message = String.format("can't start test plan: '%s'.", testPlanId);
			UnfetchTestPlanMojo.logger.error(message, ex);
			throw new MojoExecutionException(message);
		}		
	}
}