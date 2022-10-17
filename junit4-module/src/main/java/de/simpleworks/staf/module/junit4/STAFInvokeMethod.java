package de.simpleworks.staf.module.junit4;

import java.lang.reflect.Method;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assume;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import de.simpleworks.staf.commons.annotation.Step;
import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.report.StepReport;
import de.simpleworks.staf.commons.report.artefact.Artefact;
import de.simpleworks.staf.commons.utils.Scanner;
import de.simpleworks.staf.framework.elements.commons.ATestCaseImpl;
import de.simpleworks.staf.framework.elements.commons.TemplateTestCase; 

public class STAFInvokeMethod extends Statement {
	private static final Logger logger = LogManager.getLogger(STAFInvokeMethod.class);
	private final Step step;
	private final FrameworkMethod testMethod;
	private final Object target;
	private final static String TEST_STEP_METHOD_NAME = "executeTestStep";

	public STAFInvokeMethod(final FrameworkMethod testMethod, final Object target) {
		if (!(target instanceof ATestCaseImpl)) {
			throw new IllegalArgumentException(
					String.format("'%s' does not extend '%s'.", target.getClass().getName(), ATestCaseImpl.class.getName()));
		}
		this.target = target;
		if (testMethod.getAnnotation(Step.class) == null) {
			throw new IllegalArgumentException(String.format("'%s' from '%s' has not 'Step Annotation'.",
					testMethod.getName(), ATestCaseImpl.class.getName()));
		}
		step = testMethod.getAnnotation(Step.class);
		if (step.manual()) {
			if (STAFInvokeMethod.logger.isDebugEnabled()) {
				STAFInvokeMethod.logger.debug(String.format("Do not substitute step '%s'.", step.description()));
			}
			this.testMethod = testMethod;
		} else {
			try {
				this.testMethod = substituteTestMethod(testMethod, this.target);
			} catch (Exception ex) {
				final String msg = String.format("can't substitute test method '%s' from '%s'.", testMethod.getName(),
						ATestCaseImpl.class.getName());
				STAFInvokeMethod.logger.error(msg, ex);
				throw new RuntimeException(msg);
			}
		}
	}

	protected FrameworkMethod substituteTestMethod(final FrameworkMethod frameworkmethod, Object testcase)
			throws SystemException {
		if (!(testcase instanceof TemplateTestCase)) {
			return frameworkmethod;
		}
		Method apiTestStepMethod = null;
		// look for any "overriden" executeTestStep method
		apiTestStepMethod = Scanner.getMethod(testcase.getClass(), TEST_STEP_METHOD_NAME);
		if (apiTestStepMethod == null) {
			// look for any "inherited" executeTestStep method
			apiTestStepMethod = Scanner.getDeclaredMethod(testcase.getClass(), TEST_STEP_METHOD_NAME);
		}
		if (apiTestStepMethod == null) {
			throw new SystemException(String.format("can't substitute method \"%s\" with \"%s\".",
					frameworkmethod.getName(), TEST_STEP_METHOD_NAME));
		}
		apiTestStepMethod.setAccessible(true);
		FrameworkMethod result = new FrameworkMethod(apiTestStepMethod);
		return result;
	}

	@Override
	public void evaluate() throws Throwable {
		final ATestCaseImpl tc = (ATestCaseImpl) this.target;
		if (tc.isFailed()) {
			Assume.assumeFalse(tc.isFailed());
		}
		if (!tc.start()) {
			tc.bootstrap();
		}
		long testStepStartTime = -1;
		long testStepStopTime = -1;
		try {
			testStepStartTime = System.nanoTime();
			tc.markStepExecution(step.description());
			// notify, before execution!!!!
			testMethod.invokeExplosively(target);
			testStepStopTime = System.nanoTime();
		} catch (final Throwable th) {
			try {
				testStepStopTime = System.nanoTime();
				@SuppressWarnings("rawtypes")
				Artefact artefact = tc.getArtefact();
				if (artefact == null) {
					artefact = tc.createArtefact();
				}
				tc.stop(new StepReport(step.description(), step.order(), new Exception(th), testStepStartTime,
						testStepStopTime, artefact));
				tc.setFailed(true);
				tc.writeDownResults();
				tc.shutdown();
			} catch (final Exception ex) {
				STAFInvokeMethod.logger.error(ex);
			}
			throw th;
		}
		@SuppressWarnings("rawtypes")
		Artefact artefact = tc.getArtefact();
		if (artefact == null) {
			artefact = tc.createArtefact();
		}
		if (!tc.stop(new StepReport(step.description(), step.order(), testStepStartTime, testStepStopTime, artefact))) {
			tc.writeDownResults();
			tc.shutdown();
		}
	}
}