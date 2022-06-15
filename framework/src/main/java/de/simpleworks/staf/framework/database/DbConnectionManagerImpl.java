package de.simpleworks.staf.framework.database;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.database.connection.DbConnection;
import de.simpleworks.staf.commons.database.connection.DbConnectionPool;
import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.manager.DbConnectionManager;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.framework.database.properties.DbConnectionProperties;

public class DbConnectionManagerImpl implements DbConnectionManager {

	private static final Logger logger = LogManager.getLogger(DbConnectionManagerImpl.class);
	private static final DbConnectionProperties properties = DbConnectionProperties.getInstance();

	private DbConnectionPool pool = null;
	private boolean running = false;

	private final List<DbConnection> dbconnections;

	public DbConnectionManagerImpl() throws InstantiationException {

		try {

			this.dbconnections = properties.getDbConnections();

			if (Convert.isEmpty(dbconnections)) {
				throw new IllegalArgumentException("dbconnections can't be null or empty.");
			}
		} catch (Exception ex) {
			final String msg = String.format("can't instantiate instance of \"%s\".", DbConnectionManagerImpl.class);
			logger.error(msg, ex);
			throw new InstantiationException(msg);
		}

	}

	@Override
	public DbConnectionPool get() {

		if (!running) {
			DbConnectionManagerImpl.logger.info("DbConnectionPool has not been started yet.");
		}

		if (pool == null) {
			try {
				pool = new DbConnectionPool(dbconnections);
			} catch (Exception ex) {
				DbConnectionManagerImpl.logger.error("can't create DbConnectionPool.", ex);
			}
		}

		return pool;
	}

	@Override
	public void startConnectionPool() throws SystemException {
		if (!running) {
			pool = null;
			if (get() == null) {
				throw new SystemException("can't start connection pool.");
			}
			running = true;
		} else {
			if (DbConnectionManagerImpl.logger.isDebugEnabled()) {
				DbConnectionManagerImpl.logger.debug("connection pool is already running.");
			}
		}
	}

	@Override
	public void shutdownConnectionPool() throws SystemException {
		if (running) {

			if (!pool.drainPool()) {
				DbConnectionManagerImpl.logger.error("can't close all connections from pool.");
			}

			pool = null;
			running = false;
		} else {
			if (DbConnectionManagerImpl.logger.isDebugEnabled()) {
				DbConnectionManagerImpl.logger.debug("connection pool is not running anymore.");
			}
		}
	}

}
