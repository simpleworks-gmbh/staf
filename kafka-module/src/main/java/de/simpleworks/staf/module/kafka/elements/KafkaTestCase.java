package de.simpleworks.staf.module.kafka.elements;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Module;

import de.simpleworks.staf.commons.api.Assertion;
import de.simpleworks.staf.commons.api.HttpResponse;
import de.simpleworks.staf.commons.enums.ValidateMethodEnum;
import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.report.artefact.CsvFile;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.framework.api.httpclient.TeststepProvider;
import de.simpleworks.staf.framework.elements.api.APITestCase;
import de.simpleworks.staf.framework.elements.api.RewriteUrlObject;
import de.simpleworks.staf.framework.elements.commons.TemplateTestCase;
import de.simpleworks.staf.framework.util.AssertionUtils;
import de.simpleworks.staf.framework.util.assertion.HeaderAssertionValidator;
import de.simpleworks.staf.module.kafka.api.IKafkaRequest;
import de.simpleworks.staf.module.kafka.api.IKafkaResponse;
import de.simpleworks.staf.module.kafka.api.IKafkaTeststep;
import de.simpleworks.staf.module.kafka.api.KafkaConsumeRecord;
import de.simpleworks.staf.module.kafka.api.KafkaConsumeRequest;
import de.simpleworks.staf.module.kafka.api.KafkaConsumeResponse;
import de.simpleworks.staf.module.kafka.api.KafkaProduceRequest;
import de.simpleworks.staf.module.kafka.api.KafkaProduceRequestContent;
import de.simpleworks.staf.module.kafka.api.KafkaProduceRequestHeader;
import de.simpleworks.staf.module.kafka.api.KafkaProduceRequestKey;
import de.simpleworks.staf.module.kafka.api.KafkaProduceResponse;
import de.simpleworks.staf.module.kafka.api.kafkaclient.KafkaClient;
import de.simpleworks.staf.module.kafka.api.mapper.MapperKafkaTeststep;
import de.simpleworks.staf.module.kafka.consume.KafkaTeststep;
import de.simpleworks.staf.module.kafka.elements.api.KafkaTestResult;
import de.simpleworks.staf.module.kafka.util.KAFKAMessageAssertionValidator;
import net.lightbody.bmp.BrowserMobProxyServer;

public class KafkaTestCase extends TemplateTestCase<IKafkaTeststep, KafkaConsumeResponse> {

	private static final Logger logger = LogManager.getLogger(KafkaTestCase.class);
	private final static String ENVIRONMENT_VARIABLES_NAME = "KafkaTestCase";

	private String currentstepname;

	private IKafkaRequest kafkaRequest;
	private IKafkaResponse kafkaResponse;

	private KafkaTestResult currentResult;
	private Assertion[] currentAssertions;

	protected KafkaTestCase(String resource, Module... modules) throws SystemException {
		super(resource, ENVIRONMENT_VARIABLES_NAME, new MapperKafkaTeststep(), modules);
	}

	private static final Map<String, String> checkKafkaMessage(final KafkaConsumeResponse response,
			final Assertion assertion) {
		return new KAFKAMessageAssertionValidator().validateAssertion(response, assertion);
	}

	@Override
	protected Map<String, String> runAssertion(final KafkaConsumeResponse response, final Assertion assertion)
			throws SystemException {

		final Map<String, String> results;

		final ValidateMethodEnum method = assertion.getValidateMethod();

		switch (method) {
		case KAFKAMESSAGE_VALIDATION:
			results = KafkaTestCase.checkKafkaMessage(response, assertion);
			break;
		default:
			throw new SystemException(
					String.format("The validateMethod '%s' is not implemented yet.", method.getValue()));
		}

		return results;
	}

	@Override
	protected IKafkaTeststep updateTeststep(IKafkaTeststep step, Map<String, Map<String, String>> values)
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
		if (KafkaTestCase.logger.isDebugEnabled()) {
			KafkaTestCase.logger.debug("update values.");
		}
		final IKafkaTeststep result = step;

