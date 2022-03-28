package de.simpleworks.staf.plugin.maven.testflo.mojo;

import java.io.File;
import java.net.URL;
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

import de.simpleworks.staf.commons.api.APITeststep;
import de.simpleworks.staf.commons.elements.TestCase;
import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.mapper.api.MapperAPITeststep;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsIO;
import de.simpleworks.staf.commons.utils.UtilsTestcase;
import de.simpleworks.staf.module.jira.util.consts.ClientConsts;
import de.simpleworks.staf.plugin.maven.testflo.commons.TestFlo;
import de.simpleworks.staf.plugin.maven.testflo.consts.Consts;
import okhttp3.OkHttpClient;

@Mojo(name = "generateAPIRequestFile", defaultPhase = LifecyclePhase.INITIALIZE)
public class GenerateAPIRequestFileMojo extends TestfloMojo {

	private static final Logger logger = LogManager.getLogger(GenerateAPIRequestFileMojo.class);
	private static final MapperAPITeststep mapper = new MapperAPITeststep();

	@Inject
	private IssueRestClient clientJira;

	@Inject
	@Named(Consts.BASIC_AUTHENTICATED_CLIENT)
	private OkHttpClient clientHttp;

	@Parameter(property = "id", required = true)
	private String id;

	@Parameter(property = "file", required = true)
	private String file;
	private File requestFile;

	@Inject
	@Named(Consts.JIRA_REST_TMS)
	private URL urlTms;

	@Inject
	@Named(ClientConsts.URL)
	private URL jiraUrl;

	private TestFlo testFlo;

	protected GenerateAPIRequestFileMojo() {
		super();
	}

	private void init() {

		if (GenerateAPIRequestFileMojo.logger.isDebugEnabled()) {
			GenerateAPIRequestFileMojo.logger.debug(String.format("id: '%s'.", id));
			GenerateAPIRequestFileMojo.logger.debug(String.format("file: '%s'.", file));
			GenerateAPIRequestFileMojo.logger.debug("clients:");
			GenerateAPIRequestFileMojo.logger.debug(String.format("urlTms: '%s'.", urlTms));
			GenerateAPIRequestFileMojo.logger.debug(String.format("client: '%s'.", clientJira));
			GenerateAPIRequestFileMojo.logger.debug(String.format("clientHttp: '%s'.", clientHttp));
		}

		Assert.assertFalse("id can't be null or empty string.", Convert.isEmpty(id));
		Assert.assertFalse("fileName can't be null or empty string.", Convert.isEmpty(file));
		Assert.assertNotNull("urlTms can't be null.", urlTms);
		Assert.assertNotNull("client can't be null.", clientJira);
		Assert.assertNotNull("clientHttp can't be null.", clientHttp);

		requestFile = new File(file);
		testFlo = new TestFlo(clientJira, clientHttp, urlTms, jiraUrl);

	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (GenerateAPIRequestFileMojo.logger.isInfoEnabled()) {
			GenerateAPIRequestFileMojo.logger.info(String.format("fetch test case: '%s'.", id));
		}

		init();

		try {

			final TestCase testcase = testFlo.readTestCase(id);
			final List<APITeststep> apiteststeps = UtilsTestcase.convert(testcase);

			if (Convert.isEmpty(apiteststeps)) {
				throw new SystemException("apiteststeps can't be null or empty.");
			}

			if (GenerateAPIRequestFileMojo.logger.isInfoEnabled()) {
				GenerateAPIRequestFileMojo.logger
						.info(String.format("delete current file: '%s'.", requestFile.getAbsolutePath()));
			}

			UtilsIO.deleteFile(requestFile);

			if (GenerateAPIRequestFileMojo.logger.isInfoEnabled()) {
				GenerateAPIRequestFileMojo.logger
						.info(String.format("write test case file: '%s'.", requestFile.getAbsolutePath()));
			}

			GenerateAPIRequestFileMojo.mapper.write(requestFile, apiteststeps);

			final List<APITeststep> readApiteststeps = GenerateAPIRequestFileMojo.mapper.readAll(requestFile);

			if (Convert.isEmpty(readApiteststeps)) {
				throw new SystemException("readApiteststeps can't be null or empty.");
			}

		} catch (final SystemException ex) {
			final String message = String.format("can't start test plan: '%s'.", id);
			GenerateAPIRequestFileMojo.logger.error(message, ex);
			throw new MojoExecutionException(message);
		}
	}
}
