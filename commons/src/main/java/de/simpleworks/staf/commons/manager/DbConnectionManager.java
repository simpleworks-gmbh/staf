package de.simpleworks.staf.commons.manager;

import com.google.inject.Provider;

import de.simpleworks.staf.commons.database.connection.DbConnectionPool;
import de.simpleworks.staf.commons.exceptions.SystemException;

public interface DbConnectionManager extends Provider<DbConnectionPool> {

	void startConnectionPool() throws SystemException;

	void shutdownConnectionPool() throws SystemException;

	@Override
	DbConnectionPool get();
}