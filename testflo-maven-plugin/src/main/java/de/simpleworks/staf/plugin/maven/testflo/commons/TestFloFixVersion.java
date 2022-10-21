package de.simpleworks.staf.plugin.maven.testflo.commons;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.util.concurrent.Promise;
import com.jayway.jsonpath.JsonPath;

import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.commons.utils.UtilsIO;
import de.simpleworks.staf.module.jira.util.JiraProperties;
import de.simpleworks.staf.module.jira.util.JiraRateLimitingEffect;
import de.simpleworks.staf.plugin.maven.testflo.commons.pojo.FixVersion;
import net.minidev.json.JSONArray;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class TestFloFixVersion {

	private static final Logger logger = LogManager.getLogger(TestFloFixVersion.class);
	private final OkHttpClient httpClient;
	private final IssueRestClient jira;
	private final JiraProperties properties;

	public TestFloFixVersion(final OkHttpClient httpClient, final IssueRestClient jira,
			final JiraProperties properties) {
		if (httpClient == null) {
			throw new IllegalArgumentException("httpClient can't be null.");
		}
		this.httpClient = httpClient;
		if (jira == null) {
			throw new IllegalArgumentException("jira can't be null.");
		}
		this.jira = jira;
		if (properties == null) {
			throw new IllegalArgumentException("properties can't be null.");
		}
		this.properties = properties;
	}

	public void addFixVersions(final List<FixVersion> fetchedFixedVersion, final String id) {
		if (Convert.isEmpty(fetchedFixedVersion)) {
			throw new IllegalArgumentException("fetchedFixedVersion can't be null or empty string.");
		}
		if (Convert.isEmpty(id)) {
			throw new IllegalArgumentException("id can't be null or empty string.");
		}
		try {
			final Promise<Issue> promise = jira.getIssue(id);
			final Issue issue = promise.fail(new JiraRateLimitingEffect()).claim();
			if (issue == null) {
				throw new RuntimeException("issue can't be null.");
			}
			final IssueInputBuilder issueBuilder = new IssueInputBuilder();
			List<Version> versions = new ArrayList<Version>();
			for (final FixVersion fixVersion : fetchedFixedVersion) {
				if (!versions.add(new Version(new URI(fixVersion.getSelf()), null, fixVersion.getName(),
						fixVersion.getDescription(), false, false, null))) {
					TestFloFixVersion.logger.error(String.format("can't add version '%s'.", fixVersion.getId()));
				}
			}
			issueBuilder.setFixVersions(versions);
			final IssueInput newIssue = issueBuilder.build();
			if (newIssue == null) {
				throw new IllegalArgumentException("newIssue can't be null or empty string.");
			}
			final Promise<Void> update = jira.updateIssue(id, newIssue);
			update.fail(new JiraRateLimitingEffect()).claim();
		} catch (Exception ex) {
			if (TestFloFixVersion.logger.isDebugEnabled()) {
				TestFloFixVersion.logger.debug(
						String.format("can't add fix version '%s' for testcase '%s'.", String.join(", ",
								fetchedFixedVersion.stream().map(v -> v.toString()).collect(Collectors.toList())), id),
						ex);
			}
		}
	}

	public FixVersion fetchFixVersion(final String fixVersion, final String id) throws Exception {
		return fetchFixVersion(this.properties.getUrl(), this.properties.getUsername(), this.properties.getPassword(),
				fixVersion, id);
	}

	private FixVersion fetchFixVersion(final URL jiraURL, final String username, final String password,
			final String fixVersion, final String id) throws SystemException {
		if (jiraURL == null) {
			throw new IllegalArgumentException("jiraURL can't be null.");
		}
		if (Convert.isEmpty(username)) {
			throw new IllegalArgumentException("username can't be null or empty string.");
		}
		if (Convert.isEmpty(password)) {
			throw new IllegalArgumentException("password can't be null or empty string.");
		}
		if (Convert.isEmpty(fixVersion)) {
			throw new IllegalArgumentException("fixVersion can't be null or empty string.");
		}
		if (Convert.isEmpty(id)) {
			throw new IllegalArgumentException("id can't be null or empty string.");
		}
		// fetch fixVersion
		FixVersion result;
		try {
			final Promise<Issue> promise = jira.getIssue(id);
			final Issue issue = promise.fail(new JiraRateLimitingEffect()).claim();
			if (issue == null) {
				throw new RuntimeException("issue can't be null.");
			}

			final URL url = new URL(String.format("%s/rest/api/latest/project/%s/version?maxResults=1000000&startAt=0",
					jiraURL, issue.getProject().getKey()));
			if (TestFloFixVersion.logger.isDebugEnabled()) {
				TestFloFixVersion.logger.debug(String.format("use url '%s' to fetch available fix versions .", url));
			}
			final Request request = new Request.Builder().url(url).build();
			final Call call = this.httpClient.newCall(request);
			final Response response = call.execute();

			Assert.assertTrue(
					String.format("Status Code 200 was expected, but was '%s'.", Integer.toString(response.code())),
					(200 == response.code()));
			final ResponseBody responseBody = response.body();
			final String json = UtilsIO.getAllContentFromBytesArray(responseBody.bytes());
			if (Convert.isEmpty(json)) {
				throw new RuntimeException("json can't be null or empty.");
			}
			final JSONArray jsonArray = (JSONArray) JsonPath.read(json,
					String.format("$.values[?(@.name == '%s')]", fixVersion));

			final FixVersion[] fixVersions = (new ObjectMapper()).readValue(jsonArray.toJSONString(),
					FixVersion[].class);

			Assert.assertTrue(
					String.format("expcted is one fix version, but there are '%s' ones ['%s'].",
							Integer.toString(fixVersions.length), String.join(", ", UtilsCollection.toList(fixVersions)
									.stream().map(fix -> fix.toString()).collect(Collectors.toList()))),
					(fixVersions.length == 1));

			result = fixVersions[0];
		} catch (Exception ex) {
			final String msg = "can't fetch Fix Version.";
			logger.error(msg, ex);
			throw new SystemException(msg);
		}
		return result;
	}
}