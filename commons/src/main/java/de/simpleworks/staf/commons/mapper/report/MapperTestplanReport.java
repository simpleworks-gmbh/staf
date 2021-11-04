package de.simpleworks.staf.commons.mapper.report;

import com.google.gson.GsonBuilder;

import de.simpleworks.staf.commons.mapper.Adapter;
import de.simpleworks.staf.commons.mapper.Mapper;
import de.simpleworks.staf.commons.report.TestcaseReport;
import de.simpleworks.staf.commons.report.TestplanReport;

public class MapperTestplanReport extends Mapper<TestplanReport> {
	@Override
	protected GsonBuilder createBuilder() {
		final GsonBuilder result = new GsonBuilder();

		result.registerTypeAdapter(TestcaseReport.class, new Adapter<TestcaseReport>());
		return result;
	}

	@Override
	protected Class<TestplanReport> getTypeofGeneric() {
		return TestplanReport.class;
	}
}
