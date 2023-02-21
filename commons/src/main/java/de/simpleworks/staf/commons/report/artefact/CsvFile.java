package de.simpleworks.staf.commons.report.artefact;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.enums.ArtefactEnum;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsCollection;

public class CsvFile extends Artefact<String> {
	private static final Logger logger = LogManager.getLogger(CsvFile.class);
	private static final String CSV_DELIMETER = ";";

	private final List<String> expetcedColumns;
	private final List<Map<String, String>> rows;

	public CsvFile(final String[] columns) {

		this.expetcedColumns = UtilsCollection.toList(columns);
		rows = new ArrayList<>();
		this.type = ArtefactEnum.CSVFILE;
	}

	public boolean addRow(Map<String, String> row) {

		if (Convert.isEmpty(row)) {
			throw new IllegalArgumentException("row can't be null or empty.");
		}

		for (final String column : expetcedColumns) {

			final String cell = row.getOrDefault(column, null);

			if (cell == null) {
				CsvFile.logger.error(String.format("column \"%s\" is not set.", column));
				return false;
			}
		}

		return rows.add(row);
	}

	@Override
	public String getArtefact() {

		final List<String> content = new ArrayList<>();
		final String columnLine = String.join(CSV_DELIMETER, expetcedColumns);

		if (CsvFile.logger.isDebugEnabled()) {
			CsvFile.logger.debug(String.format("add column \"%s\".", columnLine));
		}

		content.add(columnLine);

		for (Map<String, String> row : rows) {
			final List<String> orderedRow = new ArrayList<>();
			for (final String column : expetcedColumns) {
				orderedRow.add(row.get(column));
			}
			final String rowLine = String.join(CSV_DELIMETER, orderedRow);

			if (CsvFile.logger.isDebugEnabled()) {
				CsvFile.logger.debug(String.format("add row \"%s\".", rowLine));
			}

			content.add(rowLine);
		}

		final String result = String.join("\r\n", content);
		return result;
	}
}
