package de.simpleworks.staf.module.kafka.elements.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;
import de.simpleworks.staf.framework.elements.commons.ATestResult;
import de.simpleworks.staf.module.kafka.api.IKafkaRequest;
import de.simpleworks.staf.module.kafka.api.IKafkaResponse;
import de.simpleworks.staf.module.kafka.api.KafkaProduceResponse;

public class KafkaTestResult<Request, Response> extends ATestResult {

	private final IKafkaRequest<Request> request;
	private final IKafkaResponse<Response> response;
	private Map<String, String> extractedValues;

	public KafkaTestResult(IKafkaRequest<Request> request, IKafkaResponse<Response> response) {

		if (request == null) {
			throw new IllegalArgumentException("request can't be null.");
		}

		if (response == null) {
			throw new IllegalArgumentException("response can't be null.");
		}

		this.request = request;
		this.response = response;
	}

	public Map<String, String> getExtractedValues() {
		return extractedValues;
	}

	public void setExtractedValues(Map<String, String> extractedValues) {
		this.extractedValues = extractedValues;
	}

	public IKafkaRequest<Request> getRequest() {
		return request;
	}

	public IKafkaResponse<Response> getResponse() {
		return response;
	}

	@Override
	public String toString() {
		final String vals;
		if (extractedValues != null) {
			final List<String> values = new ArrayList<>();
			extractedValues.keySet().stream().forEach(key -> {
				values.add(String.format("[Key : '%s', Value : '%s']", key, extractedValues.get(key)));
			});
			vals = String.join(",", values);
		} else {
			vals = null;
		}

		return String.format("[%s: %s, %s, %s, %s, %s]", Convert.getClassName(KafkaTestResult.class),
				UtilsFormat.format("response", response), UtilsFormat.format("successfull", isSuccessfull()),
				UtilsFormat.format("errormessage", getErrormessage()), UtilsFormat.format("extractedValues", vals));
	}

}
