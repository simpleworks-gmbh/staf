package de.simpleworks.staf.commons.mapper.database;

import com.google.gson.GsonBuilder;

import de.simpleworks.staf.commons.database.DbTeststep;
import de.simpleworks.staf.commons.mapper.Adapter;
import de.simpleworks.staf.commons.mapper.Mapper;

public class MapperDbTeststep extends Mapper<DbTeststep> {
	@Override
	protected GsonBuilder createBuilder() {
		final GsonBuilder result = new GsonBuilder();

		result.registerTypeAdapter(DbTeststep.class, new Adapter<DbTeststep>());

		return result;
	}

	@Override
	protected Class<DbTeststep> getTypeofGeneric() {
		return DbTeststep.class;
	}
}
