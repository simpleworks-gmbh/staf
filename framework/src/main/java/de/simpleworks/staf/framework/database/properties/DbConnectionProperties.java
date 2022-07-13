package de.simpleworks.staf.framework.database.properties;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.annotation.Property;
import de.simpleworks.staf.commons.database.connection.DbConnection;
import de.simpleworks.staf.commons.utils.PropertiesReader;
import de.simpleworks.staf.framework.consts.FrameworkConsts;
import de.simpleworks.staf.framework.database.mapper.MapperDbConnection;

public class DbConnectionProperties extends PropertiesReader {
	private static final Logger logger = LogManager.getLogger(DbConnectionProperties.class);

	private static DbConnectionProperties instance = null;

	private final static MapperDbConnection mapper = new MapperDbConnection();

	@Property(value = FrameworkConsts.DATABASE_CONNECTION_POOL_CONFIG_FILE, required = true)
	private String dbConnections;

	public List<DbConnection> getDbConnections() throws Exception {

		File dbConnectionFile = new File(dbConnections);

		if (!dbConnectionFile.exists()) {
			throw new FileNotFoundException(String.format("The file at \"%s\" does not exist.", dbConnections));
		}

		final List<DbConnection> result = mapper.readAll(dbConnectionFile);

		for (DbConnection connection : result) {
			if (!connection.validate()) {
				throw new IllegalArgumentException(String.format("The connection \"%s\" is invalid.", connection));
			}
		}

		return result;
	}

	@Override
	protected Class<?> getClazz() {
		return DbConnectionProperties.class;
	}

	public static final synchronized DbConnectionProperties getInstance() {
		if (DbConnectionProperties.instance == null) {
			if (DbConnectionProperties.logger.isDebugEnabled()) {
				DbConnectionProperties.logger.debug("create instance.");
			}

			DbConnectionProperties.instance = new DbConnectionProperties();
		}

		return DbConnectionProperties.instance;
	}
}
