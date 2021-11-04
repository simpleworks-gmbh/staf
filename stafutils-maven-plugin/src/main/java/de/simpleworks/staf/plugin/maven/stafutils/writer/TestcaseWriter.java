package de.simpleworks.staf.plugin.maven.stafutils.writer;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.api.APITeststep;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsIO;
import de.simpleworks.staf.plugin.maven.stafutils.utils.TestclassGeneratorProperties;

public class TestcaseWriter {

	static final Logger logger = LogManager.getLogger(TestcaseWriter.class);
	static final TestclassGeneratorProperties properties = TestclassGeneratorProperties.getInstance();

	private final String packageName;

	private final File testcaseFile;
	private final File testcaseTemplateFile;
	private final File methodFile;
	private final Charset encoding;

	public TestcaseWriter(String packageName, String testCaseFilePath) throws InstantiationError {

		try {

			if (Convert.isEmpty(packageName)) {
				throw new IllegalArgumentException("packageName can't be null or empty string.");
			}

			this.packageName = packageName;
			this.testcaseFile = new File(testCaseFilePath);
			this.testcaseTemplateFile = properties.getTestcaseFile();
			this.methodFile = properties.getMethodsFile();
			encoding = properties.getEncoding();

			validate();
		} catch (Exception ex) {
			logger.error(ex);
			throw new InstantiationError(
					String.format("can't create instance of \"%s\"", TestcaseWriter.class.getName()));
		}
	}

	private void validate() throws Exception {

		if (!this.methodFile.exists()) {
			throw new IllegalArgumentException(
					String.format("The methodFile at \"%s\" does not exist,", this.methodFile.getAbsolutePath()));
		}

		if (encoding == null) {
			throw new IllegalArgumentException("encoding can't be null.");
		}
	}

	private void clearFile() throws Exception {
		UtilsIO.write(Arrays.asList(), this.testcaseFile, encoding);
	}

	private List<String> writeTestcaseHeader() throws Exception {

		final List<String> imports = UtilsIO.read(testcaseTemplateFile, encoding);

		List<String> substitutedConstructor = imports.stream().map(c -> c.replace("${PACKAGE_NAME}", this.packageName))
				.collect(Collectors.toList());

		substitutedConstructor = substitutedConstructor.stream()
				.map(c -> c.replace("${TESTCLASS_NAME}",
						this.testcaseFile.getName().replace(
								String.format(".%s", FilenameUtils.getExtension(this.testcaseFile.getName())),
								Convert.EMPTY_STRING)))
				.collect(Collectors.toList());

		return substitutedConstructor;
	}

	private List<String> writeTestStepMethod(List<APITeststep> apiteststeps) throws Exception {

		final List<String> method = UtilsIO.read(methodFile, encoding);
		final List<String> substitutedMethods = new ArrayList<>();

		for (final APITeststep currentApiteststep : apiteststeps) {

			final String name = currentApiteststep.getName().replace("\n", "\\n");

			method.stream().map(c -> c.replace("${STEP_NAME}", name))
					.map(c -> c.replace("${STEP_ORDER}", Integer.toString(currentApiteststep.getOrder())))
					.forEach(m -> {
						substitutedMethods.add(m);
					});
		}

		return substitutedMethods;
	}

	public void write(List<APITeststep> apiteststeps) throws Exception {

		if (Convert.isEmpty(apiteststeps)) {
			throw new IllegalArgumentException("apiteststeps can't be null or empty.");
		}

		clearFile();
		final List<String> testcaseHeader = writeTestcaseHeader();
		final List<String> stepMethods = writeTestStepMethod(apiteststeps);

		final List<String> results = testcaseHeader.stream()
				.map(c -> c.replace("${METHODS}", String.join("\n", stepMethods))).collect(Collectors.toList());

		UtilsIO.append(results, this.testcaseFile, encoding);
	}
}
