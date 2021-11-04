package de.simpleworks.staf.plugin.maven.testflo.commons;

import java.io.File;

import de.simpleworks.staf.commons.enums.ArtefactEnum;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;
import de.simpleworks.staf.plugin.maven.testflo.commons.enums.TestStepStatus;

public class StepResult {
	private final Integer row;
	private final TestStepStatus status;
	private final String comment;
	private final File attachment;
	private final ArtefactEnum attachmentType;

	public StepResult(final Integer row, final TestStepStatus status, final String comment, final File attachment,
			final ArtefactEnum attachmentType) {
		if (row == null) {
			throw new IllegalArgumentException("row can't be null.");
		}

		if (status == null) {
			throw new IllegalArgumentException("status can't be null.");
		}

		this.row = row;
		this.status = status;
		this.comment = comment;
		this.attachment = attachment;
		this.attachmentType = attachmentType;
	}

	public Integer getRow() {
		return row;
	}

	public TestStepStatus getStatus() {
		return status;
	}

	public String getComment() {
		return comment;
	}

	public File getAttachment() {
		return attachment;
	}

	public ArtefactEnum getAttachmentType() {
		return attachmentType;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s, %s]", Convert.getClassName(this), UtilsFormat.format("row", row),
				UtilsFormat.format("status", status), UtilsFormat.format("comment", comment),
				UtilsFormat.format("attachment", attachment), UtilsFormat.format("attachmentType", attachmentType));
	}
}
