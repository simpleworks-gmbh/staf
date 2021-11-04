package de.simpleworks.staf.framework.api.httpclient;

import de.simpleworks.staf.commons.api.HttpRequest;
import de.simpleworks.staf.commons.api.HttpResponse;
import de.simpleworks.staf.commons.exceptions.SystemException;

public interface IHttpClient {
	HttpResponse doRequest(HttpRequest request) throws SystemException;
}
