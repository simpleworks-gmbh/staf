package de.simpleworks.staf.commons.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.net.UrlEscapers;

import de.simpleworks.staf.commons.enums.ContentTypeEnum;
import de.simpleworks.staf.commons.enums.HttpMethodEnum;
import de.simpleworks.staf.commons.interfaces.IPojo;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public class HttpRequest implements IPojo {
	private static final Logger logger = LogManager.getLogger(APITeststep.class);
	private HttpMethodEnum method;
	private String scheme;
	private ContentTypeEnum contentType;
	private int port;
	private String host;
	private String urlPath;
	private Cookie[] cookies;
	private String body;
	private String bodyFileName;
	private QueryParameter[] queryParameters;
	private FormParameter[] formParameters;
	private MultipartFormDataParameter[] multipartFormDataParameters;
	private MultipartFormFileParameter multipartFormFileParameter;
	private Header[] headers;
	private String basicAuth;

	public HttpRequest() {
		method = HttpMethodEnum.GET;
		scheme = Convert.EMPTY_STRING;
		contentType = ContentTypeEnum.UNKNOWN;
		host = Convert.EMPTY_STRING;
		urlPath = Convert.EMPTY_STRING;
		cookies = new Cookie[0];
		body = Convert.EMPTY_STRING;
		bodyFileName = Convert.EMPTY_STRING;
		queryParameters = new QueryParameter[0];
		formParameters = new FormParameter[0];
		multipartFormDataParameters = new MultipartFormDataParameter[0];
		multipartFormFileParameter = new MultipartFormFileParameter();
		headers = new Header[0];
		basicAuth = Convert.EMPTY_STRING;
	}

	public HttpMethodEnum getMethod() {
		return method;
	}

	public String getScheme() {
		return scheme;
	}

	public ContentTypeEnum getContentType() {
		return contentType;
	}

	public void setContentType(final ContentTypeEnum contentType) {
		this.contentType = contentType;
	}

	public int getPort() {
		return port;
	}

	public String getHost() {
		return host;
	}

	public String getUrlPath() {
		return urlPath;
	}

	public Cookie[] getCookies() {
		return cookies;
	}

	public String getBody() {
		return body;
	}

	public String getBodyFileName() {
		return bodyFileName;
	}

	public void setBody(final String body) {
		this.body = body;
	}

	public void setBodyFileName(final String bodyFileName) {
		this.bodyFileName = bodyFileName;
	}

	public QueryParameter[] getQueryParameters() {
		return queryParameters;
	}

	public FormParameter[] getFormParameters() {
		return formParameters;
	}

	public MultipartFormDataParameter[] getMultipartFormDataParameters() {
		return multipartFormDataParameters;
	}

	public MultipartFormFileParameter getMultipartFormFileParameter() {
		return multipartFormFileParameter;
	}

	public Header[] getHeaders() {
		return headers;
	}

	public String getBasicAuth() {
		return basicAuth;
	}

	public void setMethod(final HttpMethodEnum method) {
		this.method = method;
	}

	public void setScheme(final String scheme) {
		this.scheme = scheme;
	}

	public void setPort(final int port) {
		this.port = port;
	}

	public void setHost(final String host) {
		this.host = host;
	}

	public void setUrlPath(final String urlPath) {
		this.urlPath = urlPath;
	}

	public void setCookies(final Cookie[] cookies) {
		this.cookies = cookies;
	}

	public void setMultipartFormFileParameter(final MultipartFormFileParameter multipartFormFileParameter) {
		this.multipartFormFileParameter = multipartFormFileParameter;
	}

	public void setQueryParameters(final QueryParameter[] queryParameters) {
		this.queryParameters = queryParameters;
	}

	public void setFormParameters(FormParameter[] formParameters) {
		this.formParameters = formParameters;
	}

	public void setHeaders(final Header[] headers) {
		this.headers = headers;
	}

	public void setMultipartFormDataParameters(final MultipartFormDataParameter[] multipartFormDataParameters) {
		this.multipartFormDataParameters = multipartFormDataParameters;
	}

	public void setBasicAuth(final String basicAuth) {
		this.basicAuth = basicAuth;
	}

	public String getUrl() {
		String result = Convert.EMPTY_STRING;
		if (port > 0) {
			result = String.format("%s://%s:%d/%s", scheme, host, Integer.valueOf(port), urlPath);
		} else {
			result = String.format("%s://%s/%s", scheme, host, urlPath);
		}
		if (HttpRequest.logger.isDebugEnabled()) {
			HttpRequest.logger.debug(String.format("result: '%s'.", result));
		}

		if (queryParameters.length > 0) {
			result = substituteQueryParameter(result);
		}
		return result;
	}

	private String substituteQueryParameter(final String url) {
		if (Convert.isEmpty(url)) {
			throw new IllegalArgumentException("url can't be null or empty string.");
		}
		if (Convert.isEmpty(Arrays.asList(queryParameters))) {
			return url;
		}
		final List<String> substitutedQueryParams = new ArrayList<>();
		try {
			for (final QueryParameter q : Arrays.asList(queryParameters)) {

				if (ContentTypeEnum.FORM_URLENCODED.equals(contentType)) {
					substitutedQueryParams.add(
							String.format("%s=%s", URLEncoder.encode(q.getName(), StandardCharsets.UTF_8.toString()),
									URLEncoder.encode(q.getValue(), StandardCharsets.UTF_8.toString())));
				} else {

					substitutedQueryParams.add(String
							.format("%s=%s", UrlEscapers.urlFragmentEscaper().escape(q.getName()),
									UrlEscapers.urlFragmentEscaper().escape(q.getValue()))
							// workaround to convert to the ascii variant
							.replace(  "%2B", "%20"));
				}

			}
		} catch (final UnsupportedEncodingException ex) {
			HttpRequest.logger.error("can't determine encoding for query parameter.", ex);
			substitutedQueryParams.clear();
		}
		return String.format("%s?%s", url, substitutedQueryParams.stream().collect(Collectors.joining("&")));
	}

	@Override
	public boolean validate() {
		if (HttpRequest.logger.isDebugEnabled()) {
			HttpRequest.logger.debug("validate HttpRequest...");
		}
		boolean result = true;
		if (method == null) {
			HttpRequest.logger.error("method can't be null.");
			result = false;
		}
		if (Convert.isEmpty(scheme)) {
			HttpRequest.logger.error("scheme can't be null or empty string.");
			result = false;
		}
		if (contentType == null) {
			HttpRequest.logger.error("contentType can't be null.");
			result = false;
		}
		if (port < 0) {
			HttpRequest.logger.error(String.format("port can't be negative, but was \"%s\".", Integer.toString(port)));
			result = false;
		} else if (port > 65535) {
			HttpRequest.logger
					.error(String.format("port can't be greater than 65535, but was \"%s\".", Integer.toString(port)));
			result = false;
		}
		if (Convert.isEmpty(host)) {
			HttpRequest.logger.error("host can't be null or empty string.");
			result = false;
		}
		if (Convert.isEmpty(urlPath)) {
			HttpRequest.logger.error("urlPath can't be null or empty string.");
			result = false;
		}
		if (!Convert.isEmpty(cookies)) {
			if (Arrays.asList(cookies).stream().filter(c -> c.validate()).collect(Collectors.toList()).isEmpty()) {
				HttpRequest.logger.error(String.format("cookies are invalid '%s'.", String.join(",",
						Arrays.asList(cookies).stream().map(c -> c.toString()).collect(Collectors.toList()))));
				result = false;
			}
		}
		if (!Convert.isEmpty(queryParameters)) {
			if (Arrays.asList(queryParameters).stream().filter(h -> h.validate()).collect(Collectors.toList())
					.isEmpty()) {
				HttpRequest.logger.error(String.format("queryParameters are invalid '%s'.", String.join(",",
						Arrays.asList(queryParameters).stream().map(q -> q.toString()).collect(Collectors.toList()))));
				result = false;
			}
		}
		if (!Convert.isEmpty(formParameters)) {
			if (Arrays.asList(formParameters).stream().filter(h -> h.validate()).collect(Collectors.toList())
					.isEmpty()) {
				HttpRequest.logger.error(String.format("formParameters are invalid '%s'.", String.join(",",
						Arrays.asList(formParameters).stream().map(q -> q.toString()).collect(Collectors.toList()))));
				result = false;
			}
		}
		if (!Convert.isEmpty(multipartFormDataParameters)) {
			if (Arrays.asList(multipartFormDataParameters).stream().filter(h -> h.validate())
					.collect(Collectors.toList()).isEmpty()) {
				HttpRequest.logger.error(String.format("multipartBody are invalid '%s'.",
						String.join(",", Arrays.asList(multipartFormDataParameters).stream().map(q -> q.toString())
								.collect(Collectors.toList()))));
				result = false;
			}
		}
		if (!Convert.isEmpty(headers)) {
			if (Arrays.asList(headers).stream().filter(h -> h.validate()).collect(Collectors.toList()).isEmpty()) {
				HttpRequest.logger.error(String.format("headers are invalid '%s'.", String.join(",",
						Arrays.asList(headers).stream().map(h -> h.toString()).collect(Collectors.toList()))));
				result = false;
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s]",
				Convert.getClassName(HttpRequest.class),
				UtilsFormat.format("method", method == null ? null : method.getValue()),
				UtilsFormat.format("scheme", scheme),
				UtilsFormat.format("contentType", contentType == null ? null : contentType.getValue()),
				UtilsFormat.format("port", port), UtilsFormat.format("host", host),
				UtilsFormat.format("urlPath", urlPath),
				UtilsFormat.format("cookies",
						String.join(",",
								Arrays.asList(cookies).stream().map(c -> c.toString()).collect(Collectors.toList()))),
				UtilsFormat.format("body", body), UtilsFormat.format("bodyFileName", bodyFileName),
				UtilsFormat.format("queryParameters",
						String.join(",",
								Arrays.asList(queryParameters).stream().map(q -> q.toString())
										.collect(Collectors.toList()))),
				UtilsFormat.format("formParameters",
						String.join(",",
								Arrays.asList(formParameters).stream().map(q -> q.toString())
										.collect(Collectors.toList()))),
				UtilsFormat.format("multipartFormDataParameters",
						String.join(",",
								Arrays.asList(multipartFormDataParameters).stream().map(m -> m.toString())
										.collect(Collectors.toList()))),
				UtilsFormat.format("multipartFormFileParameter", multipartFormFileParameter),
				UtilsFormat.format("headers",
						String.join(",",
								Arrays.asList(headers).stream().map(h -> h.toString()).collect(Collectors.toList()))),
				UtilsFormat.format("basicAuth", basicAuth));
	}
}