package de.simpleworks.staf.plugin.maven.testflo.mojo;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import de.simpleworks.staf.commons.elements.TestCase;
import de.simpleworks.staf.commons.elements.TestPlan;
import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.mapper.elements.MapperTestplan;
import de.simpleworks.staf.commons.mapper.report.MapperTestcaseReport;
import de.simpleworks.staf.commons.report.TestcaseReport;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.plugin.maven.testflo.commons.TestFlo;
import de.simpleworks.staf.plugin.maven.testflo.commons.enums.TestPlanStatus;
import de.simpleworks.staf.plugin.maven.testflo.commons.enums.TestPlanTransition;
import de.simpleworks.staf.plugin.maven.testflo.consts.Consts;
import edu.emory.mathcs.backport.java.util.Arrays;
import okhttp3.OkHttpClient;

@Mojo(name = "uploadTestResult", defaultPhase = LifecyclePhase.INITIALIZE)
public class UploadResultMojo extends TestfloMojo {
	private static final Logger logger = LogManager.getLogger(UploadResultMojo.class);

	@Inject
	private IssueRestClient clientJira;

	@Inject
	@Named(Consts.BASIC_AUTHENTICATED_CLIENT)
	private OkHttpClient clientHttp;

	@Parameter(property = "result", required = true)
	private String resultFile;

	@Parameter(property = "testplan", required = true)
	private String testplanFile;

	@Inject
	@Named(Consts.JIRA_REST_TMS)
	private URL urlTms;

	private TestFlo testFlo;

	protected UploadResultMojo() {
		super();
	}

	protected UploadResultMojo(final String testplanFile, final String resultFile, final URL urlTms) {
		super();

		if (Convert.isEmpty(testplanFile)) {
			throw new IllegalArgumentException("testplanFile can't be null or empty string.");
		}

		if (Convert.isEmpty(resultFile)) {
			throw new IllegalArgumentException("resultFile can't be null or empty string.");
		}

		if (urlTms == null) {
			throw new IllegalArgumentException("urlTms can't be null.");
		}

		this.testplanFile = testplanFile;
		this.resultFile = resultFile;
		this.urlTms = urlTms;
	}

	private void init() {
		if (UploadResultMojo.logger.isDebugEnabled()) {
			UploadResultMojo.logger.debug(String.format("testplanFile: '%s'.", testplanFile));
			UploadResultMojo.logger.debug(String.format("resultFile: '%s'.", resultFile));
			UploadResultMojo.logger.debug("clients:");
			UploadResultMojo.logger.debug(String.format("urlTms: '%s'.", urlTms));
			UploadResultMojo.logger.debug(String.format("client: '%s'.", clientJira));
			UploadResultMojo.logger.debug(String.format("clientHttp: '%s'.", clientHttp));
		}

		Assert.assertFalse("testplanFile can't be null or empty string.", Convert.isEmpty(testplanFile));
		Assert.assertTrue(String.format("testplanFile '%s' does not exist.", testplanFile),
				(new File(testplanFile)).exists());
		Assert.assertFalse("resultFile can't be null or empty string.", Convert.isEmpty(resultFile));
		Assert.assertTrue(String.format("resultFile '%s' does not exist.", resultFile),
				(new File(resultFile)).exists());

		Assert.assertNotNull("urlTms can't be null.", urlTms);
		Assert.assertNotNull("client can't be null.", clientJira);
		Assert.assertNotNull("clientHttp can't be null.", clientHttp);

		testFlo = new TestFlo(clientJira, clientHttp, urlTms);
	}

	@SuppressWarnings("unchecked")
	private Map<String, TestcaseReport> readResults() throws SystemException {
		final List<File> files = new ArrayList<>();

		final File file = new File(resultFile);
		if (UploadResultMojo.logger.isDebugEnabled()) {
			UploadResultMojo.logger.debug(String.format("search for result(s), starts at file: '%s'.", file));
		}

		if (file.isDirectory()) {
			files.addAll(Arrays.asList(file.listFiles()));
		} else {
			files.add(file);
		}

		if (Convert.isEmpty(files)) {
			throw new SystemException(String.format("can't find result(s) at '%s'.", file));
		}

		final MapperTestcaseReport mapper = new MapperTestcaseReport();
		final Map<String, TestcaseReport> result = new HashMap<>();
		for (final File f : files) {
			if (UploadResultMojo.logger.isDebugEnabled()) {
				UploadResultMojo.logger.debug(String.format("read result(s) from file: '%s'.", file));
			}

			final List<TestcaseReport> reports = mapper.readAll(f);
			if (!Convert.isEmpty(reports)) {
				for (final TestcaseReport report : reports) {
					final String key = report.getId();
					if (result.containsKey(key)) {
						throw new SystemException(String.format(
								"can't process report: '%s': result for test case template: '%s' already readed.", f,
								key));
					}
					result.put(key, report);
				}
			}
		}

		if (result.isEmpty()) {
			throw new SystemException(String.format("can't find result(s) at '%s'.", file));
		}

		return result;
	}

	private List<TestPlan> readTestPlan() throws SystemException {
		final File file = new File(testplanFile);
		if (UploadResultMojo.logger.isDebugEnabled()) {
			UploadResultMojo.logger.debug(String.format("read test plan(s) from file: '%s'.", file));
		}

		final List<TestPlan> result = (new MapperTestplan()).readAll(file);
		if (Convert.isEmpty(result)) {
			throw new SystemException(String.format("can't find test plan(s) at '%s'.", file));
		}

		return result;
	}

	private void upload(final TestPlan testPlan, final Map<String, TestcaseReport> reports) throws SystemException {
		Assert.assertNotNull("testPlan type can't be null.", testPlan);
		Assert.assertNotNull("reports type can't be null.", reports);

		if (UploadResultMojo.logger.isInfoEnabled()) {
			UploadResultMojo.logger.info(String.format("update results for test plan '%s'.", testPlan.getId()));
		}

		for (final TestCase testCase : testPlan.getTestCases()) {
			final TestcaseReport report = reports.get(testCase.getTemplateId());
			if (report == null) {
				throw new SystemException(
						String.format("can't find result for test case template: '%s'.", testCase.getTemplateId()));
			}

			testFlo.updateTestCase(testCase, report);
		}

		testFlo.transition(testPlan, TestPlanStatus.InProgress, TestPlanTransition.Acceptance);
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (UploadResultMojo.logger.isInfoEnabled()) {
			UploadResultMojo.logger.info(String.format("start update test plan based on: '%s'.", testplanFile));
		}

		init();

		try {
			final Map<String, TestcaseReport> reports = readResults();
			final List<TestPlan> testPlans = readTestPlan();

			final StringBuilder builder = new StringBuilder();

			for (final TestPlan testPlan : testPlans) {
				try {
					upload(testPlan, reports);
				} catch (final SystemException ex) {
					final String message = String.format("can't update test plan: '%s'.", testPlan.getId());
					UploadResultMojo.logger.error(message, ex);
					builder.append(", '").append(ex.getMessage()).append("'");
				}
			}

			if (2 < builder.length()) {
				throw new MojoExecutionException(String.format("can't update test plan(s): %s.", builder.substring(2)));
			}
		} catch (final SystemException ex) {
			final String message = String.format("can't update update test plan based on: '%s'.", testplanFile);
			UploadResultMojo.logger.error(message, ex);
			throw new MojoExecutionException(message);
		}
	}
}
