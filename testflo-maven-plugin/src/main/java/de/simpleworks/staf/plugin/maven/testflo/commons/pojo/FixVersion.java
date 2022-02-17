package de.simpleworks.staf.plugin.maven.testflo.commons.pojo;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public class FixVersion {

	private String self;
	private String id;
	private String description;
	private String name;
	private boolean archived;
	private boolean released;
	private String startDate;
	private String releaseDate;
	private String userStartDate;
	private String userReleaseDate;
	private int projectId;

	public String getSelf() {
		return self;
	}

	public void setSelf(String self) {
		this.self = self;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	public boolean isReleased() {
		return released;
	}

	public void setReleased(boolean released) {
		this.released = released;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}

	public String getUserStartDate() {
		return userStartDate;
	}

	public void setUserStartDate(String userStartDate) {
		this.userStartDate = userStartDate;
	}

	public String getUserReleaseDate() {
		return userReleaseDate;
	}

	public void setUserReleaseDate(String userReleaseDate) {
		this.userReleaseDate = userReleaseDate;
	}

	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s, %s, %s, %s, %s, %s,]", Convert.getClassName(FixVersion.class),
				UtilsFormat.format("self", self), UtilsFormat.format("id", id),
				UtilsFormat.format("description", description), UtilsFormat.format("name", name),
				UtilsFormat.format("archived", archived), UtilsFormat.format("released", released),
				UtilsFormat.format("startDate", startDate), UtilsFormat.format("releaseDate", releaseDate),
				UtilsFormat.format("userStartDate", userStartDate),
				UtilsFormat.format("userReleaseDate", userReleaseDate), UtilsFormat.format("projectId", projectId)

		);
	}
}
