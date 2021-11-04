package de.simpleworks.staf.plugin.maven.testflo.commons;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public final class TestFloTmsUtils {
	private TestFloTmsUtils() {
		throw new IllegalStateException("utility class.");
	}

	static class Comment {
		private final Long issueId;
		private final Integer rowIndex;
		private final String comment;

		public Comment(final Long issueId, final Integer rowIndex, final String comment) {
			if (issueId == null) {
				throw new IllegalArgumentException("issueId can't be null.");
			}

			if (rowIndex == null) {
				throw new IllegalArgumentException("rowIndex can't be null.");
			}

			if (comment == null) {
				throw new IllegalArgumentException("comment can't be null.");
			}

			this.issueId = issueId;
			this.rowIndex = rowIndex;
			this.comment = comment;
		}

		@Override
		public String toString() {
			return String.format("[%s: %s, %s, %s]", Convert.getClassName(Comment.class),
					UtilsFormat.format("issueId", issueId), UtilsFormat.format("rowIndex", rowIndex),
					UtilsFormat.format("comment", comment));
		}
	}

	static class TestStepStatus {
		private final Long issueId;
		private final Integer rowIndex;
		private final String status;

		public TestStepStatus(final Long issueId, final Integer rowIndex, final String status) {
			if (issueId == null) {
				throw new IllegalArgumentException("issueId can't be null.");
			}

			if (rowIndex == null) {
				throw new IllegalArgumentException("rowIndex can't be null.");
			}

			if (status == null) {
				throw new IllegalArgumentException("status can't be null.");
			}

			this.issueId = issueId;
			this.rowIndex = rowIndex;
			this.status = status;
		}

		@Override
		public String toString() {
			return String.format("[%s: %s, %s, %s]", Convert.getClassName(TestStepStatus.class),
					UtilsFormat.format("issueId", issueId), UtilsFormat.format("rowIndex", rowIndex),
					UtilsFormat.format("status", status));
		}
	}

	static class TestStepCell {
		private final Long issueId;
		private final Integer rowIndex;
		private final Integer columnIndex;
		private final String cellValue;

		public TestStepCell(final Long issueId, final Integer rowIndex, final Integer columnIndex,
				final String cellValue) {
			if (issueId == null) {
				throw new IllegalArgumentException("issueId can't be null.");
			}

			if (rowIndex == null) {
				throw new IllegalArgumentException("rowIndex can't be null.");
			}

			if (columnIndex == null) {
				throw new IllegalArgumentException("columnIndex can't be null.");
			}

			if (cellValue == null) {
				throw new IllegalArgumentException("cellValue can't be null.");
			}

			this.issueId = issueId;
			this.rowIndex = rowIndex;
			this.columnIndex = columnIndex;
			this.cellValue = cellValue;
		}

		@Override
		public String toString() {
			return String.format("[%s: %s, %s, %s, %s]", Convert.getClassName(TestStepCell.class),
					UtilsFormat.format("issueId", issueId), UtilsFormat.format("rowIndex", rowIndex),
					UtilsFormat.format("columnIndex", columnIndex), UtilsFormat.format("cellValue", cellValue));
		}
	}

	private static Gson getGson() {
		return new GsonBuilder().create();
	}

	public static String getComment(final Long issueId, final Integer rowIndex, final String comment) {
		return TestFloTmsUtils.getGson().toJson(new Comment(issueId, rowIndex, comment));
	}

	public static String getTestStepStatus(final Long issueId, final Integer rowIndex, final String status) {
		return TestFloTmsUtils.getGson().toJson(new TestStepStatus(issueId, rowIndex, status));
	}

	public static String getTestStepCell(final Long issueId, final Integer rowIndex, final Integer columnIndex,
			final String cellValue) {
		return TestFloTmsUtils.getGson().toJson(new TestStepCell(issueId, rowIndex, columnIndex, cellValue));
	}
}
