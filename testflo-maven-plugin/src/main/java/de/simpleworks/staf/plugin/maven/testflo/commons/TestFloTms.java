package de.simpleworks.staf.plugin.maven.testflo.commons;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;

import com.atlassian.jira.rest.client.api.domain.Issue;

import de.simpleworks.staf.commons.consts.ContentTypeValue;
import de.simpleworks.staf.commons.enums.ArtefactEnum;
import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.plugin.maven.testflo.commons.enums.HttpMethod;
import de.simpleworks.staf.plugin.maven.testflo.commons.enums.TestStepStatus;
import de.simpleworks.staf.plugin.maven.testflo.utils.TestFLOProperties;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.MultipartBody.Part;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class TestFloTms {

	private static final Logger logger = LogManager.getLogger(TestFloTms.class);

	private static final String JSON_MOVE_TO_NEXT_ITERATION = "{\"nextIterationStrategy\": \"all-test-cases\"}";
	private static final String MARKUP_THUMBNAIL = "!%s|thumbnail!";

	private final OkHttpClient client;
	private final URL urlTms;
	private final boolean skipTimeOut;

	public TestFloTms(final OkHttpClient client, final URL urlTms, TestFLOProperties instance) {
		if (client == null) {
			throw new IllegalArgumentException("client can't be null.");
		}

		if (urlTms == null) {
			throw new IllegalArgumentException("urlTms can't be null.");
		}

		if (instance == null) {
			throw new IllegalArgumentException("instance can't be null.");
		}

		this.client = client;
		this.urlTms = urlTms;
		this.skipTimeOut = instance.isSkipTimeout();
	}

	private HttpUrl.Builder createBuilder() {
		HttpUrl.Builder builder = new HttpUrl.Builder().scheme(urlTms.getProtocol()).host(urlTms.getHost());
		final int port = urlTms.getPort();
		if (0 < port) {
			builder = builder.port(port);
		}

		return builder.addPathSegments(urlTms.getPath());
	}

	private static String getresponseBody(final Response response) {
		String result = null;

		try (ResponseBody body = response.body();) {
			Assert.assertNotNull("body can't be null.", body);

			result = body.string();
			if (TestFloTms.logger.isDebugEnabled()) {
				TestFloTms.logger.debug(String.format("response body: '%s'.", result));
			}
		} catch (final IOException ex) {
			final String msg = String.format("can't get response body response: '%s'.", response);
			TestFloTms.logger.error(msg, ex);
		}

		return result;
	}

	private String tmsSend(final HttpMethod method, final HttpUrl.Builder builder, final RequestBody requestBody)
			throws SystemException {
		Assert.assertNotNull("builder can't be null.", builder);

		final URL url;

		try {
			url = builder.build().uri().normalize().toURL();
		} catch (final MalformedURLException ex) {
			final String message = String.format("can't convert '%s' to valid URL.", builder.toString());
			TestFloTms.logger.error(message, ex);
			throw new SystemException(message);
		}

		if (TestFloTms.logger.isDebugEnabled()) {
			TestFloTms.logger.debug(String.format("use url: '%s'.", url));
		}

		final Request request;
		if (requestBody == null) {
			request = new Request.Builder().url(url).build();
		} else {
			switch (method) {
			case POST:
				request = new Request.Builder().url(url).post(requestBody).build();
				break;
			case PUT:
				request = new Request.Builder().url(url).put(requestBody).build();
				break;
			default:
				throw new SystemException(String.format("unsupported http method: '%s'.", method));
			}
		}

		if (TestFloTms.logger.isDebugEnabled()) {
			TestFloTms.logger.debug(String.format("request: '%s'.", request));
		}

		final Call call = client.newCall(request);
		try (final Response response = call.execute();) {
			if (TestFloTms.logger.isDebugEnabled()) {
				TestFloTms.logger.debug(String.format("got response code: %d.", Integer.valueOf(response.code())));
			}

			if (response.code() != 200) {
				TestFloTms.logger.error(String.format("request: '%s'.", request));
				TestFloTms.logger.error(String.format("response body: '%s'.", TestFloTms.getresponseBody(response)));
				throw new SystemException(String.format("unexpected http status code %d (expected: 200).",
						Integer.valueOf(response.code())));
			}

			return TestFloTms.getresponseBody(response);
		} catch (final IOException ex) {
			final String msg = String.format("can't execute request: '%s'.", request);

			if (!skipTimeOut) {
				TestFloTms.logger.error(msg, ex);
				throw new SystemException(msg);
			} else {
				if (TestFloTms.logger.isDebugEnabled()) {
					TestFloTms.logger.debug(msg, ex);
					TestFloTms.logger.debug("will return empty string.");
				}

				return Convert.EMPTY_STRING;
			}
		}
	}

	private String tmsPost(final HttpUrl.Builder builder, final String content) throws SystemException {
		return tmsSend(HttpMethod.POST, builder,
				Convert.isEmpty(content) ? null : RequestBody.create(MediaType.parse("application/json"), content));
	}

	private String tmsPut(final HttpUrl.Builder builder, final String content) throws SystemException {
		return tmsSend(HttpMethod.PUT, builder,
				Convert.isEmpty(content) ? null : RequestBody.create(MediaType.parse("application/json"), content));
	}

	public void moveTestPlanToNextIteration(final Issue testPlan) throws SystemException {
		if (testPlan == null) {
			throw new IllegalArgumentException("testPlan can't be null.");
		}

		if (TestFloTms.logger.isDebugEnabled()) {
			TestFloTms.logger.debug(String.format("call 'moveToNextIteration' for test plan '%s'.", testPlan.getKey()));
		}

		final HttpUrl.Builder builder = createBuilder().addPathSegment("testplan").addPathSegment("moveToNextIteration")
				.addEncodedQueryParameter("testPlanIdOrKey", testPlan.getKey());
		tmsPost(builder, TestFloTms.JSON_MOVE_TO_NEXT_ITERATION);
	}

	private void updateTestStepStatus(final Issue issue, final Integer row, final TestStepStatus status)
			throws SystemException {
		if (row == null) {
			throw new IllegalArgumentException("row can't be null.");
		}

		if (status == null) {
			throw new IllegalArgumentException("status can't be null.");
		}

		if (TestFloTms.logger.isDebugEnabled()) {
			TestFloTms.logger.debug(String.format("update status for issue: '%s' and row: %d.", issue.getKey(), row));
		}

		final String json = TestFloTmsUtils.getTestStepStatus(issue.getId(), row, status.getTestFloName());
		if (TestFloTms.logger.isDebugEnabled()) {
			TestFloTms.logger
					.debug(String.format("test case: %d: update test step %d with '%s'.", issue.getId(), row, json));
		}

		final HttpUrl.Builder builder = createBuilder().addPathSegment("steps").addPathSegment("status")
				.addEncodedQueryParameter("check-need-refresh-issue", "true");
		tmsPut(builder, json);
	}

	private void appendThumbnail(final Issue issue, final Integer row, final File attachment) throws SystemException {
		if (attachment == null) {
			throw new IllegalArgumentException("attachment can't be null.");
		}

		final int column = TestFloUtils.getActualResultIndex(issue);
		final String thumbnail = String.format(TestFloTms.MARKUP_THUMBNAIL, attachment.getName());
		final String json = TestFloTmsUtils.getTestStepCell(issue.getId(), row, Integer.valueOf(column), thumbnail);
		if (TestFloTms.logger.isDebugEnabled()) {
			TestFloTms.logger
					.debug(String.format("test case: %d: update test step %d with '%s'.", issue.getId(), row, json));
		}

		final HttpUrl.Builder builder = createBuilder().addPathSegment("steps").addPathSegment("cell");

		tmsPut(builder, json);

	}

	private void updateTestStepAttachment(final Issue issue, final Integer row, final File attachment,
			final ArtefactEnum attachmentType) throws SystemException {
		if (row == null) {
			throw new IllegalArgumentException("row can't be null.");
		}

		if (attachment == null) {
			throw new IllegalArgumentException("attachment can't be null.");
		}

		if (attachmentType == null) {
			throw new IllegalArgumentException("attachmentType can't be null.");
		}

		if (TestFloTms.logger.isDebugEnabled()) {
			TestFloTms.logger
					.debug(String.format("update attachment for issue: '%s' and row: %d.", issue.getKey(), row));
		}

		final HttpUrl.Builder builder = createBuilder().addPathSegment("steps").addPathSegment("attachment")
				.addEncodedQueryParameter("issueId", issue.getId().toString())
				.addEncodedQueryParameter("rowIndex", row.toString());

		final boolean thumbnail;
		final MediaType mediaType;
		switch (attachmentType) {
		case SCREENSHOT:
			mediaType = MediaType.parse(ContentTypeValue.PNG);
			thumbnail = true;
			break;

		case HARFILE:
			mediaType = MediaType.parse(ContentTypeValue.MULTIPART_FORM_DATA);
			thumbnail = false;
			break;

		case CSVFILE:
			mediaType = MediaType.parse(ContentTypeValue.CSV);
			thumbnail = false;
			break;

		default:
			throw new SystemException(String.format("artefactType '%s' is not implemented yet.", attachmentType));
		}

		final MultipartBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
				.addPart(Part.createFormData("file", attachment.getName(), RequestBody.create(mediaType, attachment)))
				.build();
		tmsSend(HttpMethod.POST, builder, body);

		if (thumbnail) {
			appendThumbnail(issue, row, attachment);
		}
	}

	private void updateTestStepComment(final Issue issue, final Integer row, final String comment)
			throws SystemException {
		if (row == null) {
			throw new IllegalArgumentException("row can't be null.");
		}

		if (comment == null) {
			throw new IllegalArgumentException("comment can't be null.");
		}

		if (TestFloTms.logger.isDebugEnabled()) {
			TestFloTms.logger.debug(String.format("update comment for issue: '%s' and row: %d.", issue.getKey(), row));
		}

		final String json = TestFloTmsUtils.getComment(issue.getId(), row, comment);
		if (TestFloTms.logger.isDebugEnabled()) {
			TestFloTms.logger
					.debug(String.format("test case: %d: update test step %d with '%s'.", issue.getId(), row, json));
		}

		final HttpUrl.Builder builder = createBuilder().addPathSegment("steps").addPathSegment("comment");
		tmsPut(builder, json);
	}

	public void updateTestStep(final Issue issue, final StepResult stepResult) throws SystemException {
		if (issue == null) {
			throw new IllegalArgumentException("issue can't be null.");
		}

		if (stepResult == null) {
			throw new IllegalArgumentException("stepResult can't be null.");
		}

		final Integer row = stepResult.getRow();
		updateTestStepStatus(issue, row, stepResult.getStatus());

		if (stepResult.getAttachment() != null) {
			try {
				updateTestStepAttachment(issue, row, stepResult.getAttachment(), stepResult.getAttachmentType());
			} catch (final SystemException ex) {
				TestFloTms.logger
						.error(String.format("can't upload attachment for issue '%s' and row %d -> continue work.",
								issue.getKey(), row), ex);
			}
		}

		if (!Convert.isEmpty(stepResult.getComment())) {
			try {
				updateTestStepComment(issue, row, stepResult.getComment());
			} catch (final SystemException ex) {
				TestFloTms.logger.error(
						String.format("can't update comment for issue '%s' and row %d: comment: '%s' -> continue work.",
								issue.getKey(), row),
						ex);
			}
		}
	}
}
