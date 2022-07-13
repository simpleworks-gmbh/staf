package de.simpleworks.staf.plugin.maven.testflo.commons;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.util.concurrent.Promise;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.plugin.maven.testflo.commons.enums.TestCaseGeneral;

public class TestFloFields {

	private static final Logger logger = LogManager.getLogger(TestFloFields.class);
	private final IssueRestClient jira;

	private final TestFloLabel testFloLabel;
	

	public TestFloFields(final IssueRestClient jira, final boolean keepJiraLabel) {
		if (jira == null) {
			throw new IllegalArgumentException("jira can't be null.");
		}
		this.jira = jira;

		this.testFloLabel = new TestFloLabel(this.jira, keepJiraLabel);
	}

	private List<String> fetchJiraFields(Issue jiraIssue, List<String> customFields) throws Exception {

		if (jiraIssue == null) {
			throw new IllegalArgumentException("jiraIssue can't be null.");
		}

		if (Convert.isEmpty(customFields)) {
			throw new IllegalArgumentException("customFields can't be null or empty.");
		}

		final List<String> result = new ArrayList<String>();

		for (String customField : customFields) {
			final String fieldValue = TestFloUtils.getField(jiraIssue, customField);

			if (!Convert.isEmpty(fieldValue)) {

				if (TestFloFields.logger.isDebugEnabled()) {
					TestFloFields.logger.debug(String.format("fetched '%s':'%s'", customField, fieldValue));
				}

				TestFloField[] fields = (new ObjectMapper()).readValue(fieldValue, TestFloField[].class);

				if (Convert.isEmpty(fields)) {
					if (TestFloFields.logger.isDebugEnabled()) {
						TestFloFields.logger.debug(String.format("customField '%s' is empty, will skip.", customField));
					}
					continue;
				}

				TestFloField field = fields[0];

				result.add(field.getValue());
			}
		}

		return result;
	}

	public void addFields(String issueKey, List<String> customFields) {

		if (Convert.isEmpty(issueKey)) {
			throw new IllegalArgumentException("issueKey can't be null or empty string.");
		}

		if (Convert.isEmpty(customFields)) {
			throw new IllegalArgumentException("customFields can't be null or empty.");
		}

		String jiraTestcaseTemplateIssueKey = Convert.EMPTY_STRING;

		try {
			Promise<Issue> promiseIssue = jira.getIssue(issueKey);

			Issue jiraIssue = promiseIssue.claim();
			jiraTestcaseTemplateIssueKey = TestFloUtils.getField(jiraIssue, TestCaseGeneral.TEMPLATE.getTestFloName());

		} catch (Exception ex) {
			TestFloFields.logger.error(String.format("can't fetch TestcaseTemplate issue from '%s' by using \"%s\".",
					issueKey, TestCaseGeneral.TEMPLATE.getTestFloName()), ex);
			return;
		}

		List<String> jiraFields = new ArrayList<String>();

		try {
			Promise<Issue> promiseIssue = jira.getIssue(jiraTestcaseTemplateIssueKey);

			Issue jiraIssue = promiseIssue.claim();
			jiraFields = fetchJiraFields(jiraIssue, customFields);
		} catch (Exception ex) {
			TestFloFields.logger.error(String.format("can't fetch issue from '%s'.", jiraTestcaseTemplateIssueKey), ex);
			return;
		}

		testFloLabel.addLabels(issueKey, jiraFields);
	}
}

class TestFloField {

	private String self;
	private String value;
	private String id;
	private boolean disabled;

	public String getSelf() {
		return self;
	}

	public void setSelf(String self) {
		this.self = self;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
}
