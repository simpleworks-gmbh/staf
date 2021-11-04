package de.simpleworks.staf.module.jira.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;
import de.simpleworks.staf.module.jira.interfaces.IJiraIssue;
import de.simpleworks.staf.module.jira.util.JiraProperties;
import de.simpleworks.staf.module.jira.util.enums.TaskLabel;

public abstract class Task implements IJiraIssue {
	private static final long serialVersionUID = 7795792566703865713L;

	private static final Logger logger = LogManager.getLogger(Task.class);
	private final JiraProperties properties = JiraProperties.getInstance();

	private String key;
	private String projectKey;
	private String summary;
	private String description;

	private List<TaskLabel> labels = new ArrayList<>();

	public void validate() throws SystemException {
		if (Task.logger.isInfoEnabled()) {
			Task.logger.info(String.format("validate %s..", this));
		}

		if (Convert.isEmpty(key)) {
			throw new SystemException("key can't be empty.");
		}

		if (Convert.isEmpty(projectKey)) {
			throw new SystemException("projectKey can't be empty.");
		}

		if (Convert.isEmpty(summary)) {
			throw new SystemException("summary can't be empty.");
		}
	}

	public String getKey() {
		return key;
	}

	public void setKey(final String key) {
		this.key = key;
	}

	public String getProjectKey() {
		return projectKey;
	}

	public void setProjectKey(final String projectKey) {
		this.projectKey = projectKey;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(final String summary) {
		this.summary = summary;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public List<TaskLabel> getLabels() {
		return labels;
	}

	public void setLabels(final List<TaskLabel> labels) {
		this.labels = labels;
	}

	public boolean addLabel(final TaskLabel taskLabel) {
		if (taskLabel == null) {
			throw new IllegalArgumentException("taskLabel can't be null.");
		}

		final Optional<TaskLabel> optional = getLabels().stream().filter(label -> taskLabel.equals(label)).findAny();

		if (optional.isPresent()) {
			return false;
		}

		return this.labels.add(taskLabel);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Task) {

			final Task task = (Task) obj;

			final boolean summaryFlag = this.getSummary().equals(task.getSummary());

			if (summaryFlag) {
				if (Task.logger.isDebugEnabled()) {
					Task.logger.debug(String.format("Summaries are not equal \"%s\" != \"%s\".", this.getSummary(),
							task.getSummary()));
				}
			}

			final boolean keyFlag = this.getKey().equals(task.getKey());

			if (keyFlag) {
				if (Task.logger.isDebugEnabled()) {
					Task.logger
							.debug(String.format("Keys are not equal \"%s\" != \"%s\".", this.getKey(), task.getKey()));
				}
			}

			final boolean getProjectKeyFlag = this.getProjectKey().equals(task.getProjectKey());

			if (getProjectKeyFlag) {
				if (Task.logger.isDebugEnabled()) {
					Task.logger.debug(String.format("ProjectKey are not equal \"%s\" != \"%s\".", this.getProjectKey(),
							task.getProjectKey()));
				}
			}

			return summaryFlag && keyFlag && getProjectKeyFlag;
		}

		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((key == null) ? 0 : key.hashCode());
		result = (prime * result) + ((projectKey == null) ? 0 : projectKey.hashCode());
		result = (prime * result) + ((summary == null) ? 0 : summary.hashCode());
		result = (prime * result) + ((description == null) ? 0 : description.hashCode());
		return result;
	}

	@Override
	public long getIssueType() {
		return properties.getIssueType();
	}

	public abstract boolean checkLabels() throws SystemException;

	@Override
	public String toString() {
		return String.format("[%s: %s, %s, %s, %s, %s]", Convert.getClassName(Task.class),
				UtilsFormat.format("key", key), UtilsFormat.format("projectKey", projectKey),
				UtilsFormat.format("summary", summary), UtilsFormat.format("description", description),
				UtilsFormat.format("labels", String.join(", ",
						labels.stream().map(label -> label.getValue()).collect(Collectors.toList()))));
	}

}
