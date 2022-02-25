package de.simpleworks.staf.framework.elements.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Module;

import de.simpleworks.staf.commons.api.Assertion;
import de.simpleworks.staf.commons.database.DbResultRow;
import de.simpleworks.staf.commons.database.DbTeststep;
import de.simpleworks.staf.commons.database.IDbResult;
import de.simpleworks.staf.commons.database.QueuedDbResult;
import de.simpleworks.staf.commons.database.Statement;
import de.simpleworks.staf.commons.database.connection.DbConnectionPool;
import de.simpleworks.staf.commons.enums.DbResultsEnum;
import de.simpleworks.staf.commons.enums.StatementsEnum;
import de.simpleworks.staf.commons.enums.ValidateMethodEnum;
import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.mapper.database.MapperDbTeststep;
import de.simpleworks.staf.commons.report.artefact.CsvFile;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.framework.api.httpclient.TeststepProvider;
import de.simpleworks.staf.framework.database.DbConnectionManagerImpl;
import de.simpleworks.staf.framework.elements.api.RewriteUrlObject;
import de.simpleworks.staf.framework.elements.commons.TemplateTestCase;
import de.simpleworks.staf.framework.util.AssertionUtils;
import de.simpleworks.staf.framework.util.assertion.DbResultAssertionValidator;
import net.lightbody.bmp.BrowserMobProxyServer;

public class DbTestCase extends TemplateTestCase<DbTeststep, QueuedDbResult> {

	private static final Logger logger = LogManager.getLogger(DbTestCase.class);

	private final static String ENVIRONMENT_VARIABLES_NAME = "DbTestCase";

	private final DbConnectionManagerImpl databaseconnectionimpl;

	private String currentstepname;
	private Statement currentStatement;
	private Assertion[] currentAssertions;
	private QueuedDbResult currentResult;

	protected DbTestCase(final String resource, final Module... modules) throws SystemException {
		super(resource, DbTestCase.ENVIRONMENT_VARIABLES_NAME, new MapperDbTeststep(), modules);

		try {
			databaseconnectionimpl = new DbConnectionManagerImpl();
		} catch (final InstantiationException ex) {
			DbTestCase.logger.error(ex);
			throw new SystemException("can't set up database connection manager.");
		}
	}

	private static final Map<String, String> checkDbResult(final QueuedDbResult result, final Assertion assertion) {
		return new DbResultAssertionValidator().validateAssertion(result, assertion);
	}

	@Override
	public void executeTestStep() throws Exception {

		// add error handling
		getNextTeststep();

		final Statement current = getCurrentStatement();
		final Assertion[] assertions = getCurrentAssertions();
		currentResult = new QueuedDbResult();

		final DbTestResult result = runStatement(current, UtilsCollection.toList(assertions));
		AssertionUtils.assertTrue(result.getErrormessage(), result.isSuccessfull());

		addExtractedValues(currentstepname, result.getExtractedValues());
	}

	private Statement getCurrentStatement() {
		return currentStatement;
	}

	@Override
	protected DbTeststep updateTeststep(final DbTeststep step, final Map<String, Map<String, String>> values)
			throws SystemException {

		if (step == null) {
			throw new IllegalArgumentException("step can't be null.");
		}

		if (!step.validate()) {
			throw new IllegalArgumentException(String.format("Step '%s' is invalid.", step));
		}

		if (values == null) {
			throw new IllegalArgumentException("value can't be null.");
		}

		if (values.keySet().isEmpty()) {
			throw new IllegalArgumentException("extractedValues can't be empty.");
		}

		if (DbTestCase.logger.isDebugEnabled()) {
			DbTestCase.logger.debug("update values.");
		}

		final DbTeststep result = step;

		try {
			final Statement statement = step.getStatement();
			final Statement updatedStatement = updateFields(Statement.class, statement, values);
			result.setStatement(updatedStatement);

			if (!Convert.isEmpty(Arrays.asList(step.getAssertions()))) {
				final Assertion[] assertions = step.getAssertions();
				final Assertion[] updatedAssertions = new Assertion[assertions.length];

				for (int itr = 0; itr < assertions.length; itr += 1) {
					final Assertion assertion = updateFields(Assertion.class, assertions[itr], values);
					updatedAssertions[itr] = assertion;
				}

				result.setAssertions(updatedAssertions);
			}
		} catch (final Exception ex) {
			final String message = "can't update database test step.";
			DbTestCase.logger.error(message, ex);
			throw new SystemException(message);
		}

		return result;
	}

