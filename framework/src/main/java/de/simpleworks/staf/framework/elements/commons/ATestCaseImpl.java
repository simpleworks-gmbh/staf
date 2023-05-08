package de.simpleworks.staf.framework.elements.commons;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.simpleworks.staf.commons.annotation.Testcase;
import de.simpleworks.staf.framework.elements.commons.properties.TestCaseProperties;
import de.simpleworks.staf.commons.enums.Result;
import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.report.StepReport;
import de.simpleworks.staf.commons.report.TestcaseReport;
import de.simpleworks.staf.commons.report.artefact.Artefact;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsDate; 
import de.simpleworks.staf.framework.enums.CreateArtefactEnum;
import de.simpleworks.staf.framework.reporter.manager.ReporterManager;
import de.simpleworks.staf.framework.util.TestCaseUtils;
import net.lightbody.bmp.BrowserMobProxyServer;

public abstract class ATestCaseImpl implements ITestCase{

	private static final Logger logger = LoggerFactory.getLogger(ATestCaseImpl.class);
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
	
	
	@SuppressWarnings("deprecation")
	public ATestCaseImpl() {
		
		if (this.getClass().getAnnotation(Testcase.class) == null) {
			final String msg = String.format("'%s' does not have the annotation '%s'.", this.getClass().getName(),
					Testcase.class.getName());
			logger.error(msg);
			throw new InstantiationError(msg);
		}
		
		Object ob = null;

		try{
			@SuppressWarnings("rawtypes")
			Class clazz =  Class.forName(testcaseProperties.getReporterManagerClass());
			ob = clazz.newInstance();
		}
		catch(Exception ex) {
			ex.printStackTrace();
			final String msg = String.format("'%s' does not have the annotation '%s'.", this.getClass().getName(),
					Testcase.class.getName());
			ATestCaseImpl.logger.error(msg);
			throw new InstantiationError(msg);
		}
		
		if(!(ob instanceof ReporterManager)) {
			final String msg =String.format("object '%s' is no instance of '%s'.",ob.getClass(), ReporterManager.class);
			ATestCaseImpl.logger.error(msg);
			throw new InstantiationError(msg);
		}
		
		reporter = (ReporterManager) ob;
		// initialize lists of step methods
		shutdownCounter = getStepsSize(); 
		extractedValues = new HashMap<>();

		testcaseReport = new TestcaseReport(getTestCaseName());
	}
	

	/**
	 * @brief method that starts the Testcase (manages execution of
	 *        {@code bootstrap})
	 * @return true if the testacse is running, false if not.
	 */
	@Override
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
			ATestCaseImpl.logger.error("Cannot start instance of TestCase.", ex);
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
		final CreateArtefactEnum createartefact = testcaseProperties.getCreateArtefactOn();
		if (ATestCaseImpl.logger.isDebugEnabled()) {
			ATestCaseImpl.logger.debug(String.format("Create Artefact on '%s'.", createartefact.getValue()));
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
			if (ATestCaseImpl.logger.isDebugEnabled()) {
				ATestCaseImpl.logger.debug(String.format("No action defined for '%s' will return '%s'.",
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
	@Override
	public final boolean stop(final StepReport stepReport) {
		if (stepReport == null) {
			ATestCaseImpl.logger.error(String.format("The testcase '%s' is missing a stepreport.", getTestCaseName()));
		} else {
			try {
				if (!shouldArtefactBeCreated(stepReport.getResult())) {
					stepReport.setArtefact(null);
				}
				testcaseReport.setStopTime(stepReport.getStopTime());
				testcaseReport.addStep(stepReport);
			} catch (final SystemException ex) {
				ATestCaseImpl.logger
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
	
		
	@Override
	public void writeDownResults() {
		final TestcaseReport report = getTestcaseReport();
		if (report == null) {
			if (ATestCaseImpl.logger.isDebugEnabled()) {
				ATestCaseImpl.logger.debug("report can't be null, no report will be written.");
			}
		} else { 	
			reporter.saveReport(report);
		}
	}
	
	public void markStepExecution(final String mark)  {
		
		if (Convert.isEmpty(mark)) {
			ATestCaseImpl.logger.info("no mark has been defined, to identify the step.");
			return;
		}
		final BrowserMobProxyServer proxy = getProxy();
		if (proxy == null) {
			if (ATestCaseImpl.logger.isDebugEnabled()) {
				ATestCaseImpl.logger.debug("steps will not be marked, because the proxy is null.");
			}
			return;
		}
		// check if random nuance was already created
		if (Convert.isEmpty(nuance)) {
			nuance = UUID.randomUUID().toString();
		}
		proxy.removeHeader(testcaseProperties.getTestStepHeaderName());
		proxy.addHeader(testcaseProperties.getTestStepHeaderName(), createXRequestId(mark));
		proxy.addHeader("Cookie", String.format("%s_%s=%s", testcaseProperties.getTestCaseHeaderName(),
				getTestCaseName(), nuance));
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
	
	
	public final String getTestCaseName() {
		final Testcase testcase = this.getClass().getAnnotation(Testcase.class);
		if (testcase == null) {
			if (ATestCaseImpl.logger.isWarnEnabled()) {
				ATestCaseImpl.logger
						.warn(String.format("testcase annotation was not set, name is set to '%s'.", Convert.UNKNOWN));
			}
			return Convert.UNKNOWN;
		}
		final String id = testcase.id();
		if (Convert.isEmpty(id)) {
			if (ATestCaseImpl.logger.isWarnEnabled()) {
				ATestCaseImpl.logger.warn(String.format("testcase id was not set, name is set to '%s'.", Convert.UNKNOWN));
			}
			return Convert.UNKNOWN;
		}
		return id;
	}
	
	public final void setExtractedValues(final Map<String, Map<String, String>> map) {
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
	
	public int getStepsSize() {
		return TestCaseUtils.fetchStepMethods(this.getClass()).size();
	}
	
	/**
	 * @brief getters setters
	 */
	
	public Map<String, Map<String, String>> getExtractedValues() {
		return extractedValues;
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
	
	public boolean isFailed() {
		return isFailed;
	}

	public void setFailed(final boolean isFailed) {
		this.isFailed = isFailed;
	}
	
	public TestcaseReport getTestcaseReport() {
		return testcaseReport;
	}
}
