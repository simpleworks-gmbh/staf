package de.simpleworks.staf.plugin.maven.stafutils.mojo;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import de.simpleworks.staf.commons.api.APITeststep;
import de.simpleworks.staf.commons.mapper.api.MapperAPITeststep;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsIO;
import de.simpleworks.staf.commons.utils.comparer.TeststepComparator;
import de.simpleworks.staf.plugin.maven.stafutils.writer.TestcaseWriter;

@Mojo(name = "generateAPITestclasses", defaultPhase = LifecyclePhase.INITIALIZE, threadSafe = true)
public class APITestclassesGenerator extends AbstractMojo {

	private final static Logger logger = LogManager.getLogger(APITestclassesGenerator.class);
	private final static MapperAPITeststep mapper = new MapperAPITeststep();

	@Parameter(property = "testclassDirectory", required = true)
	private String testclassDirectory;

	@Parameter(property = "requestFileDirectory", required = true)
	private String requestFileDirectory;

	@Parameter(property = "packageName", required = true)
	private String packageName;

	private List<File> requestFiles;

	private void init() throws Exception {

		final File testclassesDirectory = new File(testclassDirectory);

		if (!testclassesDirectory.isDirectory()) {
			throw new IllegalArgumentException(String.format("testclassesDirectory \"%s\" is not a directory.",
					testclassesDirectory.getAbsolutePath()));
		}

		if (!testclassesDirectory.exists()) {
			UtilsIO.createParentDir(testclassesDirectory);
		}

		final File requestFilesDirectory = new File(requestFileDirectory);

		if (!requestFilesDirectory.exists()) {
			throw new IllegalArgumentException(String.format("requestFilesDirectory at \"%s\" does not exist.",
					requestFilesDirectory.getAbsolutePath()));
		}

		requestFiles = UtilsIO.listFiles(requestFilesDirectory, "*.json");

		if (Convert.isEmpty(requestFiles)) {
			throw new IllegalArgumentException("requestFiles can't be null or empty.");
		}
	}

	/**
	 * @return List of sorted APITeststeps
	 * @param File requestFile, that contains the steps of an "API related Test"
	 * @throws Exception
	 */
	private static List<APITeststep> fetchAPISteps(final File requestFile) throws Exception {

		if (requestFile == null) {
			throw new IllegalArgumentException("requestFile can't be null.");
		}

		if (!requestFile.exists()) {
			throw new IllegalArgumentException(
					String.format("requestFile at \"%s\" does not exist.", requestFile.getAbsolutePath()));
		}

		final List<APITeststep> result = APITestclassesGenerator.mapper.readAll(requestFile);
		result.sort(new TeststepComparator());
		return result;
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		try {
			// init variables
			init();

			for (final File requestFile : requestFiles) {
				final List<APITeststep> apiteststeps = APITestclassesGenerator.fetchAPISteps(requestFile);

				final File file = FileUtils.getFile(testclassDirectory,
						requestFile.getName().replace(FilenameUtils.getExtension(requestFile.getName()), "java"));

				final TestcaseWriter testcasewriter = new TestcaseWriter(packageName, file.getAbsolutePath());
				testcasewriter.write(apiteststeps);
			}

		} catch (final Exception ex) {
			final String msg = "can't create Testclass.";
			APITestclassesGenerator.logger.error(msg, ex);
			throw new MojoExecutionException(msg);
		}
	}
}