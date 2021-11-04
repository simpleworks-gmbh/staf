package de.simpleworks.staf.commons.mapper.elements;

import com.google.gson.GsonBuilder;

import de.simpleworks.staf.commons.elements.TestCase;
import de.simpleworks.staf.commons.elements.TestStep;
import de.simpleworks.staf.commons.mapper.Adapter;
import de.simpleworks.staf.commons.mapper.Mapper;

public class MapperTestcase extends Mapper<TestCase> {

	@Override
	protected GsonBuilder createBuilder() {
		final GsonBuilder result = new GsonBuilder();

		result.registerTypeAdapter(TestStep.class, new Adapter<TestStep>());

		return result;
	}

	@Override
	protected Class<TestCase> getTypeofGeneric() {
		return TestCase.class;
	}

}
