package de.simpleworks.staf.commons.api;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.interfaces.ITeststep;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public class APITeststep implements ITeststep {

	private static final Logger logger = LogManager.getLogger(APITeststep.class);

	private String name;
	private int order;
	private HttpRequest request;
	private HttpResponse response;
	private Assertion[] assertions;

	public APITeststep() {
		request = new HttpRequest();
		response = new HttpResponse();
		assertions = new Assertion[0];
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getOrder() {
		return order;
	}

	public HttpRequest getRequest() {
		return request;
	}

	public HttpResponse getResponse() {
		return response;
	}

	public Assertion[] getAssertions() {
		return assertions;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setOrder(final int order) {
		this.order = order;
	}

	public void setRequest(final HttpRequest request) {
		this.request = request;
	}

	public void setResponse(final HttpResponse response) {
		this.response = response;
	}

	public void setAssertions(final Assertion[] assertions) {
		this.assertions = assertions;
	}

	@Override
	public boolean validate() {
		if (APITeststep.logger.isDebugEnabled()) {
			APITeststep.logger.debug("validate APITeststep...");
		}

		boolean result = true;

		if (Convert.isEmpty(name)) {
			if (APITeststep.logger.isDebugEnabled()) {
				APITeststep.logger.debug("name can't be null or empty string.");
			}
			result = false;
		}

		if (order < 1) {
			if (APITeststep.logger.isDebugEnabled()) {
				APITeststep.logger
						.debug(String.format("order can't be less than 1, but was \"%s\".", Integer.toString(order)));
			}
			result = false;
		}

		if (request == null) {
			APITeststep.logger.error("request can't be null.");
			result = false;
		} else {
			if (!request.validate()) {
				APITeststep.logger.error(String.format("request \"%s\" is invalid.", request.toString()));
				result = false;
			}
		}

		if (response == null) {
			APITeststep.logger.error("response can't be null.");
			result = false;
		} else {
			if (!response.validate()) {
				APITeststep.logger.error(String.format("response \"%s\" is invalid.", response.toString()));
				result = false;
			}
		}

		if (!Convert.isEmpty(assertions)) {
			List<Assertion> currentAssertions = Arrays.asList(assertions);

			if (currentAssertions.stream().filter(a -> a.validate()).collect(Collectors.toList()).isEmpty()) {
				APITeststep.logger.error(String.format("assertions are invalid [%s].", String.join(",",
						Arrays.asList(assertions).stream().map(a -> a.toString()).collect(Collectors.toList()))));
				result = false;
			}
			
			for (Assertion assertion : currentAssertions) {
				if (currentAssertions.indexOf(assertion) != currentAssertions.lastIndexOf(assertion)) {
					APITeststep.logger.error(String
							.format("assertion \"%s\" is used at last two times, which is not supported.", assertion));
					result = false;
					break;
				}
			}

		}

		return result;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s, %s, %s, %s]", Convert.getClassName(APITeststep.class),
				UtilsFormat.format("name", name), UtilsFormat.format("order", order),
				UtilsFormat.format("request", request), UtilsFormat.format("response", response),
				UtilsFormat.format("assertions", String.join(",",
						Arrays.asList(assertions).stream().map(a -> a.toString()).collect(Collectors.toList()))));
	}
}
