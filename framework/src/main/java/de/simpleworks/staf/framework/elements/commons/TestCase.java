package de.simpleworks.staf.framework.elements.commons;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.inject.Guice;
import com.google.inject.Module;
import de.simpleworks.staf.commons.annotation.Step;
import de.simpleworks.staf.commons.annotation.Testcase;
import de.simpleworks.staf.commons.enums.Result;
import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.report.StepReport;
import de.simpleworks.staf.commons.report.TestcaseReport;
import de.simpleworks.staf.commons.report.artefact.Artefact;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsDate;
import de.simpleworks.staf.framework.elements.api.APITestCase;
import de.simpleworks.staf.framework.elements.api.RewriteUrlObject;
import de.simpleworks.staf.framework.elements.commons.properties.TestCaseProperties;
import de.simpleworks.staf.framework.enums.CreateArtefactEnum;
import de.simpleworks.staf.framework.reporter.json.JsonReporter;
import de.simpleworks.staf.framework.reporter.manager.ReporterManager;
import de.simpleworks.staf.framework.util.TestCaseUtils;
import net.lightbody.bmp.BrowserMobProxyServer;

public abstract class TestCase {
	private static final Logger logger = LoggerFactory.getLogger(TestCase.class);
	private static final TestCaseProperties testcaseProperties = TestCaseProperties.getInstance();
	private final Semaphore startLock = new Semaphore(1);
	private ReporterManager reporter = null;
	private boolean isRunning = false;
	private boolean isFailed = false;
	private String nuance = Convert.EMPTY_STRING;
	private final TestcaseReport testcaseReport;
	private int shutdownCounter;
	@SuppressWarnings("rawtypes")
	private Artefact artefact;
	private final Map<String, Map<String, String>> extractedValues;

	public TestCase(final Module... modules) {
		if (Convert.isEmpty(modules)) {
			if (TestCase.logger.isDebugEnabled()) {
				TestCase.logger.debug(String.format("No Custom-Modules will be injected."));
			}
		}
		if (this.getClass().getAnnotation(Testcase.class) == null) {
			final String msg = String.format("'%s' does not have the annotation '%s'.", this.getClass().getName(),
					Testcase.class.getName());
			TestCase.logger.error(msg);
			throw new InstantiationError(msg);
		}
		

		Object ob = null;

		try{
			Class clazz =  Class.forName(testcaseProperties.getReporterManagerClass());
			ob = clazz.newInstance();
		}
		catch(Exception ex) {
			ex.printStackTrace();
			final String msg = String.format("'%s' does not have the annotation '%s'.", this.getClass().getName(),
					Testcase.class.getName());
			TestCase.logger.error(msg);
			throw new InstantiationError(msg);
		}
		
		if(!(ob instanceof ReporterManager)) {
			final String msg =String.format("object '%s' is no instance of '%s'.",ob.getClass(), ReporterManager.class);
			TestCase.logger.error(msg);
			throw new InstantiationError(msg);
		}
		
		reporter = (ReporterManager) ob;
		
		try {
			// load guice models
			Guice.createInjector(modules).injectMembers(this);
			// initialize lists of step methods
			shutdownCounter = getStepsSize();
			testcaseReport = new TestcaseReport(getTestCaseName());
			extractedValues = new HashMap<>();
		} catch (final Exception ex) {
			final String message = String.format("Cannot initiate TestCase of Type '%s'.", this.getClass().getName());
			TestCase.logger.error(message, ex);
			throw new InstantiationError(message);
		}
	}

	public int getStepsSize() {
		return TestCaseUtils.fetchStepMethods(this.getClass()).size();
	}

	/**
	 * @brief method that starts the Testcase (manages execution of
	 *        {@code bootstrap})
	 * @return true if the testacse is running, false if not.
	 */
	public synchronized final boolean start() {
		try {
			// if not running
			startLock.acquire();
			if (!isRunning) {
				bootstrap();
				isRunning = true;
				testcaseReport.setStartTime(System.nanoTime());
			}
		} catch (final Exception ex) {
			TestCase.logger.error("Cannot start instance of TestCase.", ex);
			isRunning = false;
		} finally {
			startLock.release();
		}
		return isRunning;
	}

	private static boolean shouldArtefactBeCreated(final Result stepResult) {
		if (stepResult == null) {
			throw new IllegalArgumentException("stepResult can't be null.");
		}
		final CreateArtefactEnum createartefact = TestCase.testcaseProperties.getCreateArtefactOn();
		if (TestCase.logger.isDebugEnabled()) {
			TestCase.logger.debug(String.format("Create Artefact on '%s'.", createartefact.getValue()));
		}
		if (CreateArtefactEnum.EVERYTIME.equals(createartefact)) {
			return true;
		}
		boolean result = false;
		switch (stepResult) {
		case FAILURE:
			result = CreateArtefactEnum.ON_FAILURE.equals(createartefact);
			break;
		case SUCCESSFULL:
			result = CreateArtefactEnum.ON_SUCCESS.equals(createartefact);
			break;
		default:
			if (TestCase.logger.isDebugEnabled()) {
				TestCase.logger.debug(String.format("No action defined for '%s' will return '%s'.",
						stepResult.getValue(), Boolean.valueOf(result)));
			}
		}
		return result;
	}

