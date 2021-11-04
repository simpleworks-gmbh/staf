package de.simpleworks.staf.commons.mapper.api;

import com.google.gson.GsonBuilder;

import de.simpleworks.staf.commons.api.APITeststep;
import de.simpleworks.staf.commons.mapper.Adapter;
import de.simpleworks.staf.commons.mapper.Mapper;

public class MapperAPITeststep extends Mapper<APITeststep> {
	@Override
	protected GsonBuilder createBuilder() {
		final GsonBuilder result = new GsonBuilder();

		result.registerTypeAdapter(APITeststep.class, new Adapter<APITeststep>());

		return result;
	}

	@Override
	protected Class<APITeststep> getTypeofGeneric() {
		return APITeststep.class;
	}
}
