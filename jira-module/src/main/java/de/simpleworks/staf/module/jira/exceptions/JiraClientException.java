package de.simpleworks.staf.module.jira.exceptions;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.httpclient.api.Response;

public class JiraClientException extends RestClientException{

	private static final long serialVersionUID = 1180260501910272677L;
	final Response response;
	
	public JiraClientException(Response response, Throwable cause, int statusCode) {
		super(cause, statusCode);
		this.response = response;
	}

	public Response getResponse() {
		return response;
	}

}