		try {
			final IKafkaRequest request = step.getRequest();
			IKafkaRequest updatedRequest = null;

			// updated KafkaProduceRequest
			if (request.getType() == KafkaProduceRequest.class) {
				updatedRequest = updateFields(KafkaProduceRequest.class, request, values);
			}

			// updated KafkaProduceRequest
			else if (request.getType() == KafkaConsumeRequest.class) {
				updatedRequest = updateFields(KafkaConsumeRequest.class, request, values);
			}

			else {
				throw new SystemException(
						String.format("KafkaRequest Type '%s' is not implemented yet.", request.getType()));
			}

			/*
			 * result.setRequest(updatedRequest); if
			 * (!Convert.isEmpty(Arrays.asList(step.getAssertions()))) { final Assertion[]
			 * assertions = step.getAssertions(); final Assertion[] updatedAssertions = new
			 * Assertion[assertions.length]; for (int itr = 0; itr < assertions.length; itr
			 * += 1) { final Assertion assertion = updateFields(Assertion.class,
			 * assertions[itr], values); updatedAssertions[itr] = assertion; }
			 * result.setAssertions(updatedAssertions); }
			 */
		} catch (final Exception ex) {
			final String message = "can't update api test step.";
			KafkaTestCase.logger.error(message, ex);
			throw new SystemException(message);
		}

