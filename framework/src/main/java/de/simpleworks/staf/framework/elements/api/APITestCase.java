package de.simpleworks.staf.framework.elements.api;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Module;

import de.simpleworks.staf.commons.api.APITeststep;
import de.simpleworks.staf.commons.api.Assertion;
import de.simpleworks.staf.commons.api.HttpRequest;
import de.simpleworks.staf.commons.api.HttpResponse;
import de.simpleworks.staf.commons.consts.CommonsConsts;
import de.simpleworks.staf.commons.enums.ContentTypeEnum;
import de.simpleworks.staf.commons.enums.ValidateMethodEnum;
import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.mapper.api.MapperAPITeststep;
import de.simpleworks.staf.commons.report.artefact.HarFile;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.commons.utils.UtilsIO;
import de.simpleworks.staf.framework.api.httpclient.HttpClient;
import de.simpleworks.staf.framework.api.httpclient.TeststepProvider;
import de.simpleworks.staf.framework.elements.commons.TemplateTestCase;
import de.simpleworks.staf.framework.util.AssertionUtils;
import de.simpleworks.staf.framework.util.HttpResponseUtils;
import de.simpleworks.staf.framework.util.assertion.File_ComparerAssertionValidator;
import de.simpleworks.staf.framework.util.assertion.JSONPATHAssertionValidator;
import de.simpleworks.staf.framework.util.assertion.XPATHAssertionValidator;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.core.har.Har;

public abstract class APITestCase extends TemplateTestCase<APITeststep, HttpResponse> {
	private static final Logger logger = LogManager.getLogger(APITestCase.class);

	private final static String ENVIRONMENT_VARIABLES_NAME = "APITestCase";

	private String currentstepname;
	private HttpRequest currentHttpRequest;
	private HttpResponse currentExpetcedHttpResponse;
	private Assertion[] currentAssertions;

	private final HttpClient client = new HttpClient();

	protected APITestCase(final String resource, final Module... modules) throws SystemException {
		super(resource, ENVIRONMENT_VARIABLES_NAME, new MapperAPITeststep(), modules);
	}

	private static final Map<String, String> checkXpath(final HttpResponse response, final Assertion assertion) {
		return new XPATHAssertionValidator().validateAssertion(response, assertion);
	}

	private static final Map<String, String> checkJSon(final HttpResponse response, final Assertion assertion) {
		return new JSONPATHAssertionValidator().validateAssertion(response, assertion);
	}

	private static final Map<String, String> checkFile(final HttpResponse response, final Assertion assertion) {
		return new File_ComparerAssertionValidator().validateAssertion(response, assertion);
	}

	@Override
	protected Map<String, String> validateAssertions(HttpResponse response, List<Assertion> assertions)
			throws SystemException {

		if (response == null) {
			throw new IllegalArgumentException("response can't be null.");
		}

		HttpResponse httpResponse = response;

		if (Convert.isEmpty(assertions)) {
			throw new IllegalArgumentException("assertions can't be null or empty.");
		}

		if (APITestCase.logger.isDebugEnabled()) {
			APITestCase.logger.debug("run assertions");
		}

		final Map<String, String> result = new HashMap<>();
		for (final Assertion assertion : assertions) {
			if (APITestCase.logger.isDebugEnabled()) {
				APITestCase.logger.debug(String.format("work with assertion: '%s'.", assertion));
			}
			assertion.validate();

			final ValidateMethodEnum method = assertion.getValidateMethod();
			final Map<String, String> results;
			switch (method) {
			case XPATH:
				results = APITestCase.checkXpath(httpResponse, assertion);
				break;

			case JSONPATH:
				results = APITestCase.checkJSon(httpResponse, assertion);
				break;

			case FILE_COMPARER:
				results = APITestCase.checkFile(httpResponse, assertion);
				break;

			default:
				throw new SystemException(
						String.format("The validateMethod '%s' is not implemented yet.", method.getValue()));
			}

			results.keySet().stream().forEach(key -> {
				result.put(key, results.get(key));
			});
		}

		return result;
	}

