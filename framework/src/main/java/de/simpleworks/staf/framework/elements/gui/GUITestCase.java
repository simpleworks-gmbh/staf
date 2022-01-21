package de.simpleworks.staf.framework.elements.gui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import com.google.inject.Inject;
import com.google.inject.Module;

import de.simpleworks.staf.commons.annotation.Step;
import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.manager.WebDriverManager;
import de.simpleworks.staf.commons.report.artefact.Screenshot;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.framework.api.proxy.ProxyUtils;
import de.simpleworks.staf.framework.elements.api.RewriteUrlObject;
import de.simpleworks.staf.framework.elements.commons.TestCase;
import de.simpleworks.staf.framework.gui.webdriver.module.DriverModule;
import de.simpleworks.staf.framework.gui.webdriver.module.WebDriverManagerImpl;
import de.simpleworks.staf.framework.util.AssertionUtils;
import de.simpleworks.staf.framework.util.TestCaseUtils;
import net.lightbody.bmp.BrowserMobProxyServer;

public abstract class GUITestCase extends TestCase {
	private static final Logger logger = LogManager.getLogger(GUITestCase.class);

	@Inject
	private WebDriverManager driverManager;

	@Inject
	private WebDriverManagerImpl drivermanagerimpl;

	private int currentStep;

	public GUITestCase(final Module... modules) {
		super(UtilsCollection.add(Module.class, modules, new DriverModule()));
	}

	private void configureProxy() {
		final BrowserMobProxyServer proxy = drivermanagerimpl.getProxy();
		if (proxy == null) {
			if (GUITestCase.logger.isDebugEnabled()) {
				GUITestCase.logger.debug("steps will not be marked, because the proxy is null.");
			}

			return;
		}

		for (final RewriteUrlObject rewrittenUrl : getRewriteUrls()) {
			try {
				if (rewrittenUrl != null) {
					rewrittenUrl.validate();
					if (GUITestCase.logger.isDebugEnabled()) {
						GUITestCase.logger.debug(String.format("matching requests will be rewritten from '%s' to '%s'.",
								rewrittenUrl.getPattern(), rewrittenUrl.getRewriteExpression()));
					}

					ProxyUtils.addRewriteRules(proxy, rewrittenUrl);
				}
			} catch (final Exception ex) {
				GUITestCase.logger.error("can't add rewrite rule.", ex);
				continue;
			}
		}
	}

	@Override
	public BrowserMobProxyServer getProxy() {
		return drivermanagerimpl.getProxy();
	}

	@Override
	public void bootstrap() throws Exception {
		if (!drivermanagerimpl.isRunning()) {
			drivermanagerimpl.startDriver();
		}

		configureProxy();

		drivermanagerimpl.startProxy();

		currentStep = 0;
	}

	@Override
	public void shutdown() throws Exception {
		if (drivermanagerimpl.isRunning()) {
			drivermanagerimpl.quitProxy();
			drivermanagerimpl.quitDriver();
		}
	}

	public final WebDriver getDriver() throws SystemException {
		final WebDriver driver = driverManager.get();

		if (driver == null) {
			final String msg = "driver can't be null.";
			GUITestCase.logger.error(msg);
			throw new SystemException(msg);
		}

		return driver;
	}

	@Override
	public Screenshot createArtefact() {
		Screenshot result = null;

		try {
			final TakesScreenshot screenshot = (TakesScreenshot) getDriver();
			if (screenshot == null) {
				throw new IllegalArgumentException("screenshot can't be null.");
			}

			result = new Screenshot(screenshot);
		} catch (final Exception ex) {
			GUITestCase.logger.error("can't create artifact.", ex);
		}

		return result;
	}

	private Method getNextTeststep() throws Exception {

		List<Method> methods = TestCaseUtils.fetchStepMethods(this.getClass());

		if (Convert.isEmpty(methods)) {
			throw new Exception("methods can't be null or empty.");
		}

		final Method result = methods.get(currentStep++);

		return result;
	}

	@Override
	public void executeTestStep() throws Exception {
		// add error handling
		final Method testStep = getNextTeststep();

		final GUITestResult result = runTeststep(testStep);
		AssertionUtils.assertTrue(result.getErrormessage(), result.isSuccessfull());
	}

	private GUITestResult runTeststep(Method method) throws Exception {

		if (method == null) {
			throw new IllegalArgumentException("method can't be null.");
		}

		final Step step = method.getAnnotation(Step.class);

		if (step == null) {
			throw new IllegalArgumentException(String.format("method \"%s\" is not annotated with \"%s\".",
					method.getName(), Step.class.getName()));
		}

		final GUITestResult result = new GUITestResult();
		result.setSuccessfull(false);

		try {
			method.invoke(this);
			result.setSuccessfull(true);
		} catch (Throwable th) {

			if (th instanceof IllegalAccessException) {
				GUITestCase.logger
						.error(String.format("method \"%s\" is not accessible via reflection.", method.getName()));
				throw th;
			}

			else if (th instanceof IllegalArgumentException) {

				GUITestCase.logger.error(
						String.format(
								"method \"%s\" was called without parameters, although parameter \"[]\" were expected.",
								method.getName(), String
										.join(",",
												UtilsCollection.toList(method.getParameters()).stream()
														.map(param -> param.getName()).collect(Collectors.toList()))),
						th);
				throw th;
			}

			else if (th instanceof InvocationTargetException) {
				GUITestCase.logger
						.error(String.format("method \"%s\" caused an exception, while it was called via reflection.",
								method.getName()), th);
				throw th;
			}

			// for every failing assertion and runtime error
			else {

				final String msg = String.format("Test Step '%s' has failed.", step.description());
				GUITestCase.logger.error(msg, th);
				result.setErrormessage(th.getMessage());

				result.setSuccessfull(false);
			}

		}

		return result;
	}
}
