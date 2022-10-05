package de.simpleworks.staf.framework.api.httpclient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.simpleworks.staf.commons.api.FormParameter;
import de.simpleworks.staf.commons.api.Header;
import de.simpleworks.staf.commons.api.HttpRequest;
import de.simpleworks.staf.commons.api.HttpResponse;
import de.simpleworks.staf.commons.api.MultipartFormFileParameter;
import de.simpleworks.staf.commons.api.RawFileParameter;
import de.simpleworks.staf.commons.enums.ContentTypeEnum;
import de.simpleworks.staf.commons.enums.HttpMethodEnum;
import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.commons.utils.UtilsEnum;
import de.simpleworks.staf.commons.utils.UtilsFormat;
import de.simpleworks.staf.commons.utils.UtilsIO;
import de.simpleworks.staf.framework.util.OkHttpBuilder;
import de.simpleworks.staf.framework.util.OkHttpClientRecipe;
import net.lightbody.bmp.BrowserMobProxyServer;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.FormBody.Builder;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HttpClient implements IHttpClient {
	private static final Logger logger = LogManager.getLogger(HttpClient.class);
	private final OkHttpClientRecipe okhttpclientRecipe;
	private final OkHttpClient client;
	private final BrowserMobProxyServer browsermobProxy;

	public HttpClient() throws SystemException {
		okhttpclientRecipe = OkHttpBuilder.buildOkHttpClientRecipe();
		client = okhttpclientRecipe.getClient();
		browsermobProxy = okhttpclientRecipe.getBrowsermobProxy();
	}

	public BrowserMobProxyServer getBrowserMobProxyServer() {
		return browsermobProxy;
	}

	/**
	 * @brief method that executes a {@code HttpRequest}
	 * @param request {@code HttpRequest}
	 * @return HttpResponse {@code HttpResponse}, null if an inner execption was
	 *         thrown.
	 * @throws SystemException
	 */
	@Override
	public HttpResponse doRequest(final HttpRequest request) throws SystemException {
		if (request == null) {
			throw new IllegalArgumentException("request can't be null.");
		}
		if (HttpClient.logger.isDebugEnabled()) {
			HttpClient.logger.debug(String.format("request: ", request));
		}
		final Headers headers = HttpClient.buildHeaders(request);
		final String url = request.getUrl();
		final HttpMethodEnum method = request.getMethod();
		final RequestBody requestBody = HttpClient.buildRequestBody(request);
		Request req = null;
		try {
			req = HttpClient.buildRequest(headers, url, method, requestBody);
		} catch (final Exception ex) {
			final String message = "can't set up Request.";
			HttpClient.logger.error(message, ex);
			throw new SystemException(message);
		}
		HttpResponse result = new HttpResponse();
		try {
			final Call call = client.newCall(req);
			final long startTime = System.nanoTime();
			try (Response response = call.execute();) {
				final long stopTime = System.nanoTime();
				List<Header> responseHeaders = HttpClient.buildResponseHeader(response.headers());
				try (final ResponseBody responseBody = response.body();) {
					if ((204 != response.code())) {
						result = HttpClient.buildResponseBody(responseBody);
					}
				}
				result.setStatus(response.code());
				result.setDuration(stopTime - startTime);
				result.setHeaders(UtilsCollection.toArray(Header.class, responseHeaders));
			}
		} catch (final Exception ex) {
			final String message = "can't set up Response.";
			HttpClient.logger.error(message, ex);
			throw new SystemException(message);
		}
		return result;
	}

	/**
	 * @brief Factory Methods Requests
	 */
	private static Request buildRequest(final Headers headers, final String url, final HttpMethodEnum method,
			final RequestBody requestBody) {
		if (headers == null) {
			throw new IllegalArgumentException("headers can't be null.");
		}
		if (Convert.isEmpty(url)) {
			throw new IllegalArgumentException("url can't be null or empty string.");
		}
		if (method == null) {
			throw new IllegalArgumentException("method can't be null.");
		}
		final Request result = new Request.Builder().headers(headers).url(url).method(method.getValue(), requestBody)
				.build();
		if (HttpClient.logger.isDebugEnabled()) {
			HttpClient.logger.debug(String.format("Request: '%s'.", result));
		}
		return result;
	}

	private static Headers buildHeaders(final HttpRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("request can't be null.");
		}
		if (request.getHeaders() == null) {
			return Headers.of();
		}
		final Map<String, String> headerMap = new HashedMap<>();
		final Header[] expectedHeaders = request.getHeaders();
		for (final Header header : expectedHeaders) {
			if (headerMap.containsKey(header.getName())) {
				if (HttpClient.logger.isDebugEnabled()) {
					HttpClient.logger.debug(String.format("Entry '%s' is already inserted.", header.getName()));
				}
				continue;
			}
			headerMap.put(header.getName(), header.getValue());
		}
		final Headers result = Headers.of(headerMap);
		if (HttpClient.logger.isDebugEnabled()) {
			HttpClient.logger.debug(String.format("Headers: '%s'.", result));
		}
		return result;
	}

	/**
	 * @brief method that builds the request body for an HttpRequest
	 * @param {@code HttpRequest} request
	 * @retrun {@code RequestBody} request body, of the respecting request, null if
	 *         method does not support request bodies
	 */
	private static RequestBody buildRequestBody(final HttpRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("request can't be null.");
		}
		if (!request.getMethod().hasRequestBody()) {
			return null;
		}
		final RequestBody result;
		final ContentTypeEnum contenttype = request.getContentType();
		switch (contenttype) {
		case JSON:
			result = RequestBody.create(MediaType.parse(request.getContentType().getValue()), request.getBody());
			break;
		case FORM_URLENCODED:
			final Builder formBodyBuilder = new FormBody.Builder();
			for (FormParameter parameter : UtilsCollection.toList(request.getFormParameters())) {
				formBodyBuilder.add(parameter.getName(), parameter.getValue());
			}
			result = formBodyBuilder.build();
			break;
		case MULTIPART_FORM_DATA:
			final okhttp3.MultipartBody.Builder multipartBodyBuilder = new okhttp3.MultipartBody.Builder()
					.setType(MultipartBody.FORM);
			for (final de.simpleworks.staf.commons.api.MultipartFormDataParameter multiPart : request
					.getMultipartFormDataParameters()) {
				final String name = multiPart.getName();
				final String value = multiPart.getValue();
				multipartBodyBuilder.addFormDataPart(name, value);
			}
			final MultipartFormFileParameter multipartFormFileParameter = request.getMultipartFormFileParameter();
			if (multipartFormFileParameter != null) {
				multipartBodyBuilder.addFormDataPart("file", multipartFormFileParameter.getName(),
						RequestBody.create(MediaType.parse(multipartFormFileParameter.getMimeType()),
								new File(multipartFormFileParameter.getFile())));
			}
			result = multipartBodyBuilder.build();
			break;
		default:

			final RawFileParameter rawFileParameter = request.getRawFileParameter();

			result = RequestBody.create(MediaType.parse(contenttype.getValue()), new File(rawFileParameter.getFile()));

		}
		return result;
	}

	/**
	 * @brief Factory Methods Responses
	 */
	private static byte[] readBody(final ResponseBody body) throws Exception {
		final byte[] bytes;
		try (InputStream stream = body.byteStream();) {
			bytes = IOUtils.toByteArray(stream);
		}
		return bytes;
	}

	private static ContentTypeEnum getContentType(final ResponseBody body) {
		final MediaType contentType = body.contentType();
		final String ct = String.format("%s/%s", contentType.type(), contentType.subtype());
		final ContentTypeEnum result = UtilsEnum.getEnumByValue(ContentTypeEnum.JSON, ct);
		if (result == null) {
			throw new IllegalArgumentException(String.format("Content Type '%s' is not implemented yet.", ct));
		}
		return result;
	}

	private static HttpResponse getResponseText(final ResponseBody body) throws Exception {
		final byte[] bytes = HttpClient.readBody(body);
		if ((bytes == null) || (bytes.length == 0)) {
			throw new RuntimeException("Response Body is empty.");
		}
		final HttpResponse result = new HttpResponse();
		result.setContentType(HttpClient.getContentType(body));
		final byte[] encoded = Base64.getEncoder().encode(bytes);
		final String base64 = new String(encoded);
		result.setBase64Body(base64);
		result.setBody(new String(bytes));
		if (HttpClient.logger.isDebugEnabled()) {
			HttpClient.logger.debug(UtilsFormat.format("body", result.getBody()));
		}
		return result;
	}

	private static HttpResponse getResponseApplication(final ResponseBody body) throws Exception {
		final byte[] bytes = HttpClient.readBody(body);
		if ((bytes == null) || (bytes.length == 0)) {
			throw new RuntimeException("Response Body is empty.");
		}
		final HttpResponse result = new HttpResponse();
		final ContentTypeEnum contentTypeEnum = HttpClient.getContentType(body);
		result.setContentType(contentTypeEnum);
		if (contentTypeEnum == ContentTypeEnum.JSON) {
			final String json = UtilsIO.getAllContentFromBytesArray(bytes);
			result.setJsonBody(json);
			if (HttpClient.logger.isDebugEnabled()) {
				HttpClient.logger.debug(UtilsFormat.format("jsonBody", result.getJsonBody()));
			}
		} else {
			final byte[] encodedFile = Base64.getEncoder().encode(bytes);
			final String base64EncodedFile = new String(encodedFile);
			result.setBase64Body(base64EncodedFile);
			if (HttpClient.logger.isDebugEnabled()) {
				HttpClient.logger.debug(UtilsFormat.format("base64Body", result.getBase64Body()));
			}
		}
		return result;
	}

	private static HttpResponse getResponseImage(final ResponseBody body) throws Exception {
		final byte[] bytes = HttpClient.readBody(body);
		if ((bytes == null) || (bytes.length == 0)) {
			throw new RuntimeException("Response Body is empty.");
		}
		final HttpResponse result = new HttpResponse();
		final MediaType contenttype = body.contentType();
		final byte[] encodedImage = Base64.getEncoder().encode(bytes);
		final String pdf = new String(encodedImage);
		result.setBase64Body(pdf);
		final ContentTypeEnum type;
		switch (contenttype.subtype()) {
		case "png":
			type = ContentTypeEnum.PNG;
			break;
		case "jpg":
			type = ContentTypeEnum.JPG;
			break;
		case "jpeg":
			type = ContentTypeEnum.JPEG;
			break;
		default:
			type = ContentTypeEnum.UNKNOWN;
		}
		result.setContentType(type);
		return result;
	}

	/**
	 * @brief method to transform a {@code Headers} into a convinient List of
	 *        {@code Header} headers
	 * @param {@code Headers} headers
	 * @throws IOException
	 */
	private static List<Header> buildResponseHeader(final Headers headers) {
		if (headers == null) {
			throw new IllegalArgumentException("headers can't be null.");
		}
		final List<Header> result = new ArrayList<>();
		for (String name : headers.names()) {
			final String value = headers.get(name);
			if (HttpClient.logger.isDebugEnabled()) {
				HttpClient.logger.debug(String.format("fetch Header %s:%s.", name, value));
			}
			result.add(new Header(name, value));
		}
		return result;
	}

	/**
	 * @brief method to transform a {@code Response} into a convinient
	 *        {@code HttpResponse} response
	 *
	 * @param {@code ResponseBody} body
	 * @return {@code HttpResponse} response, respecting the method argument,
	 *         ReponseBody can be empty!
	 * @throws IOException
	 */
	private static HttpResponse buildResponseBody(final ResponseBody body) throws Exception {
		if (body == null) {
			throw new IllegalArgumentException("body can't be null.");
		}
		final HttpResponse result;
		final MediaType contenttype = body.contentType();
		if (contenttype == null) {
			return new HttpResponse();
		}
		final String type = contenttype.type();
		if (HttpClient.logger.isDebugEnabled()) {
			HttpClient.logger.debug(String.format("body: %s, %s.", UtilsFormat.format("contenttype", contenttype),
					UtilsFormat.format("type", type)));
		}
		switch (type) {
		// available media types
		// https://square.github.io/okhttp/4.x/okhttp/okhttp3/-media-type/type/
		case "text":
			result = HttpClient.getResponseText(body);
			break;
		case "application":
			result = HttpClient.getResponseApplication(body);
			break;
		case "image":
			result = HttpClient.getResponseImage(body);
			break;
		default:
			throw new IllegalArgumentException(String.format("Content Type '%s' is not implemented yet.", type));
		}
		return result;
	}
}