	/**
	 * @param stepReport
	 * @brief method that starts the Testcase (manages execution of
	 *        {@code bootstrap})
	 * @return true if the testacse is running, false if not.
	 */
	public final boolean stop(final StepReport stepReport) {
		if (stepReport == null) {
			TestCase.logger.error(String.format("The testcase '%s' is missing a stepreport.", getTestCaseName()));
		} else {
			try {
				if (!TestCase.shouldArtefactBeCreated(stepReport.getResult())) {
					stepReport.setArtefact(null);
				}
				testcaseReport.setStopTime(stepReport.getStopTime());
				testcaseReport.addStep(stepReport);
			} catch (final SystemException ex) {
				TestCase.logger
						.error(String.format("Cannot add step '%s' to test report.", stepReport.getDescription()), ex);
			}
		}
		shutdownCounter -= 1;
		if (shutdownCounter <= 0) {
			isRunning = false;
		}
		
		// remove current artefact
		this.setArtefact(null);
		
		return isRunning;
	}

	protected String createXRequestId(final String description) {
		if (Convert.isEmpty(description)) {
			throw new IllegalArgumentException("description can't be null or empty string.");
		}
		final Date date = UtilsDate.getCurrentDateTime();
		final String result = String.format("Testcase_%s-Step_%s-Date_%s-Timestamp_%s", getTestCaseName(), description,
				UtilsDate.getCurrentTimeFormatted(date), Long.valueOf(UtilsDate.getCurrentTimeInMilliSeonds(date)));
		return result.replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", "").replaceAll(" ", "");
	}

	public void markStepExecution(final String mark) throws Exception {
		if (Convert.isEmpty(mark)) {
			TestCase.logger.info(String.format("no mark has been defined, to identify the step of '%s'.",
					APITestCase.class.getName()));
			return;
		}
		final BrowserMobProxyServer proxy = getProxy();
		if (proxy == null) {
			if (TestCase.logger.isDebugEnabled()) {
				TestCase.logger.debug("steps will not be marked, because the proxy is null.");
			}
			return;
		}
		// check if random nuance was already created
		if (Convert.isEmpty(nuance)) {
			nuance = UUID.randomUUID().toString();
		}
		proxy.removeHeader(TestCase.testcaseProperties.getTestStepHeaderName());
		proxy.addHeader(TestCase.testcaseProperties.getTestStepHeaderName(), createXRequestId(mark));
		proxy.addHeader("Cookie", String.format("%s_%s=%s", TestCase.testcaseProperties.getTestCaseHeaderName(),
				getTestCaseName(), nuance));
	}

	public abstract void bootstrap() throws Exception;

	public abstract void shutdown() throws Exception;

	public abstract void executeTestStep() throws Exception;

	public abstract BrowserMobProxyServer getProxy();

	public abstract List<RewriteUrlObject> getRewriteUrls();

	public void writeDownResults() {
		final TestcaseReport report = getTestcaseReport();
		if (report == null) {
			if (TestCase.logger.isDebugEnabled()) {
				TestCase.logger.debug("report can't be null, no report will be written.");
			}
		} else { 	
			reporter.saveReport(report);
		}
	}

	public void setExtractedValues(final Map<String, Map<String, String>> map) {
		for (final String key : map.keySet()) {
			final Map<String, String> values = map.get(key);
			if (extractedValues.containsKey(key)) {
				final Map<String, String> tmpValues = extractedValues.get(key);
				for (final String tmpKey : tmpValues.keySet()) {
					// values, only adds keys from extractedValues that do not exist.
					if (!values.containsKey(tmpKey)) {
						values.put(tmpKey, tmpValues.get(tmpKey));
					}
				}
			}
			extractedValues.put(key, values);
		}
	}

	public Map<String, Map<String, String>> getExtractedValues() {
		return extractedValues;
	}

	/**
	 * @brief method that starts the Testcase (manages execution of
	 *        {@code bootstrap})
	 * @return artefact {@code Artefact} of the current test(-step), null if no
	 *         artefact was generated.
	 */
	@SuppressWarnings("rawtypes")
	public abstract Artefact createArtefact();

	/**
	 * @brief getters
	 */
	public TestcaseReport getTestcaseReport() {
		return testcaseReport;
	}

	public final String getTestCaseName() {
		final Testcase testcase = this.getClass().getAnnotation(Testcase.class);
		if (testcase == null) {
			if (TestCase.logger.isWarnEnabled()) {
				TestCase.logger
						.warn(String.format("testcase annotation was not set, name is set to '%s'.", Convert.UNKNOWN));
			}
			return Convert.UNKNOWN;
		}
		final String id = testcase.id();
		if (Convert.isEmpty(id)) {
			if (TestCase.logger.isWarnEnabled()) {
				TestCase.logger.warn(String.format("testcase id was not set, name is set to '%s'.", Convert.UNKNOWN));
			}
			return Convert.UNKNOWN;
		}
		return id;
	}

	/**
	 * @brief method that returns, if the testcase has failed
	 */
	public boolean isFailed() {
		return isFailed;
	}

	public void setFailed(final boolean isFailed) {
		this.isFailed = isFailed;
	}

	@SuppressWarnings("rawtypes")
	public Artefact getArtefact() {
		return artefact;
	}

	@SuppressWarnings("rawtypes")
	public void setArtefact(final Artefact artefact) {
		this.artefact = artefact;
	}

	public int getShutdownCounter() {
		return shutdownCounter;
	}

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