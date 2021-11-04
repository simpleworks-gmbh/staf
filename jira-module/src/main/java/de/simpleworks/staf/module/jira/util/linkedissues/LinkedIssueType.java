package de.simpleworks.staf.module.jira.util.linkedissues;

import com.atlassian.jira.rest.client.api.domain.IssueLinkType;

public class LinkedIssueType extends IssueLinkType {
	public LinkedIssueType(final String name, final String description, final Direction direction) {
		super(name, description, direction);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			throw new IllegalArgumentException("obj can't be null.");
		}

		if (obj instanceof IssueLinkType) {
			final IssueLinkType type = (IssueLinkType) obj;

			final boolean nameFlag = type.getName().equals(this.getName());

			final boolean descriptionFlag = type.getDescription().equals(this.getDescription());

			final boolean directionFlag = type.getDirection().equals(this.getDirection());

			return (nameFlag) && (descriptionFlag) && (directionFlag);
		}

		return false;
	}
}
