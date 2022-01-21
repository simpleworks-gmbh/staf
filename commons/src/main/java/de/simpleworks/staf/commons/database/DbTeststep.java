package de.simpleworks.staf.commons.database;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.api.Assertion;
import de.simpleworks.staf.commons.interfaces.ITeststep;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public class DbTeststep implements ITeststep {

	private static final Logger logger = LogManager.getLogger(DbTeststep.class);

	private String name;
	private int order;
	private Statement statement;

	private Assertion[] assertions;

	public DbTeststep() {
		name = Convert.EMPTY_STRING;
		order = -1;
		statement = new Statement();
		assertions = new Assertion[0];
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getOrder() {
		return order;
	}

	public Statement getStatement() {
		return statement;
	}

	public Assertion[] getAssertions() {
		return assertions;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setOrder(final int order) {
		this.order = order;
	}

	public void setStatement(Statement statement) {
		this.statement = statement;
	}

	public void setAssertions(final Assertion[] assertions) {
		this.assertions = assertions;
	}

	@Override
	public boolean validate() {
		if (DbTeststep.logger.isDebugEnabled()) {
			DbTeststep.logger.debug("validate DbTeststep...");
		}

		boolean result = true;

		if (Convert.isEmpty(name)) {
			if (DbTeststep.logger.isDebugEnabled()) {
				DbTeststep.logger.debug("name can't be null or empty string.");
			}
			result = false;
		}

		if (order < 1) {
			if (DbTeststep.logger.isDebugEnabled()) {
				DbTeststep.logger
						.debug(String.format("order can't be less than 1, but was \"%s\".", Integer.toString(order)));
			}
			result = false;
		}

		if (!statement.validate()) {
			if (DbTeststep.logger.isDebugEnabled()) {
				DbTeststep.logger.debug(String.format("statement \"%s\" is invalid.", statement));
			}
			result = false;
		}

		if (!Convert.isEmpty(assertions)) {

			List<Assertion> currentAssertions = Arrays.asList(assertions);

			if (currentAssertions.stream().filter(a -> a.validate()).collect(Collectors.toList()).isEmpty()) {
				DbTeststep.logger.error(String.format("assertions are invalid [%s].", String.join(",",
						Arrays.asList(assertions).stream().map(a -> a.toString()).collect(Collectors.toList()))));
				result = false;
			}

			for (Assertion assertion : currentAssertions) {

				if (currentAssertions.indexOf(assertion) != currentAssertions.lastIndexOf(assertion)) {
					DbTeststep.logger.error(String
							.format("assertion \"%s\" is used at last two times, which is not supported.", assertion));
					result = false;
					break;
				}
			}

		}

		return result;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s, %s, %s]", Convert.getClassName(DbTeststep.class),
				UtilsFormat.format("name", name), UtilsFormat.format("order", order),
				UtilsFormat.format("statement", statement), UtilsFormat.format("assertions", String.join(",",
						Arrays.asList(assertions).stream().map(a -> a.toString()).collect(Collectors.toList()))));
	}

}
