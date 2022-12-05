package de.simpleworks.staf.framework.elements.api;

import java.io.File;
import java.util.ArrayList;
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
import de.simpleworks.staf.commons.api.ResponseEntity;
import de.simpleworks.staf.commons.enums.ContentTypeEnum;
import de.simpleworks.staf.commons.enums.ValidateMethodEnum;
import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.mapper.Mapper;
import de.simpleworks.staf.commons.mapper.api.MapperAPITeststep;
import de.simpleworks.staf.commons.report.artefact.HarFile;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.commons.utils.UtilsIO;
import de.simpleworks.staf.framework.api.httpclient.HttpClient;
import de.simpleworks.staf.framework.api.httpclient.TeststepProvider;
import de.simpleworks.staf.framework.api.httpclient.properties.HttpClientProperties;
import de.simpleworks.staf.framework.elements.commons.TemplateTestCase;
import de.simpleworks.staf.framework.util.AssertionUtils;
import de.simpleworks.staf.framework.util.HttpClientFactory;
import de.simpleworks.staf.framework.util.HttpResponseUtils;
import de.simpleworks.staf.framework.util.assertion.File_ComparerAssertionValidator;
import de.simpleworks.staf.framework.util.assertion.HeaderAssertionValidator;
import de.simpleworks.staf.framework.util.assertion.JSONPATHAssertionValidator;
import de.simpleworks.staf.framework.util.assertion.ResponseBodyAssertionValidator;
import de.simpleworks.staf.framework.util.assertion.XPATHAssertionValidator;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.core.har.Har;

public abstract class APITestCase extends TemplateTestCase<APITeststep, HttpResponse> {
	private static final Logger logger = LogManager.getLogger(APITestCase.class);
	public final static String ENVIRONMENT_VARIABLES_NAME = "APITestCase";
	private static final HttpClientProperties httpClientProperties = HttpClientProperties.getInstance();
 
	private String currentstepname;
	private HttpRequest currentHttpRequest;
	private HttpResponse currentExpetcedHttpResponse;
	private Assertion[] currentAssertions; 
	
	private final Map<String, ResponseEntity> extractedResponseEntities = new HashMap<>();
	private final HttpClient client;

	protected APITestCase(final String resource, final Module... modules) throws SystemException {
		super(resource, ENVIRONMENT_VARIABLES_NAME, new MapperAPITeststep(), modules);
		this.client = HttpClientFactory.createHttpClient();  
	}

	private static final Map<String, String> checkHeader(final HttpResponse response, final Assertion assertion) {
		return new HeaderAssertionValidator().validateAssertion(response, assertion);
	}

	private static final Map<String, String> checkXpath(final HttpResponse response, final Assertion assertion) {
		return new XPATHAssertionValidator().validateAssertion(response, assertion);
	}

	private static final Map<String, String> checkJSonPath(final HttpResponse response, final Assertion assertion) {
		return new JSONPATHAssertionValidator().validateAssertion(response, assertion);
	}

	private static final Map<String, String> checkResponseBody(final HttpResponse response, final Assertion assertion) {
		return new ResponseBodyAssertionValidator().validateAssertion(response, assertion);
	}

	private static final Map<String, String> checkFile(final HttpResponse response, final Assertion assertion) {
		return new File_ComparerAssertionValidator().validateAssertion(response, assertion);
	}

	protected Map<String, String> runAssertion(final HttpResponse response, final Assertion assertion)
			throws SystemException {

		final Map<String, String> results;

		final ValidateMethodEnum method = assertion.getValidateMethod();

		switch (method) {
		case HEADER:
			results = APITestCase.checkHeader(response, assertion);
			break;
		case XPATH:
			results = APITestCase.checkXpath(response, assertion);
			break;
		case JSONPATH:
			results = APITestCase.checkJSonPath(response, assertion);
			break;
		case RESPONSEBODY:
			results = APITestCase.checkResponseBody(response, assertion);
			break;
		case FILE_COMPARER:
			results = APITestCase.checkFile(response, assertion);
			break;
		default:
			throw new SystemException(
					String.format("The validateMethod '%s' is not implemented yet.", method.getValue()));
		}

		return results;
	}