	/**
	 * @brief method, that checks, if a {@param HttpRequest} request, returns the
	 *        expected {@param HttpResponse} response.
	 */
	protected APITestResult checkRequest(final HttpRequest request, final HttpResponse expected,
			final List<Assertion> assertions) {
		if (request == null) {
			throw new IllegalArgumentException("request can't be null.");
		}

		if (expected == null) {
			throw new IllegalArgumentException("expectedResponse can't be null.");
		}

		final APITestResult result = new APITestResult(request, expected);
		if (APITestCase.logger.isDebugEnabled()) {
			APITestCase.logger.debug(String.format("created test result: %s.", result));
		}

		HttpResponse response = null;
		try {
			response = doRequest(request);
			if (APITestCase.logger.isDebugEnabled()) {
				APITestCase.logger.debug(String.format("get response: %s.", response));
			}
			result.setResponse(response);

			HttpResponseUtils.compare(response, expected);

			if (!Convert.isEmpty(assertions)) {
				final Map<String, String> values = validateAssertions(response, UtilsCollection.toList(assertions));
				result.setExtractedValues(values);
			}

			result.setSuccessfull(true);
		} catch (final Throwable th) {
			final String msg = String.format(
					"Request '%s' failed, expected is Response '%s', but the Response is '%s'.", request, expected,
					response);
			APITestCase.logger.error(msg, th);

			result.setErrormessage(th.getMessage());
			result.setSuccessfull(false);
		}

		return result;
	}

	protected HttpResponse doRequest(final HttpRequest request) throws SystemException {
		if (request == null) {
			throw new IllegalArgumentException("request can't be null.");
		}

		// apply pacing if it's defined.
		String timeout = Convert.EMPTY_STRING;
		try {

			timeout = System.getProperty(CommonsConsts.API_TIMEOUT, null);

			if (!Convert.isEmpty(timeout)) {
				Thread.sleep(Integer.parseInt(timeout) * 1000);
			}

		} catch (Exception ex) {

			if (ex instanceof NumberFormatException) {
				APITestCase.logger.error(String.format("pacing \"%s\" can't be parsed to an integer.", timeout), ex);
			}

			else if (ex instanceof InterruptedException) {
				APITestCase.logger.error(String.format("can't wait whole pacing \"%s\".", timeout), ex);
			}

			else {
				APITestCase.logger.error("pacing can't be applied.", ex);
			}
		}

		final BrowserMobProxyServer proxy = client.getBrowserMobProxyServer();
		if (proxy != null) {
			proxy.newHar(currentstepname);
		}

		return client.doRequest(request);
	}

	protected final APITestResult doRequest() {
		return checkRequest(currentHttpRequest, currentExpetcedHttpResponse, UtilsCollection.toList(currentAssertions));
	}

	@Override
	protected APITeststep updateTeststep(final APITeststep step, final Map<String, Map<String, String>> values)
			throws SystemException {
		if (step == null) {
			throw new IllegalArgumentException("step can't be null.");
		}

		if (!step.validate()) {
			throw new IllegalArgumentException(String.format("Step '%s' is invalid.", step));
		}

		if (values == null) {
			throw new IllegalArgumentException("value can't be null.");
		}

		if (values.keySet().isEmpty()) {
			throw new IllegalArgumentException("extractedValues can't be empty.");
		}

		if (APITestCase.logger.isDebugEnabled()) {
			APITestCase.logger.debug("update values.");
		}

		final APITeststep result = step;
		try {
			final HttpRequest request = step.getRequest();
			final HttpRequest updatedRequest = updateFields(HttpRequest.class, request, values);
			result.setRequest(updatedRequest);

			final HttpResponse response = step.getResponse();
			final HttpResponse updatedResponse = updateFields(HttpResponse.class, response, values);
			result.setResponse(updatedResponse);

			if (!Convert.isEmpty(Arrays.asList(step.getAssertions()))) {
				final Assertion[] assertions = step.getAssertions();
				final Assertion[] updatedAssertions = new Assertion[assertions.length];

				for (int itr = 0; itr < assertions.length; itr += 1) {
					final Assertion assertion = updateFields(Assertion.class, assertions[itr], values);
					updatedAssertions[itr] = assertion;
				}

				result.setAssertions(updatedAssertions);
			}
		} catch (final Exception ex) {
			final String message = "can't update api test step.";
			APITestCase.logger.error(message, ex);
			throw new SystemException(message);
		}

		return result;
	}

