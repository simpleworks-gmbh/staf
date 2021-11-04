package de.simpleworks.staf.plugin.maven.surefire;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.surefire.SurefirePlugin;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import de.simpleworks.staf.commons.elements.TestPlan;
import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.mapper.elements.MapperTestplan;
import de.simpleworks.staf.commons.utils.Convert;

@Mojo(name = "test", defaultPhase = LifecyclePhase.TEST, threadSafe = true, requiresDependencyResolution = ResolutionScope.TEST)
public class STAFSurefirePlugin extends SurefirePlugin {
	static final Logger logger = LogManager.getLogger(STAFSurefirePlugin.class);

	private static final String TEST_CLASSES_DELIMETER = ",";

	@Parameter(property = "file", required = true)
	private String file;

	@Override
	public void setTest(final String test) {
		final String t = getTest();
		if (STAFSurefirePlugin.logger.isDebugEnabled()) {
			STAFSurefirePlugin.logger.debug(String.format("current test: '%s'.", t));
		}

		final String tests = Convert.isEmpty(t) ? test
				: String.join(STAFSurefirePlugin.TEST_CLASSES_DELIMETER, test, t);
		if (STAFSurefirePlugin.logger.isDebugEnabled()) {
			STAFSurefirePlugin.logger.debug(String.format("set tests: '%s'.", tests));
		}

		super.setTest(tests);
	}

	private List<TestPlan> getTestPlans(final String filepath) {
		if (Convert.isEmpty(filepath)) {
			throw new IllegalArgumentException("filepath can't be null or empty string.");
		}

		final File testplanFile = new File(filepath);
		if (!testplanFile.exists()) {
			throw new RuntimeException(String.format("testplan at '%s' does not exist.", file));
		}

		if (STAFSurefirePlugin.logger.isDebugEnabled()) {
			STAFSurefirePlugin.logger.debug(String.format("read test plan from file: '%s'.", testplanFile));
		}

		final List<TestPlan> testplans;

		try {
			testplans = new MapperTestplan().readAll(testplanFile);
		} catch (final Exception ex) {
			final String msg = String.format("cannot read testplans from '%s'.", filepath);
			STAFSurefirePlugin.logger.error(msg, ex);
			throw new RuntimeException(msg);
		}

		return testplans;
	}

	private static TestcaseFinder createTestcaseFinder(final MavenProject project) throws SystemException {
		final TestcaseFinder result = new TestcaseFinder();

		if (STAFSurefirePlugin.logger.isDebugEnabled()) {
			STAFSurefirePlugin.logger.debug("scanning elements of class path for compiled tests.");
		}

		try {
			result.load(project.getTestClasspathElements());
		} catch (final DependencyResolutionRequiredException ex) {
			final String msg = "can't get elements of class path for compiled classes.";
			STAFSurefirePlugin.logger.error(msg, ex);
			throw new SystemException(msg);
		}

		return result;
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (getProject() == null) {
			throw new MojoExecutionException("project can't be null.");
		}

		final List<TestPlan> testPlans = getTestPlans(file);
		if (Convert.isEmpty(testPlans)) {
			STAFSurefirePlugin.logger.info("no test plans found.");
			return;
		}

		final List<Class<?>> testCaseClasses = new ArrayList<>();

		try {
			final TestcaseFinder finder = STAFSurefirePlugin.createTestcaseFinder(getProject());
			final TestplanValidator validator = new TestplanValidator(finder);
			for (final TestPlan testPlan : testPlans) {
				testCaseClasses.addAll(validator.validate(testPlan));
			}
		} catch (final SystemException ex) {
			final String msg = "can't add implementation to test plan.";
			STAFSurefirePlugin.logger.error(msg, ex);
			throw new MojoFailureException(msg);
		}

		if (Convert.isEmpty(testCaseClasses)) {
			STAFSurefirePlugin.logger.info("no testcases were defined, will execute tests, from the command line.");
			return;
		}

		setTest(String.join(STAFSurefirePlugin.TEST_CLASSES_DELIMETER,
				testCaseClasses.stream().map(clazz -> clazz.getName()).collect(Collectors.toList())));

		super.execute();
	}
}
