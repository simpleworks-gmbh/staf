package de.simpleworks.staf.commons.database.connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.interfaces.IPojo;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public class DbConnection implements IPojo {

	private static final Logger logger = LogManager.getLogger(DbConnection.class);

	private String id;
	private String connectionString;
	private String driver;
	private String username;
	private String password;

	public DbConnection() {
		id = Convert.EMPTY_STRING;
		connectionString = Convert.EMPTY_STRING;
		driver = Convert.EMPTY_STRING;
		username = Convert.EMPTY_STRING;
		password = Convert.EMPTY_STRING;
	}

	@Override
	public boolean validate() {

		if (logger.isTraceEnabled()) {
			logger.trace("validate DbConnection...");
		}

		if (Convert.isEmpty(id)) {
			logger.error("id can't be null or empty String");
			return false;
		}

		if (Convert.isEmpty(connectionString)) {
			logger.error("connectionString can't be null or empty String");
			return false;
		}

		if (Convert.isEmpty(driver)) {
			logger.error("dbName can't be null or empty String");
			return false;
		}

		if (Convert.isEmpty(username)) {
			logger.error("username can't be null or empty String");
			return false;
		}

		if (Convert.isEmpty(password)) {
			logger.error("password can't be null or empty String");
			return false;
		}

		return true;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getConnectionString() {
		return connectionString;
	}

	public void setConnectionString(String connectionString) {
		this.connectionString = connectionString;
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s, %s, %s, %s]", Convert.getClassName(DbConnection.class),
				UtilsFormat.format("id", id), UtilsFormat.format("connectionString", connectionString),
				UtilsFormat.format("driver", driver), UtilsFormat.format("username", username),
				UtilsFormat.format("password", password));
	}
}