	@Override
	protected final void getNextTeststep() throws SystemException {

		TeststepProvider<APITeststep> provider = getProvider();

		if (provider == null) {
			throw new IllegalArgumentException("provider can't be null.");
		}

		APITeststep apiteststep = provider.get();

		if (!apiteststep.validate()) {
			throw new IllegalArgumentException(String.format("Step '%s' is invalid.", apiteststep));
		}

		if (getExtractedValues() == null) {
			throw new IllegalStateException("extractedValues can't be null.");
		}

		if (APITestCase.logger.isDebugEnabled()) {
			APITestCase.logger.debug(String.format("next apiteststep '%s'.", apiteststep));
		}

		if (!getExtractedValues().keySet().isEmpty()) {
			apiteststep = updateTeststep(apiteststep, getExtractedValues());
		}

		currentstepname = apiteststep.getName();
		currentHttpRequest = apiteststep.getRequest();
		currentExpetcedHttpResponse = apiteststep.getResponse();

		try {
			if (ContentTypeEnum.JSON.equals(currentExpetcedHttpResponse.getContentType())) {
				if (!Convert.isEmpty(currentExpetcedHttpResponse.getBodyFileName())) {
					final File file = new File(currentExpetcedHttpResponse.getBodyFileName());
					final String content = UtilsIO.getAllContentFromFile(file);
					currentExpetcedHttpResponse.setJsonBody(content);
				}
			}
		} catch (final Exception ex) {
			final String msg = "can't fetch response body.";
			APITestCase.logger.error(msg, ex);
			throw new SystemException(msg);
		}

		currentAssertions = apiteststep.getAssertions();
	}

	@Override
	public BrowserMobProxyServer getProxy() {
		return client.getBrowserMobProxyServer();
	}

	@Override
	public void bootstrap() throws Exception {
		if (client == null) {
			final String msg = "client can't be null.";
			APITestCase.logger.error(msg);
			throw new Exception(msg);
		}

		final BrowserMobProxyServer proxy = client.getBrowserMobProxyServer();
		if ((proxy != null) && !proxy.isStarted()) {
			final String msg = String.format("Proxy at '%s:%d', can't be started.",
					proxy.getServerBindAddress().getHostName(), Integer.valueOf(proxy.getPort()));
			APITestCase.logger.error(msg);
			throw new Exception(msg);
		}

		if (getExtractedValues() == null) {
			final String msg = "extractedValues can't be null.";
			APITestCase.logger.error(msg);
			throw new Exception(msg);
		}
	}

	@Override
	public void shutdown() throws Exception {
		final BrowserMobProxyServer proxy = client.getBrowserMobProxyServer();
		if ((proxy != null) && !proxy.isStopped()) {
			proxy.stop();
			if (!proxy.isStopped()) {
				final String msg = String.format("Proxy at '%s:%d', can't be stopped.",
						proxy.getServerBindAddress().getHostName(), Integer.valueOf(proxy.getPort()));
				APITestCase.logger.error(msg);
				throw new Exception(msg);
			}
		}
	}

	@Override
	public HarFile createArtefact() {
		final BrowserMobProxyServer proxy = client.getBrowserMobProxyServer();
		if (proxy == null) {
			return null;
		}

		HarFile result = null;
		try {
			final Har har = proxy.getHar();
			if (har != null) {
				final File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".har");
				if (APITestCase.logger.isDebugEnabled()) {
					APITestCase.logger.debug(String.format("write HAR into file: '%s'.", tempFile));
				}
				tempFile.deleteOnExit();

				har.writeTo(tempFile);
				result = new HarFile(tempFile);
			}
		} catch (final Exception ex) {
			APITestCase.logger.error("can't create artifact.", ex);
		}

		return result;
	}

	@Override
	public void executeTestStep() throws Exception {

		// add error handling
		getNextTeststep();

		final HttpRequest request = getCurrentHttpRequest();
		final HttpResponse expectedResponse = getCurrentExpectedHttpResponse();

		final Assertion[] assertions = getCurrentAssertions();

		final APITestResult result = checkRequest(request, expectedResponse, UtilsCollection.toList(assertions));
		AssertionUtils.assertTrue(result.getErrormessage(), result.isSuccessfull());

		addExtractedValues(currentstepname, result.getExtractedValues());
	}

	protected HttpRequest getCurrentHttpRequest() {
		return currentHttpRequest;
	}

	protected HttpResponse getCurrentExpectedHttpResponse() {
		return currentExpetcedHttpResponse;
	}

	protected Assertion[] getCurrentAssertions() {
		return currentAssertions;
	}

	public HttpClient getHttpClient() {
		return client;
	}
}