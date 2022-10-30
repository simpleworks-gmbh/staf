package de.simpleworks.staf.framework.elements.commons;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.inject.Guice;
import com.google.inject.Module;
import de.simpleworks.staf.commons.annotation.Step;
import de.simpleworks.staf.commons.report.artefact.Artefact;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.framework.elements.api.RewriteUrlObject;
import de.simpleworks.staf.framework.util.TestCaseUtils;

public abstract class TestCase extends ATestCaseImpl{
	
	private static final Logger logger = LoggerFactory.getLogger(TestCase.class);

	public TestCase(final Module... modules) {
		if (Convert.isEmpty(modules)) {
			if (TestCase.logger.isDebugEnabled()) {
				TestCase.logger.debug(String.format("No Custom-Modules will be injected."));
			}
		}
		
		try {
			// load guice models
			Guice.createInjector(modules).injectMembers(this);
		} catch (final Exception ex) {
			final String message = String.format("Cannot initiate TestCase of Type '%s'.", this.getClass().getName());
			TestCase.logger.error(message, ex);
			throw new InstantiationError(message);
		}
	}
 

	public abstract List<RewriteUrlObject> getRewriteUrls();

	/**
	 * @return
	 * @brief method to start other testcases
	 * @return current variable storage (Map<String, Map<String, String>>) of any
	 *         testcase
	 */
	protected Map<String, Map<String, String>> executeTestcase(final TestCase testcase) throws Exception {
		if (testcase == null) {
			throw new IllegalArgumentException("testcase can't be null.");
		}

		if (testcase.isFailed()) {
			throw new RuntimeException(String.format("testcase '%s' has already failed..", testcase.getTestCaseName()));
		}

		if (!testcase.start()) {
			testcase.bootstrap();
		}

		try {
			for (Method stepmethod : TestCaseUtils.fetchStepMethods(testcase.getClass())) {
				if (stepmethod == null) {
					TestCase.logger.error("stepmethod can't be null.");
					break;
				}
				final Step step = stepmethod.getAnnotation(Step.class);
				if (step == null) {
					TestCase.logger.error(String.format("Method '%s' is not annotated by a 'Step' annotation.",
							stepmethod.getName()));
					break;
				}
				if (TestCase.logger.isDebugEnabled()) {
					TestCase.logger.debug(String.format("Execute step '%s'.", step.description()));
				}
				testcase.executeTestStep();

				@SuppressWarnings("rawtypes")
				final Artefact current = testcase.createArtefact();
				// transmit artefact
				this.setArtefact(current);

			}
		} catch (final Throwable th) {

			TestCase.logger.error(String.format("Testcase '%s' has failed.", this.getTestCaseName()), th);

			@SuppressWarnings("rawtypes")
			final Artefact current = testcase.createArtefact();

			// transmit artefact
			this.setArtefact(current);

			// shutdown testcase after an error happened
			testcase.shutdown();

			throw th;
		}

		if (testcase.start()) {
			testcase.shutdown();
		}

		return testcase.getExtractedValues();
	}

}