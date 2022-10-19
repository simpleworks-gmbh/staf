package de.simpleworks.staf.commons.database;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.enums.DbResultsEnum;

/**
 * @brief pojo that describes the result of an executed Query
 */
public class QueuedDbResult implements IDbResult<DbResultRow> {

	private static final Logger logger = LogManager.getLogger(QueuedDbResult.class);

	private final DbResultRow rows;

	public QueuedDbResult() {
		rows = new DbResultRow();
	}

	@Override
	public boolean validate() {

		if (QueuedDbResult.logger.isTraceEnabled()) {
			QueuedDbResult.logger.trace("validate UpdateDbResult...");
		}

		return true;
	}

	public boolean add(final Map<String, String> row) {

		if (row == null) {
			throw new IllegalArgumentException("row can't be null.");
		}

		return rows.add(row);
	}

	@Override
	public DbResultRow getResult() {
		return rows;
	}

	@Override
	public DbResultsEnum getDbResultsEnum() {
		return DbResultsEnum.SELECTED;
	}
}
