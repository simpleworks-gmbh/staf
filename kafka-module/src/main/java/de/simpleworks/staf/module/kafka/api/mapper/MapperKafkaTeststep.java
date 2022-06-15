package de.simpleworks.staf.module.kafka.api.mapper;

import com.google.gson.GsonBuilder;

import de.simpleworks.staf.commons.mapper.Adapter;
import de.simpleworks.staf.commons.mapper.Mapper;
import de.simpleworks.staf.module.kafka.api.IKafkaTeststep;

@SuppressWarnings("rawtypes")
public class MapperKafkaTeststep extends Mapper<IKafkaTeststep> {
	@Override
	public GsonBuilder createBuilder() {
		final GsonBuilder result = new GsonBuilder();

		result.registerTypeAdapter(IKafkaTeststep.class, new Adapter<IKafkaTeststep>());

		return result;
	}

	@Override
	public Class<IKafkaTeststep> getTypeofGeneric() {
		return IKafkaTeststep.class;
	}
}
