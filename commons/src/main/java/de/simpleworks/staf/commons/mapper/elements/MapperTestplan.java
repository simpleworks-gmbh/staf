package de.simpleworks.staf.commons.mapper.elements;

import com.google.gson.GsonBuilder;

import de.simpleworks.staf.commons.elements.TestCase;
import de.simpleworks.staf.commons.elements.TestPlan;
import de.simpleworks.staf.commons.elements.TestStep;
import de.simpleworks.staf.commons.mapper.Adapter;
import de.simpleworks.staf.commons.mapper.Mapper;

public class MapperTestplan extends Mapper<TestPlan> {

	@Override
	protected GsonBuilder createBuilder() {
		final GsonBuilder result = new GsonBuilder();

		result.registerTypeAdapter(TestCase.class, new Adapter<TestCase>());
		result.registerTypeAdapter(TestStep.class, new Adapter<TestStep>());

		return result;
	}

	@Override
	protected Class<TestPlan> getTypeofGeneric() {
		return TestPlan.class;
	}
}