	// Database connnections will be closed at the end of the test
	@SuppressWarnings("resource")
	private DbTestResult runStatement(final Statement statement, final List<Assertion> assertions)
			throws SystemException {

		if (statement == null) {
			throw new IllegalArgumentException("statement can't be null.");
		}

		if (!statement.validate()) {
			throw new IllegalArgumentException(String.format("statement '%s' is invalid.", statement));
		}

		final DbTestResult result = new DbTestResult();

		final DbConnectionPool connectionPool = databaseconnectionimpl.get();

		if (connectionPool == null) {
			throw new SystemException("connectionPool can't be null.");
		}

		final String connectionId = statement.getConnectionId();
		final Connection conn = connectionPool.getConnection(connectionId);

		try {

			final StatementsEnum type = statement.getType();

			switch (type) {

			case SELECT:
				currentResult = DbTestCase.runSelectStatement(conn, statement);
				break;

			case QUERY:
				currentResult = DbTestCase.runQueryStatement(conn, statement);
				break;
			default:
				throw new IllegalArgumentException(String.format("type '%s' is not implemented yet.", type.getValue()));
			}

			DbTestCase.validateExpectedRows(currentResult, statement);

			if (!Convert.isEmpty(assertions)) {
				final Map<String, String> values = validateAssertions(currentResult,
						UtilsCollection.toList(assertions));
				result.setExtractedValues(values);
			}

			result.setSuccessfull(true);
		} catch (final Throwable th) {
			final String msg = String.format("Statement '%s' failed.", statement);
			DbTestCase.logger.error(msg, th);
			result.setErrormessage(th.getMessage());
			result.setSuccessfull(false);
		}

		if (DbTestCase.logger.isDebugEnabled()) {
			DbTestCase.logger.debug(String.format("created test result: %s.", result));
		}

		return result;
	}

	private static QueuedDbResult readData(final ResultSet rs) throws SQLException {
		final QueuedDbResult result = new QueuedDbResult();

		final ResultSetMetaData rsMetaData = rs.getMetaData();

		final Map<String, String> columns = new HashedMap<>();
		for (int itr = 1; itr <= rsMetaData.getColumnCount(); itr++) {
			columns.put(rsMetaData.getColumnName(itr), Convert.EMPTY_STRING);
		}

		while (rs.next()) { // will traverse through all rows

			final Map<String, String> row = new HashedMap<>();

			for (final String column : columns.keySet()) {

				final Object ob = rs.getObject(column);
				String value = Convert.EMPTY_STRING;
				if (ob == null) {
					row.put(column, value);
					continue;
				}

				if (Integer.class.equals(ob.getClass())) {
					value = Integer.toString(rs.getInt(column));
				} else if (Double.class.equals(ob.getClass())) {
					value = Double.toString(rs.getDouble(column));
				} else if (Boolean.class.equals(ob.getClass())) {
					value = Boolean.toString(rs.getBoolean(column));
				} else if (Float.class.equals(ob.getClass())) {
					value = Float.toString(rs.getFloat(column));
				} else if (Long.class.equals(ob.getClass())) {
					value = Long.toString(rs.getLong(column));
				} else if (String.class.equals(ob.getClass())) {
					value = rs.getString(column);
				} else if (UUID.class.equals(ob.getClass())) {
					final UUID uuid = rs.getObject(column, UUID.class);
					value = uuid.toString();
				} else if (Timestamp.class.equals(ob.getClass())) {
					final Timestamp timestamp = rs.getObject(column, Timestamp.class);
					value = timestamp.toString();
				} else {
					throw new IllegalArgumentException(
							String.format("Cannot handle type: '%s', value '%s'.", ob.getClass(), value));
				}

				row.put(column, value);

			}

			result.add(row);
		}

		return result;
	}

	private static QueuedDbResult runSelectStatement(final Connection connection, final Statement statement)
			throws Exception {

		if (connection == null) {
			throw new SystemException(String.format("connection can't be null."));
		}

		if (connection.isClosed()) {
			throw new SystemException(String.format("connection to database is closed."));
		}

		try (final PreparedStatement selectStatement = connection.prepareStatement(statement.getExpression());
				final ResultSet rs = selectStatement.executeQuery();) {
			return DbTestCase.readData(rs);
		} catch (final Exception ex) {
			final String msg = String.format("can't parse response from statement '%s'.", statement);
			DbTestCase.logger.error(msg, ex);
			throw new SystemException(msg);
		}
	}

	private static QueuedDbResult runQueryStatement(final Connection connection, final Statement statement)
			throws Exception {

		if (connection == null) {
			throw new SystemException(String.format("connection can't be null."));
		}

		if (connection.isClosed()) {
			throw new SystemException(String.format("connection to database is closed."));
		}

		final QueuedDbResult result = new QueuedDbResult();

		try (final PreparedStatement queryStatement = connection.prepareStatement(statement.getExpression())) {

			final int limit = queryStatement.executeUpdate();

			for (int itr = 0; itr < limit; itr += 1) {
				result.add(new HashedMap<>());
			}
			return result;
		} catch (final Exception ex) {
			final String msg = String.format("can't parse response from statement '%s'.", statement);
			DbTestCase.logger.error(msg, ex);
			throw new SystemException(msg);
		}
	}

