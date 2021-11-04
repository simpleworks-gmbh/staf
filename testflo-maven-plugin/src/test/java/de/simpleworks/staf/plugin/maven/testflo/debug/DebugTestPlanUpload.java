package de.simpleworks.staf.plugin.maven.testflo.debug;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.junit.Assert;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.beust.jcommander.JCommander;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.simpleworks.staf.commons.elements.TestCase;
import de.simpleworks.staf.commons.elements.TestPlan;
import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.mapper.elements.MapperTestplan;
import de.simpleworks.staf.commons.mapper.report.MapperTestcaseReport;
import de.simpleworks.staf.commons.report.TestcaseReport;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.module.jira.module.JiraModule;
import de.simpleworks.staf.plugin.maven.testflo.commons.TestFlo;
import de.simpleworks.staf.plugin.maven.testflo.consts.Consts;
import de.simpleworks.staf.plugin.maven.testflo.mojo.TestfloMojo;
import okhttp3.OkHttpClient;

public class DebugTestPlanUpload extends TestfloMojo {

	private static final Logger logger = LogManager.getLogger(DebugTestPlanUpload.class);

	@Inject
	private IssueRestClient clientJira;

	@Inject
	@Named(Consts.BASIC_AUTHENTICATED_CLIENT)
	private OkHttpClient clientHttp;

	@Parameter(property = "report", required = true)
	private final String reportFile;

	@Parameter(property = "testplan", required = true)
	private final String testplanFile;

	@Inject
	@Named(Consts.JIRA_REST_TMS)
	private final URL urlTms;

	private TestFlo testFlo;

	protected DebugTestPlanUpload(final String testplanFile, final String reportFile, final URL urlTms) {
		super(new JiraModule());

		if (Convert.isEmpty(testplanFile)) {
			throw new IllegalArgumentException("testplanFile can't be null or empty string.");
		}

		if (Convert.isEmpty(reportFile)) {
			throw new IllegalArgumentException("reportFile can't be null or empty string.");
		}

		if (urlTms == null) {
			throw new IllegalArgumentException("urlTms can't be null.");
		}

		this.testplanFile = testplanFile;
		this.reportFile = reportFile;
		this.urlTms = urlTms;
	}

	private void init() {
		if (DebugTestPlanUpload.logger.isDebugEnabled()) {
			DebugTestPlanUpload.logger.debug(String.format("testplanFile: '%s'.", testplanFile));
			DebugTestPlanUpload.logger.debug(String.format("reportFile: '%s'.", reportFile));
			DebugTestPlanUpload.logger.debug("clients:");
			DebugTestPlanUpload.logger.debug(String.format("urlTms: '%s'.", urlTms));
			DebugTestPlanUpload.logger.debug(String.format("client: '%s'.", clientJira));
			DebugTestPlanUpload.logger.debug(String.format("clientHttp: '%s'.", clientHttp));
		}

		Assert.assertFalse("testplanFile can't be null or empty string.", Convert.isEmpty(testplanFile));
		Assert.assertTrue(String.format("testplanFile '%s' does not exist.", testplanFile),
				(new File(testplanFile)).exists());
		Assert.assertFalse("reportFile can't be null or empty string.", Convert.isEmpty(reportFile));
		Assert.assertTrue(String.format("reportFile '%s' does not exist.", reportFile),
				(new File(reportFile)).exists());

		Assert.assertNotNull("urlTms can't be null.", urlTms);
		Assert.assertNotNull("client can't be null.", clientJira);
		Assert.assertNotNull("clientHttp can't be null.", clientHttp);

		testFlo = new TestFlo(clientJira, clientHttp, urlTms);
	}

	private Map<String, TestcaseReport> readReports() throws SystemException {
		final List<File> files = new ArrayList<>();

		final File file = new File(reportFile);
		if (DebugTestPlanUpload.logger.isDebugEnabled()) {
			DebugTestPlanUpload.logger.debug(String.format("search for report(s), starts at file: '%s'.", file));
		}

		if (file.isDirectory()) {
			files.addAll(Arrays.asList(file.listFiles()));
		} else {
			files.add(file);
		}

		if (Convert.isEmpty(files)) {
			throw new SystemException(String.format("can't find report(s) at '%s'.", file));
		}

		final MapperTestcaseReport mapper = new MapperTestcaseReport();
		final Map<String, TestcaseReport> result = new HashMap<>();
		for (final File f : files) {
			if (DebugTestPlanUpload.logger.isDebugEnabled()) {
				DebugTestPlanUpload.logger.debug(String.format("read report(s) from file: '%s'.", file));
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
			throw new SystemException(String.format("can't find report(s) at '%s'.", file));
		}

		return result;
	}

	private List<TestPlan> readTestPlan() throws SystemException {
		final File file = new File(testplanFile);
		if (DebugTestPlanUpload.logger.isDebugEnabled()) {
			DebugTestPlanUpload.logger.debug(String.format("read test plan(s) from file: '%s'.", file));
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

		if (DebugTestPlanUpload.logger.isInfoEnabled()) {
			DebugTestPlanUpload.logger.info(String.format("update results for test plan '%s'.", testPlan.getId()));
		}

		for (final TestCase testCase : testPlan.getTestCases()) {
			final TestcaseReport report = reports.get(testCase.getTemplateId());
			if (report == null) {
				throw new SystemException(
						String.format("can't find result for test case template: '%s'.", testCase.getTemplateId()));
			}

			testFlo.updateTestCase(testCase, report);
		}
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (DebugTestPlanUpload.logger.isInfoEnabled()) {
			DebugTestPlanUpload.logger.info(String.format("start update test plan based on: '%s'.", testplanFile));
		}

		init();

		try {
			final Map<String, TestcaseReport> reports = readReports();
			final List<TestPlan> testPlans = readTestPlan();

			final StringBuilder builder = new StringBuilder();

			for (final TestPlan testPlan : testPlans) {
				try {
					upload(testPlan, reports);
				} catch (final SystemException ex) {
					final String message = String.format("can't update test plan: '%s'.", testPlan.getId());
					DebugTestPlanUpload.logger.error(message, ex);
					builder.append(", '").append(ex.getMessage()).append("'");
				}
			}

			if (2 < builder.length()) {
				throw new MojoExecutionException(String.format("can't update test plan(s): %s.", builder.substring(2)));
			}
		} catch (final SystemException ex) {
			final String message = String.format("can't update update test plan based on: '%s'.", testplanFile);
			DebugTestPlanUpload.logger.error(message, ex);
			throw new MojoExecutionException(message);
		}
	}

	public static void main(final String[] args) throws MojoExecutionException, MojoFailureException {
		DebugTestPlanUpload.logger.info("start..");

		final DebugArgsUpload arguments = new DebugArgsUpload();
		JCommander.newBuilder().addObject(arguments).build().parse(args);

		new DebugTestPlanUpload(arguments.testplanFile, arguments.reportFile, arguments.urlTms).execute();

		DebugTestPlanUpload.logger.info("DONE.");
	}
}
