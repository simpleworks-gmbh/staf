package de.simpleworks.staf.commons.mapper.report;

import com.google.gson.GsonBuilder;

import de.simpleworks.staf.commons.mapper.Adapter;
import de.simpleworks.staf.commons.mapper.Mapper;
import de.simpleworks.staf.commons.report.TestcaseReport;
import de.simpleworks.staf.commons.report.artefact.Artefact;

public class MapperTestcaseReport extends Mapper<TestcaseReport> {
	@SuppressWarnings("rawtypes")
	@Override
	protected GsonBuilder createBuilder() {
		final GsonBuilder result = new GsonBuilder();

		result.registerTypeAdapter(Artefact.class, new Adapter<Artefact>());

		return result;
	}

	@Override
	protected Class<TestcaseReport> getTypeofGeneric() {
		return TestcaseReport.class;
	}
}
