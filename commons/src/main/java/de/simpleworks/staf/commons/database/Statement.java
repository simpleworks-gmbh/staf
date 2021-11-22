package de.simpleworks.staf.commons.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.enums.StatementsEnum;
import de.simpleworks.staf.commons.interfaces.IPojo;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public class Statement implements IPojo {

	private static final Logger logger = LogManager.getLogger(Statement.class);
  
	private StatementsEnum type;
	private String expression;
	private String connectionId;
	private int expectedRows;

	public StatementsEnum getType() {
		return type;
	}

	public void setType(StatementsEnum type) {
		this.type = type;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getConnectionId() {
		return connectionId;
	}

	public void setConnectionId(String connectionId) {
		this.connectionId = connectionId;
	}

	public int getExpectedRows() {
		return expectedRows;
	}

	public void setExpectedRows(int expectedRows) {
		this.expectedRows = expectedRows;
	}

	@Override
	public boolean validate() {
		if (Statement.logger.isDebugEnabled()) {
			Statement.logger.debug(String.format("validate: '%s'.", toString()));
		}

		boolean result = true;

		if (type == null) {
			Statement.logger.error("type can't be null.");
			result = false;
		}

		if (Convert.isEmpty(expression)) {
			Statement.logger.error("expression can't be null or empty string.");
			result = false;
		}

		if (Convert.isEmpty(connectionId)) {
			Statement.logger.error("connectionId can't be null or empty string.");
			result = false;
		}

		return result;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s, %s, %s].", Convert.getClassName(Statement.class),
				UtilsFormat.format("type", type), UtilsFormat.format("expression", expression),
				UtilsFormat.format("connectionId", connectionId), UtilsFormat.format("expectedRows", expectedRows));
	}

}