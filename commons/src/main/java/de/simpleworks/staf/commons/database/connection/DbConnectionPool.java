package de.simpleworks.staf.commons.database.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.utils.Convert;

public class DbConnectionPool {

	private static final Logger logger = LogManager.getLogger(DbConnectionPool.class);

	private final Map<String, Connection> pool;

	public DbConnectionPool(List<DbConnection> dbconnections) throws InstantiationException {
		pool = new HashedMap<>();

		try {
			setUpConnectionPool(dbconnections);
		} catch (Exception ex) {
			final String msg = "can't instantiate DbConnectionPool.";
			DbConnectionPool.logger.error(msg, ex);
			throw new InstantiationException(msg);
		}
	}

	public Connection getConnection(final String connectionId) {

		if (Convert.isEmpty(connectionId)) {
			throw new IllegalArgumentException("connectionId can't be null or empty String.");
		}

		Connection result = pool.get(connectionId);
		return result;
	}

	public boolean drainPool() {
		for (String dbanme : pool.keySet()) {
			if (!closeConnection(dbanme)) {
				return false;
			}
		}
		return true;
	}

	private boolean closeConnection(final String dbanme) {

		if (Convert.isEmpty(dbanme)) {
			throw new IllegalArgumentException("dbname can't be null or empty String.");
		}

		Connection result = pool.remove(dbanme);
		try {
			result.close();
		} catch (SQLException ex) {
			final String msg = String.format("can't close connection to database \"%s\".", dbanme);
			logger.error(msg, ex);
			return false;
		}

		return true;
	}

	public void setUpConnectionPool(List<DbConnection> dbconnections) throws Exception {

		for (DbConnection dbconnection : dbconnections) {

			final String id = dbconnection.getId();
			final Connection connection = setUpConnection(dbconnection);

			if (pool.containsKey(id)) {
				throw new Exception(String.format("database connection \"%s\" is already configured.", id));
			}

			pool.put(id, connection);
		}
	}

	private static Connection setUpConnection(DbConnection dbconnection) throws Exception {

		if (dbconnection == null) {
			throw new IllegalArgumentException("dbconnection can't be null.");
		}

		if (!dbconnection.validate()) {
			throw new IllegalArgumentException(String.format("dbconnection \"%s\" is invalid.", dbconnection));
		}

		Connection result = createConnection(dbconnection.getConnectionString(), dbconnection.getUsername(),
				dbconnection.getDriver(), dbconnection.getPassword());

		return result;
	}

	private static Connection createConnection(String url, String user, String driver, String password)
			throws Exception {

		Class.forName(driver);

		Connection result = null;

		try {
			result = DriverManager.getConnection(url, user, password);
		} catch (Exception ex) {
			final String msg = String.format("can't create connection to \"%s\".", url);
			DbConnectionPool.logger.error(msg, ex);
			throw new Exception(msg);
		}

		return result;
	}
}