	@Override
	protected Map<String, String> validateAssertions(final QueuedDbResult dbresult, final List<Assertion> assertions)
			throws SystemException {

		if (dbresult == null) {
			throw new IllegalArgumentException("dbresult can't be null.");
		}

		@SuppressWarnings("rawtypes")
		final IDbResult dbResult = dbresult;

		if (Convert.isEmpty(assertions)) {
			throw new IllegalArgumentException("assertions can't be null or empty.");
		}

		final Map<String, String> result = new HashMap<>();

		if (DbTestCase.logger.isDebugEnabled()) {
			DbTestCase.logger.debug("run assertions");
		}

		final DbResultsEnum resultType = dbResult.getDbResultsEnum();

		switch (resultType) {

		case SELECTED:

			for (final Assertion assertion : assertions) {
				if (DbTestCase.logger.isDebugEnabled()) {
					DbTestCase.logger.debug(String.format("work with assertion: '%s'.", assertion));
				}
				assertion.validate();

				final ValidateMethodEnum method = assertion.getValidateMethod();

				if (!(dbResult instanceof QueuedDbResult)) {
					throw new SystemException(String.format("dbResult needs to be an instance of '%s', but is '%s'.",
							QueuedDbResult.class, dbResult.getClass()));
				}

				final QueuedDbResult queriedDbResult = (QueuedDbResult) dbResult;

				final Map<String, String> results;
				switch (method) {
				case DB_RESULT:
					results = DbTestCase.checkDbResult(queriedDbResult, assertion);
					break;

				default:
					throw new IllegalArgumentException(
							String.format("The validateMethod '%s' is not implemented yet.", method.getValue()));
				}

				results.keySet().stream().forEach(key -> {
					result.put(key, results.get(key));
				});
			}

			break;

		default:
			throw new IllegalArgumentException(
					String.format("The validateMethod '%s' is not implemented yet.", resultType.getValue()));
		}

		return result;
	}

	private static void validateExpectedRows(final QueuedDbResult debresult, final Statement statement)
			throws SystemException {

		if (debresult == null) {
			throw new IllegalArgumentException("debresult can't be null.");
		}

		if (!debresult.validate()) {
			throw new IllegalArgumentException("debresult is invalid.");
		}

		final int expectedRowsAmount = statement.getExpectedRows();

		if (expectedRowsAmount < 0) {
			if (DbTestCase.logger.isDebugEnabled()) {
				DbTestCase.logger.debug(
						String.format("expectedRowsAmount \"%s\" is less than zero, no validation is neccessary.",
								Integer.toString(expectedRowsAmount)));
			}
			return;
		}

		final DbResultRow rows = debresult.getResult();
		if ((rows.size() != expectedRowsAmount)) {
			throw new SystemException(String.format("debresult only has '%s' rows, but expected are '%s'.",
					Integer.toString(rows.size()), Integer.toString(expectedRowsAmount)));
		}
	}

	@Override
	protected void getNextTeststep() throws SystemException {

		final TeststepProvider<DbTeststep> provider = getProvider();

		if (provider == null) {
			throw new IllegalArgumentException("provider can't be null.");
		}

		DbTeststep dbteststep = provider.get();
		if (dbteststep == null) {
			return;
		}

		if (!dbteststep.validate()) {
			throw new IllegalArgumentException(String.format("Step '%s' is invalid.", dbteststep));
		}

		if (getExtractedValues() == null) {
			throw new IllegalStateException("extractedValues can't be null.");
		}

		if (DbTestCase.logger.isDebugEnabled()) {
			DbTestCase.logger.debug(String.format("next dbteststep '%s'.", dbteststep));
		}

		if (!getExtractedValues().keySet().isEmpty()) {
			dbteststep = updateTeststep(dbteststep, getExtractedValues());
		}

		currentstepname = dbteststep.getName();
		currentStatement = dbteststep.getStatement();

		currentAssertions = dbteststep.getAssertions();
	}

	@Override
	public List<RewriteUrlObject> getRewriteUrls() {

		if (DbTestCase.logger.isDebugEnabled()) {
			DbTestCase.logger.debug(String.format("url rewriting is not supported on '%s', will return empty list.",
					DbTestCase.class.toString()));
		}

		return new ArrayList<>();
	}

	@Override
	public BrowserMobProxyServer getProxy() {

		if (DbTestCase.logger.isDebugEnabled()) {
			DbTestCase.logger.debug(String.format("proxying connections is not supported on '%s', will return null.",
					DbTestCase.class.toString()));
		}

		return null;
	}

	@Override
	public void bootstrap() throws Exception {
		databaseconnectionimpl.startConnectionPool();
	}

	@Override
	public void shutdown() throws Exception {
		databaseconnectionimpl.shutdownConnectionPool();
	}

	@Override
	public CsvFile createArtefact() {

		if (currentResult == null) {
			return new CsvFile(UtilsCollection.toArray(String.class, Arrays.asList()));
		}

		CsvFile result = null;

		for (final Map<String, String> row : currentResult.getResult()) {

			if (Convert.isEmpty(row)) {
				continue;
			}

			if (result == null) {
				result = new CsvFile(UtilsCollection.toArray(String.class, row.keySet()));
			}

			if (!result.addRow(row)) {
				DbTestCase.logger.error("can't set up artefact, will return null.");
			}

		}

		// return at least an "empty csv file"
		if (result == null) {
			result = new CsvFile(UtilsCollection.toArray(String.class, Arrays.asList()));
		}

		return result;
	}

	protected Assertion[] getCurrentAssertions() {
		return currentAssertions;
	}
}