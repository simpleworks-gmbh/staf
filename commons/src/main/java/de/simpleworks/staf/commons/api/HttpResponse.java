package de.simpleworks.staf.commons.api;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.enums.ContentTypeEnum;
import de.simpleworks.staf.commons.interfaces.IPojo;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public class HttpResponse implements IPojo {
	private static final Logger logger = LogManager.getLogger(HttpResponse.class);

	private int status;
	private Header[] headers;
	private String body;

	private ContentTypeEnum contentType;

	private String jsonBody;
	private String base64Body;
	private String bodyFileName;

	private ResponseEntity entity;

	private long duration;

	public HttpResponse() {
		this.headers = new Header[0];
		this.body = Convert.EMPTY_STRING;
		this.contentType = ContentTypeEnum.UNKNOWN;
		this.jsonBody = Convert.EMPTY_STRING;
		this.base64Body = Convert.EMPTY_STRING;
		this.bodyFileName = Convert.EMPTY_STRING;

		this.entity = null;
	}

	public int getStatus() {
		return status;
	}

	public Header[] getHeaders() {
		return headers;
	}

	public String getBody() {
		return body;
	}

	public ContentTypeEnum getContentType() {
		return contentType;
	}

	public String getJsonBody() {
		return jsonBody;
	}

	public String getBase64Body() {
		return base64Body;
	}

	public String getBodyFileName() {
		return bodyFileName;
	}

	public ResponseEntity getEntity() {
		return entity;
	}

	public long getDuration() {
		return duration;
	}

	public void setStatus(final int status) {
		this.status = status;
	}

	public void setHeaders(final Header[] headers) {
		this.headers = headers;
	}

	public void setBody(final String body) {
		this.body = body;
	}

	public void setContentType(final ContentTypeEnum contentType) {
		this.contentType = contentType;
	}

	public void setJsonBody(final String jsonBody) {
		this.jsonBody = jsonBody;
	}

	public void setBase64Body(final String base64Body) {
		this.base64Body = base64Body;
	}

	public void setBodyFileName(final String bodyFileName) {
		this.bodyFileName = bodyFileName;
	}

	public void setEntity(ResponseEntity entity) {
		this.entity = entity;
	}

	public void setDuration(final long duration) {
		this.duration = duration;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((base64Body == null) ? 0 : base64Body.hashCode());
		result = (prime * result) + ((body == null) ? 0 : body.hashCode());
		result = (prime * result) + ((bodyFileName == null) ? 0 : bodyFileName.hashCode());
		result = (prime * result) + ((entity == null) ? 0 : entity.hashCode());
		result = (prime * result) + ((contentType == null) ? 0 : contentType.hashCode());
		result = (prime * result) + (int) (duration ^ (duration >>> 32));
		result = (prime * result) + Arrays.hashCode(headers);
		result = (prime * result) + ((jsonBody == null) ? 0 : jsonBody.hashCode());
		result = (prime * result) + status;
		return result;
	}

	@Override
	public boolean validate() {
		if (HttpResponse.logger.isDebugEnabled()) {
			HttpResponse.logger.debug(String.format("validate: '%s'.", toString()));
		}

		boolean result = true;

		if (status < 0) {
			HttpResponse.logger.error(String.format("status can't be negative, but was %d.", Integer.valueOf(status)));
			result = false;
		}

		if (!Convert.isEmpty(headers)) {
			if (Arrays.asList(headers).stream().filter(h -> h.validate()).collect(Collectors.toList()).isEmpty()) {
				HttpResponse.logger.error(String.format("headers are invalid '%s'.", String.join(",",
						Arrays.asList(headers).stream().map(h -> h.toString()).collect(Collectors.toList()))));
				result = false;
			}
		}

		if (contentType == null) {
			HttpResponse.logger.error("contentType can't be null.");
			result = false;
		}

		if (entity != null) {
			if (!(entity.validate())) {
				HttpResponse.logger.error(String.format("entity '%s' is invalid.", entity));
				result = false;
			}
		}

		return result;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s, %s, %s, %s, %s, %s, %s, %s]",
				Convert.getClassName(HttpResponse.class.getName()), UtilsFormat.format("status", status),
				UtilsFormat.format("headers",
						String.join(",",
								Arrays.asList(headers).stream().map(h -> h.toString()).collect(Collectors.toList()))),
				UtilsFormat.format("body", body), UtilsFormat.format("contentType", contentType),
				UtilsFormat.format("jsonBody", jsonBody), UtilsFormat.format("base64Body", base64Body),
				UtilsFormat.format("bodyFileName", bodyFileName), UtilsFormat.format("entity", entity),
				UtilsFormat.format("duration", Long.valueOf(duration)));
	}
}
