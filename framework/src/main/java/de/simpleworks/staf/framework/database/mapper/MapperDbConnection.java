package de.simpleworks.staf.framework.database.mapper;

import com.google.gson.GsonBuilder;

import de.simpleworks.staf.commons.database.connection.DbConnection;
import de.simpleworks.staf.commons.mapper.Adapter;
import de.simpleworks.staf.commons.mapper.Mapper;

public class MapperDbConnection extends Mapper<DbConnection> {
	@Override
	protected GsonBuilder createBuilder() {
		final GsonBuilder result = new GsonBuilder();

		result.registerTypeAdapter(DbConnection.class, new Adapter<DbConnection>());

		return result;
	}

	@Override
	protected Class<DbConnection> getTypeofGeneric() {
		return DbConnection.class;
	}
}
