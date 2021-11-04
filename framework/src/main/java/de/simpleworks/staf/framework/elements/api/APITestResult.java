package de.simpleworks.staf.framework.elements.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.simpleworks.staf.commons.api.HttpRequest;
import de.simpleworks.staf.commons.api.HttpResponse;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public class APITestResult {
	private final HttpRequest request;
	private final HttpResponse expectedResponse;

	private HttpResponse response;
	private boolean successfull;
	private String errormessage;
	private Map<String, String> extractedValues;

	public APITestResult(final HttpRequest request, final HttpResponse expectedResponse) {
		if (request == null) {
			throw new IllegalArgumentException("request can't be null.");
		}

		if (expectedResponse == null) {
			throw new IllegalArgumentException("expectedResponse can't be null.");
		}

		this.request = request;
		this.expectedResponse = expectedResponse;
	}

	public boolean isSuccessfull() {
		return successfull;
	}

	public HttpRequest getRequest() {
		return request;
	}

	public HttpResponse getResponse() {
		return response;
	}

	public Map<String, String> getExtractedValues() {
		return extractedValues;
	}

	public HttpResponse getExpectedResponse() {
		return expectedResponse;
	}

	public void setSuccessfull(final boolean successfull) {
		this.successfull = successfull;
	}

	public void setResponse(final HttpResponse response) {
		this.response = response;
	}

	public void setExtractedValues(final Map<String, String> extractedValues) {
		this.extractedValues = extractedValues;
	}

	public String getErrormessage() {
		return errormessage;
	}

	public void setErrormessage(final String errormessage) {
		this.errormessage = errormessage;
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

		return String.format("[%s: %s, %s, %s, %s, %s, %s]", Convert.getClassName(APITestResult.class),
				UtilsFormat.format("request", request), UtilsFormat.format("expectedResponse", expectedResponse),
				UtilsFormat.format("response", response), UtilsFormat.format("successfull", successfull),
				UtilsFormat.format("errormessage", errormessage), UtilsFormat.format("extractedValues", vals));
	}
}