		return result;
	}

	/**
	 * @brief method, that checks, if a {@param HttpRequest} request, returns the
	 *        expected {@param HttpResponse} response.
	 */
	@SuppressWarnings("rawtypes")
	protected KafkaTestResult<KafkaProduceRequest, KafkaProduceResponse> checkKafkaProduceRequest(
			final KafkaProduceRequest request, final List<Assertion> assertions) {

		if (request == null) {
			throw new IllegalArgumentException("request can't be null.");
		}

		if (!request.validate()) {
			throw new IllegalArgumentException(String.format("request \"%s\" is invalid.", request));
		}

		KafkaTestResult result = null;

		KafkaProduceRequest kafkaProduceRequest = (KafkaProduceRequest) request;

		KafkaProduceResponse response = null;

		try {
			response = KafkaClient.produceMessage(kafkaProduceRequest);
			if (KafkaTestCase.logger.isDebugEnabled()) {
				KafkaTestCase.logger.debug(String.format("get response: %s.", response));
			}

			result = new KafkaTestResult(kafkaProduceRequest, response);

			// implement me???
			/*
			 * if (!Convert.isEmpty(assertions)) { final Map<String, String> values =
			 * validateAssertions(response, UtilsCollection.toList(assertions));
			 * result.setExtractedValues(values); }
			 */

			result.setSuccessfull(true);
		} catch (final Throwable th) {
			final String msg = String.format("Request '%s' failed.", request);
			KafkaTestCase.logger.error(msg, th);
			result.setErrormessage(th.getMessage());
			result.setSuccessfull(false);

		}

		return result;
	}

	/**
	 * @brief method, that checks, if a {@param HttpRequest} request, returns the
	 *        expected {@param HttpResponse} response.
	 */

	protected KafkaTestResult<KafkaConsumeRequest, KafkaConsumeResponse> checkKafkaConsumeRequest(
			final KafkaConsumeRequest request, final List<Assertion> assertions) {

		if (request == null) {
			throw new IllegalArgumentException("request can't be null.");
		}

		if (!request.validate()) {
			throw new IllegalArgumentException(String.format("request \"%s\" is invalid.", request));
		}

		KafkaTestResult result = null;

		KafkaConsumeResponse response = null;

		try {
			response = KafkaClient.consumeMessage(request);
			if (KafkaTestCase.logger.isDebugEnabled()) {
				KafkaTestCase.logger.debug(String.format("get response: %s.", response));
			}

			result = new KafkaTestResult(request, response);

			if (!Convert.isEmpty(assertions)) {
				final Map<String, String> values = validateAssertions(response, UtilsCollection.toList(assertions));
				result.setExtractedValues(values);
			}

			result.setSuccessfull(true);
		} catch (final Throwable th) {
			final String msg = String.format("Request '%s' failed.", request);
			KafkaTestCase.logger.error(msg, th);
			result.setErrormessage(th.getMessage());
			result.setSuccessfull(false);
		}

		return result;
	}

	@Override
	protected final void getNextTeststep() throws SystemException {
		TeststepProvider<IKafkaTeststep> provider = getProvider();

		if (provider == null) {
			throw new IllegalArgumentException("provider can't be null.");
		}

		IKafkaTeststep kafkaTeststep = provider.get();
		if (kafkaTeststep == null) {
			return;
		}
		if (!kafkaTeststep.validate()) {
			throw new IllegalArgumentException(String.format("Step '%s' is invalid.", kafkaTeststep));
		}
		if (getExtractedValues() == null) {
			throw new IllegalStateException("extractedValues can't be null.");
		}
		if (KafkaTestCase.logger.isDebugEnabled()) {
			KafkaTestCase.logger.debug(String.format("next apiteststep '%s'.", kafkaTeststep));
		}
		if (!getExtractedValues().keySet().isEmpty()) {
			kafkaTeststep = updateTeststep(kafkaTeststep, getExtractedValues());
		}

		currentstepname = kafkaTeststep.getName();
		kafkaRequest = kafkaTeststep.getRequest();

		if (kafkaTeststep instanceof de.simpleworks.staf.module.kafka.consume.KafkaTeststep) {

			de.simpleworks.staf.module.kafka.consume.KafkaTeststep consumeTeststep = (de.simpleworks.staf.module.kafka.consume.KafkaTeststep) kafkaTeststep;
			currentAssertions = consumeTeststep.getAssertions();
		}

	}

	@Override
	public void bootstrap() throws Exception {
		if (KafkaTestCase.logger.isDebugEnabled()) {
			KafkaTestCase.logger
					.debug(String.format("bootstrap '%s'-TestCase (%s).", KafkaTestCase.class, getTestCaseName()));
		}
	}

	@Override
	public void shutdown() throws Exception {
		if (KafkaTestCase.logger.isDebugEnabled()) {
			KafkaTestCase.logger
					.debug(String.format("shutdown '%s'-TestCase (%s).", KafkaTestCase.class, getTestCaseName()));
		}
	}

	@Override
	public final void executeTestStep() throws Exception {
		// add error handling
		getNextTeststep();

		final IKafkaRequest<?> request = kafkaRequest;
		final Assertion[] assertions = getCurrentAssertions();

		if (request.getType() == KafkaProduceRequest.class) {

			KafkaProduceRequest produceRequest = (KafkaProduceRequest) request;
			currentResult = checkKafkaProduceRequest(produceRequest, UtilsCollection.toList(assertions));
		} else if (request.getType() == KafkaConsumeRequest.class) {

			KafkaConsumeRequest consumeRequest = (KafkaConsumeRequest) request;
			currentResult = checkKafkaConsumeRequest(consumeRequest, UtilsCollection.toList(assertions));
		} else {
			currentResult = null;
			throw new SystemException(
					String.format("KafkaRequest Type '%s' is not implemented yet.", request.getType()));
		}

		AssertionUtils.assertTrue(currentResult.getErrormessage(), currentResult.isSuccessfull());
		addExtractedValues(currentstepname, currentResult.getExtractedValues());
	}

	@Override
	public BrowserMobProxyServer getProxy() {
		KafkaTestCase.logger
				.error(String.format("'%s' is not supported for '%s'.", "BrowserMobProxyServer", KafkaTestCase.class));
		return null;
	}

	@Override
	public List<RewriteUrlObject> getRewriteUrls() {
		KafkaTestCase.logger
				.error(String.format("'%s' is not supported for '%s'.", "Rewriting Urls", KafkaTestCase.class));
		return null;
	}

	@Override
	public CsvFile createArtefact() {

		if (currentResult == null) {
			return new CsvFile(UtilsCollection.toArray(String.class, Arrays.asList()));
		}

		CsvFile result = null;

		IKafkaResponse<?> response = currentResult.getResponse();

		if (response != null) {

			Map<String, String> row = new HashMap<String, String>();
			// impelement me, put field extraction in a seprarate method

			IKafkaRequest request = currentResult.getRequest();

			if (request.getType() == KafkaProduceRequest.class) {

				KafkaProduceRequest kafkaProduceRequest = null;

				if (request instanceof KafkaProduceRequest) {
					kafkaProduceRequest = (KafkaProduceRequest) request;
				}

				KafkaProduceResponse kafkaProduceResponse = null;

				if (response instanceof KafkaProduceResponse) {
					kafkaProduceResponse = (KafkaProduceResponse) response;
				}

				if ((kafkaProduceRequest != null) && (kafkaProduceResponse != null)) {
					// request
					row.put("topic", kafkaProduceResponse.getTopic());

					if (kafkaProduceRequest.getKey() != null) {
						KafkaProduceRequestKey key = kafkaProduceRequest.getKey();
						row.put("key", key.getValue());
					}

					if (kafkaProduceRequest.getContent() != null) {
						KafkaProduceRequestContent content = kafkaProduceRequest.getContent();
						row.put("content", content.getContent());
					}

					if (!Convert.isEmpty(kafkaProduceRequest.getHeaders())) {
						KafkaProduceRequestHeader[] headers = kafkaProduceRequest.getHeaders();

						for (int itr = 0; itr < headers.length; itr += 1) {
							row.put(String.format("header_%s", Integer.toString(itr)), String
									.format("[Key : %s, Value : %s]", headers[itr].getKey(), headers[itr].getValue()));
						}
					}

					// response
					row.put("partition", Integer.toString(kafkaProduceResponse.getPartition()));
					row.put("timestamp", Long.toString(kafkaProduceResponse.getTimestamp()));
					row.put("offset", Long.toString(kafkaProduceResponse.getOffset()));
				}

			} else if (request.getType() == KafkaConsumeRequest.class) {

				KafkaConsumeRequest kafkaConsumeRequest = null;

				if (request instanceof KafkaConsumeRequest) {
					kafkaConsumeRequest = (KafkaConsumeRequest) request;
				}

				KafkaConsumeResponse kafkaConsumeResponse = null;

				if (response instanceof KafkaConsumeResponse) {
					kafkaConsumeResponse = (KafkaConsumeResponse) response;
				}

				if ((kafkaConsumeRequest != null) && (kafkaConsumeResponse != null)) {
					// request

					for (KafkaConsumeRecord record : Arrays.asList(kafkaConsumeResponse.getRecords())) {
						row.put("topic", record.getTopic());
						row.put("partition", Long.toString(record.getOffset()));
						row.put("offset", Long.toString(record.getOffset()));
						row.put("timestamp", Long.toString(record.getTimestamp()));
						row.put("content", (String) record.getContent());

						if (!Convert.isEmpty(record.getHeaders())) {
							KafkaProduceRequestHeader[] headers = record.getHeaders();

							for (int itr = 0; itr < headers.length; itr += 1) {
								row.put(String.format("header_%s", Integer.toString(itr)), String.format(
										"[Key : %s, Value : %s]", headers[itr].getKey(), headers[itr].getValue()));
							}
						}
					}

				}

			}

			if (result == null) {
				result = new CsvFile(UtilsCollection.toArray(String.class, row.keySet()));
			}

			if (!result.addRow(row)) {
				KafkaTestCase.logger.error("can't set up artefact, will return null.");
			}

		}

		// return at least an "empty csv file"
		if (result == null) {
			result = new CsvFile(UtilsCollection.toArray(String.class, Arrays.asList()));
		}

		return result;
	}

	protected Assertion[] getCurrentAssertions() {
		return currentAssertions;
	}

}
