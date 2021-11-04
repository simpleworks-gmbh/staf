package de.simpleworks.staf.plugin.maven.xray.mapper;

import com.google.gson.GsonBuilder;

import de.simpleworks.staf.commons.mapper.Adapter;
import de.simpleworks.staf.commons.mapper.Mapper;
import de.simpleworks.staf.plugin.maven.xray.elements.XrayResult;

public class MapperXRayResult extends Mapper<XrayResult> {

	@Override
	protected GsonBuilder createBuilder() {
		final GsonBuilder result = new GsonBuilder();

		result.registerTypeAdapter(XrayResult.class, new Adapter<XrayResult>());
		return result;
	}

	@Override
	protected Class<XrayResult> getTypeofGeneric() {
		return XrayResult.class;
	}

}
