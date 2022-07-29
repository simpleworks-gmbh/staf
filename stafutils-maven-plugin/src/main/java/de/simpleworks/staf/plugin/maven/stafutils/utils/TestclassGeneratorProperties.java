package de.simpleworks.staf.plugin.maven.stafutils.utils;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.annotation.Property;
import de.simpleworks.staf.commons.annotation.Property.Default;
import de.simpleworks.staf.commons.annotation.Property.NotNull;
import de.simpleworks.staf.commons.utils.PropertiesReader;
import de.simpleworks.staf.plugin.maven.stafutils.consts.TestclassGeneratorConsts;

public class TestclassGeneratorProperties extends PropertiesReader {

	private static final Logger logger = LogManager.getLogger(TestclassGeneratorProperties.class);

	private static TestclassGeneratorProperties instance = null;

	@NotNull
	@Property(TestclassGeneratorConsts.TESTCLASS_GENERATOR_TESTCASE_FILE)
	private String testcaseFile;

	@NotNull
	@Property(TestclassGeneratorConsts.TESTCLASS_GENERATOR_METHODS_FILE)
	private String methodsFile;

	@Default("UTF-8")
	@Property(TestclassGeneratorConsts.TESTCLASS_GENERATOR_FILE_ENCODING)
	private String encoding;

	public File getTestcaseFile() {
		return new File(testcaseFile);
	}

	public File getMethodsFile() {
		return new File(methodsFile);
	}

	public Charset getEncoding() {
		return Charset.forName(encoding);
	}

	public static final synchronized TestclassGeneratorProperties getInstance() {
		if (TestclassGeneratorProperties.instance == null) {
			if (TestclassGeneratorProperties.logger.isDebugEnabled()) {
				TestclassGeneratorProperties.logger.debug("create instance.");
			}

			TestclassGeneratorProperties.instance = new TestclassGeneratorProperties();
		}

		return TestclassGeneratorProperties.instance;
	}

	@Override
	protected Class<TestclassGeneratorProperties> getClazz() {
		return TestclassGeneratorProperties.class;
	}
}