package de.simpleworks.staf.plugin.maven.xray.commons;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.simpleworks.staf.commons.elements.TestCase;
import de.simpleworks.staf.commons.elements.TestStep;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsIO;
import de.simpleworks.staf.plugin.maven.xray.utils.XrayToken;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class Xray {

	private static final Logger logger = LogManager.getLogger(Xray.class);

	// Error code that indicates, to many requests in short time frame
	// https://confluence.atlassian.com/adminjiraserver/adjusting-your-code-for-rate-limiting-987143384.html
	private static final int RATE_LIMIT_EXCEEDED_STATUS_CODE = 429;
	private static final String RETRY_HEADER = "retry-after";

	private final URL backend;
	private final URL authentication;
	private final OkHttpClient client;

	private XrayToken token;

	public Xray(final URL backend, final URL authentication, final OkHttpClient client) {

		if (backend == null) {
			throw new IllegalArgumentException("backend can't be null.");
		}

		this.backend = backend;

		if (authentication == null) {
			throw new IllegalArgumentException("authentication can't be null.");
		}

		this.authentication = authentication;

		if (client == null) {
			throw new IllegalArgumentException("client can't be null.");
		}

		this.client = client;
	}

	public XrayToken getToken() {
		return token;
	}

	public void setToken(final String clientId, final String clientSecret) throws Exception {
		Xray.logger.debug("Set api token for further actions.");

		if (Convert.isEmpty(clientId)) {
			throw new IllegalArgumentException("clientId can't be null or empty string.");
		}

		if (Convert.isEmpty(clientSecret)) {
			throw new IllegalArgumentException("clientSecret can't be null or empty string.");
		}

		final Request request = new Request.Builder().url(authentication)
				.post(RequestBody.create(MediaType.parse("application/json"),
						String.format("{\"client_id\": \"%s\",\"client_secret\": \"%s\"}", clientId, clientSecret)))
				.build();

		final Call call = client.newCall(request);

		String bearerToken = Convert.EMPTY_STRING;

		try (Response response = call.execute()) {
			try (ResponseBody rb = response.body()) {
				Assert.assertEquals(200, response.code());
				try (BufferedInputStream input = new BufferedInputStream(rb.byteStream())) {
					bearerToken = UtilsIO.getAllContentFromInputStream(input).replaceAll("^\"|\"$", "");
				}
			}
		}

		Assert.assertFalse("bearerToken not received.", Convert.isEmpty(bearerToken));

		this.token = new XrayToken(bearerToken);
	}

	public JsonObject fetchResponseFromXray(final JsonObject payload) throws IOException {
		if (token == null) {
			throw new IllegalArgumentException("token can't be null.");
		}

		if (payload == null) {
			throw new IllegalArgumentException("payload can't be null.");
		}

		final Request request = new Request.Builder().url(backend)
				.header("Authorization", String.format("Bearer %s", token.getBearerToken()))
				.post(RequestBody.create(MediaType.parse("application/json"), payload.toString())).build();

		final Call call = client.newCall(request);

		JsonObject responseAsJson = null;

		try (Response response = call.execute()) {

			if (Xray.RATE_LIMIT_EXCEEDED_STATUS_CODE == response.code()) {

				final String retryAfter = response.header(Xray.RETRY_HEADER, "0");
				final int nextRetryAttempt = Integer.parseInt(retryAfter) * 1002;

				if (nextRetryAttempt > 0) {

					if (Xray.logger.isDebugEnabled()) {
						Xray.logger.debug(String.format(
								"need to wait \"%s\" seconds for the next attempt to access xray.", retryAfter));
					}

					try {
						Thread.sleep(nextRetryAttempt);
						return this.fetchResponseFromXray(payload);
					} catch (final Exception ex) {
						Xray.logger.error(String.format(
								"can't wait \"%s\" seconds, will try another attempt to access xray.", retryAfter), ex);
					}
				}
			}

			Assert.assertEquals(200, response.code());

			try (ResponseBody bodyAsStream = response.body()) {
				final String bodyAsString = bodyAsStream.string();
				Assert.assertFalse(Convert.isEmpty(bodyAsString));
				responseAsJson = new JsonParser().parse(bodyAsString).getAsJsonObject();
			}
		}

		Assert.assertNotNull("response json is null.", responseAsJson);

		return responseAsJson;
	}

	public TestCase readTestCase(final String key, final long id) {
		TestCase result = null;

		final String query = String.format(
				"query{ getTest(issueId: \"%s\") { steps { id data action result attachments { id filename } } }}",
				Long.valueOf(id));

		final JsonObject payload = new JsonObject();
		payload.addProperty("query", query);

		JsonObject response = null;
		try {
			response = fetchResponseFromXray(payload);
		} catch (final IOException e) {
			final String message = String.format("Error fetching response for testcase with id \"%s\".",
					Long.valueOf(id));
			Xray.logger.error(message, e);
		}

		if (response == null) {
			final String message = String.format("Error fetching response for testcase with id \"%s\".",
					Long.valueOf(id));
			Xray.logger.error(message);
			throw new IllegalArgumentException(message);
		}

		result = Xray.readTestCase(key, response);

		return result;
	}

	private static TestCase readTestCase(final String key, final JsonObject response) {
		final TestCase result = new TestCase();

		result.setId(key);

		final List<TestStep> testSteps = new ArrayList<>();

		final JsonArray steps = response.get("data").getAsJsonObject().get("getTest").getAsJsonObject().get("steps")
				.getAsJsonArray();

		for (int i = 0; i < steps.size(); i++) {
			final JsonObject stepObject = steps.get(i).getAsJsonObject();
			final String action = stepObject.get("action").getAsString();

			final TestStep step = new TestStep(i, action);
			testSteps.add(step);
		}

		result.setTestSteps(testSteps);

		return result;
	}
}
