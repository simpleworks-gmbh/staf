package de.simpleworks.staf.framework.util;

import de.simpleworks.staf.commons.api.HttpResponse;
import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.utils.Convert;

public class HttpResponseUtils {

	public static void compare(final HttpResponse current, final HttpResponse compareTo) throws Exception {

		if (current == null) {
			throw new IllegalArgumentException("current can't be null.");
		}

		if (compareTo == null) {
			throw new IllegalArgumentException("compareTo can't be null.");
		}

		if (current.getStatus() != compareTo.getStatus()) {
			throw new SystemException(String.format("The status '%s', does not match the expected one '%s'.",
					Integer.toString(current.getStatus()), Integer.toString(compareTo.getStatus()) ));
		}

		if (!current.getContentType().equals(compareTo.getContentType())) {
			throw new SystemException(String.format("The content type '%s', does not match the expected one '%s'.",
					current.getContentType().getValue(), compareTo.getContentType().getValue() ) );
		}

		if (!current.getBody().equals(compareTo.getBody()) && !(Convert.isEmpty(compareTo.getBody()))) {
			throw new SystemException(String.format("The body '%s', does not match the expected one '%s'.",
					current.getBody(), compareTo.getBody()));
		}

		if (!current.getBase64Body().equals(compareTo.getBase64Body())
				&& !(Convert.isEmpty(compareTo.getBase64Body()))) {
			throw new SystemException(String.format("The base64Body '%s', does not match the expected one '%s'.",
					current.getBase64Body(), compareTo.getBase64Body()));
		}

		if (!current.getJsonBody().equals(compareTo.getJsonBody()) && !(Convert.isEmpty(compareTo.getJsonBody()))) {
			throw new SystemException(String.format("The jsonBody '%s', does not match the expected one '%s'.",
					current.getJsonBody(), compareTo.getJsonBody()));
		}
	}

}
