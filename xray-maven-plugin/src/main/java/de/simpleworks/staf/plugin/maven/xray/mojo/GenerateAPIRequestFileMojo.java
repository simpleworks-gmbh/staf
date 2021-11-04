package de.simpleworks.staf.plugin.maven.xray.mojo;

import java.io.File;
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
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.util.concurrent.Promise;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.simpleworks.staf.commons.api.APITeststep;
import de.simpleworks.staf.commons.elements.TestCase;
import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.mapper.api.MapperAPITeststep;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsIO;
import de.simpleworks.staf.commons.utils.UtilsTestcase;
import de.simpleworks.staf.plugin.maven.xray.commons.Xray;
import de.simpleworks.staf.plugin.maven.xray.consts.XrayConsts;

@Mojo(name = "generateAPIRequestFile", defaultPhase = LifecyclePhase.INITIALIZE)
public class GenerateAPIRequestFileMojo extends XrayMojo {

	private static final Logger logger = LogManager.getLogger(GenerateAPIRequestFileMojo.class);
	private static final MapperAPITeststep mapper = new MapperAPITeststep();

	@Inject
	private IssueRestClient clientJira;

	@Parameter(property = "id", required = true)
	private String id;

	@Parameter(property = "file", required = true)
	private String file;
	private File requestFile;

	@Inject
	@Named(XrayConsts.XRAY_CLIENT)
	private Xray xrayClient;

	protected GenerateAPIRequestFileMojo() {
		super();
	}

	private void init() {

		if (GenerateAPIRequestFileMojo.logger.isDebugEnabled()) {
			GenerateAPIRequestFileMojo.logger.debug(String.format("id: '%s'.", id));
			GenerateAPIRequestFileMojo.logger.debug(String.format("file: '%s'.", file));
			GenerateAPIRequestFileMojo.logger.debug("clients:");
			GenerateAPIRequestFileMojo.logger.debug(String.format("client: '%s'.", clientJira));
		}

		Assert.assertFalse("id can't be null or empty string.", Convert.isEmpty(id));
		Assert.assertFalse("fileName can't be null or empty string.", Convert.isEmpty(file));
		Assert.assertNotNull("client can't be null.", clientJira);

		requestFile = new File(file);
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (GenerateAPIRequestFileMojo.logger.isInfoEnabled()) {
			GenerateAPIRequestFileMojo.logger.info(String.format("fetch test case: '%s'.", id));
		}

		init();

		try {

			final Long xrayId = getId(id);
			final TestCase testcase = xrayClient.readTestCase(id, xrayId.longValue());
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

	private Long getId(final String key) {
		try {
			final Promise<Issue> promise = clientJira.getIssue(key);
			final Issue issue = promise.claim();

			return issue.getId();
		} catch (final Exception ex) {
			final String msg = String.format("can't fetch id for Test \"%s\".", key);
			GenerateAPIRequestFileMojo.logger.error(msg, ex);
			throw new IllegalArgumentException(msg);
		}
	}
}
