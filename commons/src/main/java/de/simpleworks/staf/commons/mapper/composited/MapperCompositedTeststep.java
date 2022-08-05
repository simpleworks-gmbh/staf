package de.simpleworks.staf.commons.mapper.composited;

import com.google.gson.GsonBuilder;

import de.simpleworks.staf.commons.api.APITeststep;
import de.simpleworks.staf.commons.composited.CompositedTeststep;
import de.simpleworks.staf.commons.database.DbTeststep;
import de.simpleworks.staf.commons.mapper.Adapter;
import de.simpleworks.staf.commons.mapper.Mapper;

public class MapperCompositedTeststep extends Mapper<CompositedTeststep> {
	
	@Override
	protected GsonBuilder createBuilder() {
		final GsonBuilder result = new GsonBuilder();

		result.registerTypeAdapter(CompositedTeststep.class, new Adapter<CompositedTeststep>());

		result.registerTypeAdapter(APITeststep.class, new Adapter<APITeststep>());
		result.registerTypeAdapter(DbTeststep.class, new Adapter<DbTeststep>());
		
		return result;
	}

	@Override
	protected Class<CompositedTeststep> getTypeofGeneric() {
		return CompositedTeststep.class;
	}
}