	/**
	 * @brief method, that fetches entities from the response {@param content} as it
	 *        as requested from {@param expectedResponse}.
	 * @param HttpResponse expectedResponse
	 * @param String       content (from a received response)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object[] fetchResponseEntity(final HttpResponse expectedResponse, final String content) {

		if (expectedResponse == null) {
			throw new IllegalArgumentException("expectedResponse can't be null.");
		}

		if (!expectedResponse.validate()) {
			throw new IllegalArgumentException("expectedResponse is invalid.");
		}

		if (Convert.isEmpty(content)) {
			throw new IllegalArgumentException("content can't be null or empty string.");
		}

		final ResponseEntity entity = expectedResponse.getEntity();

		if (entity == null) {
			throw new IllegalArgumentException("entity can't be null.");
		}

		if (!(entity.validate())) {
			throw new IllegalArgumentException(String.format("entity is invalid '%s'.", entity));
		}

		Object[] result = new Object[0];

		final String className = entity.getClassname();
		Class<?> entityClass = null;

		try {
			entityClass = Class.forName(className);
		} catch (Exception ex) {
			final String msg = String.format("can't load entityClass '%s'.", className);
			APITestCase.logger.error(msg, ex);
			throw new RuntimeException(msg);
		}

		final String mapperClassName = entity.getMapperClassname();
		Class<?> mapperClass = null;

		try {
			mapperClass = Class.forName(mapperClassName);
		} catch (Exception ex) {
			final String msg = String.format("can't load mapperClass '%s'.", mapperClassName);
			APITestCase.logger.error(msg, ex);
			throw new RuntimeException(msg);
		}

		Mapper mapper = null;

		try {
			mapper = (Mapper) mapperClass.newInstance();
		} catch (InstantiationException ex) {
			final String msg = String.format("can't initialize mapper from class '%s'.", mapperClassName);
			APITestCase.logger.error(msg, ex);
			throw new RuntimeException(msg);
		} catch (IllegalAccessException ex) {
			final String msg = String.format("can't access mapper.", mapperClassName);
			APITestCase.logger.error(msg, ex);
			throw new RuntimeException(msg);
		}

		List pojos = new ArrayList<>();
		try {
			pojos = mapper.readAll(content);
		} catch (SystemException ex) {
			final String msg = "can't parse pojos.";
			APITestCase.logger.error(msg, ex);
			throw new RuntimeException(msg);
		}

		if (Convert.isEmpty(pojos)) {
			throw new RuntimeException("no response entities were fetched, expected is at least one.");
		}

		try {

			result = new Object[pojos.size()];

			for (int itr = 0; itr < pojos.size(); itr += 1) {

				try {
					final Object entit = pojos.get(itr);

					Object obj = entityClass.cast(entit);
					result[itr] = obj;
				} catch (Exception ex) {

					String msg = Convert.EMPTY_STRING;

					if (ex instanceof ClassCastException) {
						msg = String.format("can't cast entity to instance of '%s'.", entityClass.getName());
					} else {
						msg = String.format("can't fetch entities.");
					}
					APITestCase.logger.error(msg, ex);
					throw new RuntimeException(msg);
				}
			}
		} catch (Exception ex) {
			final String msg = String.format("can't fetch entities from response '%s'.", content);
			APITestCase.logger.error(msg, ex);
			result = new Object[0];
		}

		return result;
	}

	/**
	 * @brief method, that checks, if a {@param HttpRequest} request, returns the
	 *        expected {@param HttpResponse} response.
	 */
	protected APITestResult checkRequest(final HttpRequest request, final HttpResponse expectedResponse,
			final List<Assertion> assertions) {

		if (request == null) {
			throw new IllegalArgumentException("request can't be null.");
		}

		if (!request.validate()) {
			throw new IllegalArgumentException(String.format("request \"%s\" is invalid.", request));
		}

		if (expectedResponse == null) {
			throw new IllegalArgumentException("expectedResponse can't be null.");
		}

		if (!expectedResponse.validate()) {
			throw new IllegalArgumentException(String.format("expectedResponse \"%s\" is invalid.", expectedResponse));
		}

		final APITestResult result = new APITestResult(request, expectedResponse);
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
			HttpResponseUtils.compare(response, expectedResponse);

			if (expectedResponse.getEntity() != null) {
				final Object[] entities = fetchResponseEntity(expectedResponse, response.getJsonBody());
				final ResponseEntity entity = new ResponseEntity(expectedResponse.getEntity());
				entity.setEntities(entities);

				response.setEntity(entity);
				result.setResponseEntities(entity);
			}

			if (!Convert.isEmpty(assertions)) {
				final Map<String, String> values = validateAssertions(response, UtilsCollection.toList(assertions));
				result.setExtractedValues(values);
			}

			result.setSuccessfull(true);
		} catch (final Throwable th) {
			final String msg = String.format(
					"Request '%s' failed, expected is Response '%s', but the Response is '%s'.", request,
					expectedResponse, response);
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

		int timeout = httpClientProperties.getTimeout();

		try {

			if (timeout > -1) {
				Thread.sleep(timeout * 1000);
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
		if (apiteststep == null) {
			return;
		}
		
		if (getExtractedValues() == null) {
			throw new IllegalStateException("extractedValues can't be null.");
		}
		
		if (!getExtractedValues().keySet().isEmpty()) {
			apiteststep = updateTeststep(apiteststep, getExtractedValues());
		}
		
		if (!apiteststep.validate()) {
			throw new IllegalArgumentException(String.format("Step '%s' is invalid.", apiteststep));
		}
		
		if (APITestCase.logger.isDebugEnabled()) {
			APITestCase.logger.debug(String.format("next apiteststep '%s'.", apiteststep));
		}
		
		currentstepname = apiteststep.getName();
		currentHttpRequest = apiteststep.getRequest();
		currentExpetcedHttpResponse = apiteststep.getResponse();
		try {
			if (ContentTypeEnum.JSON.equals(currentHttpRequest.getContentType())) {
				if (!Convert.isEmpty(currentHttpRequest.getBodyFileName())) {
					final File file = new File(currentHttpRequest.getBodyFileName());
					final String content = UtilsIO.getAllContentFromFile(file);
					currentHttpRequest.setBody(content);
				}
			}
		} catch (final Exception ex) {
			final String msg = "can't fetch response body.";
			APITestCase.logger.error(msg, ex);
			throw new SystemException(msg);
		}
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
		addResponseEntitiy(currentstepname, result.getResponseEntities());
	}

	protected void addResponseEntitiy(final String key, final ResponseEntity entity) {
		if (Convert.isEmpty(key)) {
			throw new IllegalArgumentException("key can't be null or empty string.");
		}

		if (entity == null) {
			return;
		}

		if (!(entity.validate())) {
			throw new IllegalArgumentException(String.format("entity is invalid '%s'.", entity));
		}

		if (APITestCase.logger.isDebugEnabled()) {
			APITestCase.logger.debug(String.format("add variables for key: '%s'.", key));
		}

		if (getExtractedResponseEntities() == null) {
			throw new IllegalStateException("extractedResponseEntities can't be null.");
		}

		getExtractedResponseEntities().put(key, entity);
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

	public Map<String, ResponseEntity> getExtractedResponseEntities() {
		return extractedResponseEntities;
	}
}