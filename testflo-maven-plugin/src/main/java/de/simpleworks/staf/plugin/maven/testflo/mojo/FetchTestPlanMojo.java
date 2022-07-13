package de.simpleworks.staf.plugin.maven.testflo.mojo;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
 

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

import de.simpleworks.staf.commons.elements.TestPlan;
import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.mapper.elements.MapperTestplan;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsIO;
import de.simpleworks.staf.plugin.maven.testflo.commons.TestFlo;
import de.simpleworks.staf.plugin.maven.testflo.consts.Consts;
import okhttp3.OkHttpClient;

@Mojo(name = "fetchTestPlan", defaultPhase = LifecyclePhase.INITIALIZE)
public class FetchTestPlanMojo extends TestfloMojo {
	private static final Logger logger = LogManager.getLogger(FetchTestPlanMojo.class);
	
	@Inject
	private IssueRestClient clientJira;

	@Inject
	@Named(Consts.BASIC_AUTHENTICATED_CLIENT)
	private OkHttpClient clientHttp;

	@Parameter(property = "id", required = true)
	private String testPlanId;

	@Parameter(property = "file", required = true)
	private String fileName;

	@Parameter(property = "fixVersions")
	private List<String> fixVersions;

	@Parameter(property = "labels")
	private List<String> labels;

	@Parameter(property = "customFields")
	private List<String> customFields;

	@Parameter(property = "keepJiraLabel", defaultValue = "false")
	private boolean keepJiraLabel;
	
	@Inject
	@Named(Consts.JIRA_REST_TMS)
	private URL urlTms;

	private TestFlo testFlo;

	protected FetchTestPlanMojo() {
		super();
	}

	protected FetchTestPlanMojo(final String testPlanId, final String fileName, final URL urlTms) {
		this();
		if (Convert.isEmpty(testPlanId)) {
			throw new IllegalArgumentException("testPlanId can't be null or empty string.");
		}
		if (Convert.isEmpty(fileName)) {
			throw new IllegalArgumentException("fileName can't be null or empty string.");
		}
		this.testPlanId = testPlanId;
		this.fileName = fileName;
		this.urlTms = urlTms;
	}

	private void init() {
		if (FetchTestPlanMojo.logger.isDebugEnabled()) {
			FetchTestPlanMojo.logger.debug(String.format("testPlanId: '%s'.", testPlanId));
			FetchTestPlanMojo.logger.debug(String.format("fileName: '%s'.", fileName));
			FetchTestPlanMojo.logger.debug(String.format("fixVersions: '%s'.", String.join(",", fixVersions)));
			FetchTestPlanMojo.logger.debug("clients:");
			FetchTestPlanMojo.logger.debug(String.format("urlTms: '%s'.", urlTms));
			FetchTestPlanMojo.logger.debug(String.format("client: '%s'.", clientJira));
			FetchTestPlanMojo.logger.debug(String.format("clientHttp: '%s'.", clientHttp));
			FetchTestPlanMojo.logger.debug(String.format("keepJiraLabel: '%s'.", keepJiraLabel));
		}
		Assert.assertFalse("testPlanId can't be null or empty string.", Convert.isEmpty(testPlanId));
		Assert.assertFalse("fileName can't be null or empty string.", Convert.isEmpty(fileName));
		Assert.assertNotNull("urlTms can't be null.", urlTms);
		Assert.assertNotNull("client can't be null.", clientJira);
		Assert.assertNotNull("clientHttp can't be null.", clientHttp);
		testFlo = new TestFlo(clientJira, clientHttp, urlTms, keepJiraLabel);
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (FetchTestPlanMojo.logger.isInfoEnabled()) {
			FetchTestPlanMojo.logger.info(String.format("fetch test plan: '%s'.", testPlanId));
		}
		init();

		TestPlan testPlan = null;

		try {
			testFlo.moveTestPlanToNextIteration(testPlanId);
			testPlan = testFlo.readTestPlan(testPlanId);
			testFlo.startTestPlan(testPlan);

			final File file = new File(fileName);
			if (FetchTestPlanMojo.logger.isInfoEnabled()) {
				FetchTestPlanMojo.logger.info(String.format("write test plan into file: '%s'.", file));
			}
			UtilsIO.deleteFile(file);
			new MapperTestplan().write(file, Arrays.asList(testPlan));

		} catch (final SystemException ex) {
			final String message = String.format("can't start test plan: '%s'.", testPlanId);
			FetchTestPlanMojo.logger.error(message, ex);
			throw new MojoExecutionException(message);
		}

		try {
			if (!Convert.isEmpty(fixVersions)) {
				testFlo.addFixVersions(fixVersions, testPlan);
			}

			if (!Convert.isEmpty(labels)) {
				testFlo.addLabels(labels, testPlan);
			}

			if (!Convert.isEmpty(customFields)) {
				testFlo.addFields(customFields, testPlan);
			}
		} catch (Throwable th) {
			FetchTestPlanMojo.logger.error("can't set jira properties.", th);
		}
	}
}