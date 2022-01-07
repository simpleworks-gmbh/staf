package de.simpleworks.staf.plugin.maven.msteams.mojo;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.junit.Assert;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.jayway.jsonpath.JsonPath;

import de.simpleworks.staf.commons.elements.TestCase;
import de.simpleworks.staf.commons.elements.TestPlan;
import de.simpleworks.staf.commons.exceptions.InvalidConfiguration;
import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.mapper.elements.MapperTestplan;
import de.simpleworks.staf.commons.mapper.report.MapperTestcaseReport;
import de.simpleworks.staf.commons.report.StepReport;
import de.simpleworks.staf.commons.report.TestcaseReport;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.commons.utils.UtilsIO;
import de.simpleworks.staf.module.jira.util.JiraProperties;
import de.simpleworks.staf.plugin.maven.msteams.consts.MsTeamsConsts;
import de.simpleworks.staf.plugin.maven.msteams.elements.Section;
import de.simpleworks.staf.plugin.maven.msteams.utils.UtilsMsTeams;
import edu.emory.mathcs.backport.java.util.Arrays;
import net.minidev.json.JSONObject;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Mojo(name = "notifyTestResult", defaultPhase = LifecyclePhase.INITIALIZE)
public class NotifyTestResultMojo extends MsTeamsMojo {

	private static final Logger logger = LogManager.getLogger(NotifyTestResultMojo.class);

	private final MapperTestcaseReport mapperTestcaseReport = new MapperTestcaseReport();
	private final MapperTestplan mapperTestplan = new MapperTestplan();
	private final JiraProperties properties = JiraProperties.getInstance();

	@Parameter(property = "result", required = true)
	private String result;

	@Parameter(property = "testplan", required = true)
	private String testplan;

	@Parameter(property = "template", required = true)
	private String template;

	@Inject
	@Named(MsTeamsConsts.TEAMS_WEBHOOK)
	private URL webhook;

	@Inject
	private OkHttpClient client;

	private File resultFile;
	private List<File> resultFiles;
	private List<TestPlan> testplans;

	private File templateFile;

	protected NotifyTestResultMojo() {
		super();
	}

	@SuppressWarnings("unchecked")
	private void init() throws Exception {

		resultFile = new File(result);

		if (!resultFile.exists()) {
			throw new IllegalArgumentException(String.format("resultFile does not exist at \"%s\".", result));
		}

		resultFiles = new ArrayList<>();

		if (resultFile.isDirectory()) {
			if (!resultFiles.addAll(Arrays.asList(resultFile.listFiles()))) {
				throw new IllegalArgumentException(String.format("can't add resultFile files from \"%s\".", result));
			}
		} else {
			if (!resultFiles.add(resultFile)) {
				throw new IllegalArgumentException(String.format("can't add resultFile file from \"%s\".", result));
			}
		}

		templateFile = new File(template);

		if (!templateFile.exists()) {
			throw new IllegalArgumentException(String.format("the template at \"%s\" does not exist.", template));
		}

		final File testplanFile = new File(testplan);

		if (!testplanFile.exists()) {
			throw new IllegalArgumentException(String.format("the testplan at \"%s\" does not exist.", testplan));
		}

		try {
			testplans = mapperTestplan.readAll(testplanFile);
		} catch (final SystemException ex) {
			final String msg = String.format("can't read testplans from \"%s\".", testplan);
			NotifyTestResultMojo.logger.error(msg, ex);
			throw new Exception(msg);
		}

		if (testplans.size() != 1) {
			final String msg = String.format("testplan size of %s does not match the expected size of 1.",
					Integer.toString(testplans.size()));
			NotifyTestResultMojo.logger.error(msg);
			throw new Exception(msg);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		try {
			// init variables
			init();

			for (final TestPlan plan : testplans) {

				for (final File file : resultFiles) {

					final List<TestcaseReport> testresults = mapperTestcaseReport.readAll(file);

					if (Convert.isEmpty(testresults)) {
						throw new IllegalArgumentException("testresults can't be null or empty.");
					}

					final List<TestcaseReport> skippedTestcases = new ArrayList<>();
					for (final TestCase testcase : plan.getTestCases()) {

						if (!testresults.stream().filter(testresult -> testresult.getId().equals(testcase.getId()))
								.findAny().isPresent()) {
							if (!skippedTestcases.stream().filter(tc -> tc.getId().equals(testcase.getId())).findAny()
									.isPresent()) {

								final TestcaseReport skippedTestcaseReport = new TestcaseReport(testcase.getId());
								skippedTestcases.add(skippedTestcaseReport);
								skippedTestcases.get(0).addStep(new StepReport(
										String.format("Skipped Testcase %s", skippedTestcases.get(0).getId())));
							}
						}
					}

					if (!Convert.isEmpty(skippedTestcases)) {
						if (!testresults.addAll(skippedTestcases)) {
							throw new Exception("can't add skipped testcases.");
						}
					}

					final List<Section> sections = testresults.stream().map(section -> {
						try {
							return UtilsMsTeams.convert(section);
						} catch (Exception ex) {
							return null;
						}
					}).filter(Objects::nonNull).collect(Collectors.toList()).stream().map(section -> {

						try {
							section.setActivityText(String.format("[Current Test-Execution](%s)", String.format(
									"%s/plugins/servlet/ac/com.xpandit.plugins.xray/execution-page?ac.testExecIssueKey=%s&ac.testIssueKey=%s",
									properties.getUrl().toString(), testplans.get(0).getId(),
									section.getActivityTitle())));
						} catch (final InvalidConfiguration ex) {
							NotifyTestResultMojo.logger.error("can't setup link to current test execution.", ex);
						}

						return section;
					}).collect(Collectors.toList());

					final List<JSONObject> convertedSections = sections.stream().map(UtilsMsTeams::convert)
							.collect(Collectors.toList());

					final String jsonString = UtilsIO.getAllContentFromFile(templateFile);

					@SuppressWarnings("rawtypes")
					final LinkedHashMap jsonarray = JsonPath.read(jsonString, "$");

					final JSONObject jsonObject = new JSONObject();

					final List<JSONObject> tmp = new ArrayList<>();

					for (int itr = 0; itr < convertedSections.size(); itr += 1) {
						
						JSONObject convertedSection = convertedSections.get(itr);
						tmp.add(convertedSection);
						jsonObject.put("sections", UtilsCollection.toArray(JSONObject.class, tmp));
						jsonarray.put("sections", jsonObject.get("sections"));

						final RequestBody body = RequestBody.create(MediaType.parse("application/json"),
								new JSONObject(jsonarray).toString());
 						
						/**
						 * We need to ensure that the payload is less than 28KB 
						 * We will send 10KB, to be sure to handle further limitations. 
						 * 
						 * Furthermore we will ensure, that we send data, if we are at the "last index"
						 * */
						if ((body.contentLength() / 1024) > 10 || (itr == convertedSections.size() - 1)) {

							final Request request = new Request.Builder().url(webhook).post(body).build();

							final Call call = client.newCall(request);

							try (Response response = call.execute()) {
								
								
								Assert.assertTrue(
										String.format("The response code %s, does not match the expected one %s.",
												Integer.toString(response.code()), "200"),
										response.code() == 200);
							}

							tmp.clear();
						}
					}
				}
			}

		} catch (final Exception ex) {
			final String msg = "can't upload testresults.";
			NotifyTestResultMojo.logger.error(ex);
			throw new MojoExecutionException(msg);
		}
	}
}
