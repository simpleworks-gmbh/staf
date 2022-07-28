package de.simpleworks.staf.framework.reporter.json;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.mapper.report.MapperTestcaseReport;
import de.simpleworks.staf.commons.report.TestcaseReport;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.framework.reporter.manager.ReporterManager;
import de.simpleworks.staf.framework.reporter.properties.ReporterProperties;

public class JsonReporter extends ReporterManager {
	private static final Logger logger = LogManager.getLogger(JsonReporter.class);

	private final String FILE_EXTENSION = "json";
	private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH-mm-ss");
	private final MapperTestcaseReport mapper = new MapperTestcaseReport();

	@Override
	public File setUpFilePath(final TestcaseReport report) {
		if (report == null) {
			throw new IllegalArgumentException("report can't be null.");
		}

		if (!report.validate()) {
			throw new IllegalArgumentException(String.format("report '%s' is invalid.", report));
		}

		final String filename = report.getId();
		if (Convert.isEmpty(filename)) {
			throw new IllegalArgumentException("filename can't be null or empty string.");
		}

		final ReporterProperties properties = ReporterProperties.getInstance();

		final Path reportdirectory = properties.getReportdirectory();
		final String reportname = properties.getReportname();

		File result = null;
		if (reportdirectory == null) {
			final Date date = new Date();

			final Calendar cal = Calendar.getInstance();
			cal.setTime(date);

			final String name = String.format("%s-%s-%s", Integer.valueOf(cal.get(Calendar.DAY_OF_MONTH)),
					Integer.valueOf(cal.get(Calendar.MONTH)), Integer.valueOf(cal.get(Calendar.YEAR)));
			result = Paths.get("results", filename, name).toFile();
		} else {
			result = reportdirectory.toFile();
		}

		if (!result.exists()) {
			result.mkdirs();
		}

		if (Convert.isEmpty(reportname)) {
			final Date date = new Date();
			final Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			result = Paths.get(result.getAbsolutePath(),
					String.format("result-%s.%s", simpleDateFormat.format(date), FILE_EXTENSION)).toFile();
		} else {
			result = Paths.get(result.getAbsolutePath(), reportname).toFile();
		}

		return result;
	}

	@Override
	public boolean saveReport(final TestcaseReport report) {

		try {
			final File file = setUpFilePath(report);
			if (!file.exists()) {
				if (JsonReporter.logger.isInfoEnabled()) {
					JsonReporter.logger.info(String.format("save report into file: '%s'.", file));
				}

				mapper.write(file, Arrays.asList(report));
			} 
			else {
				
				if (JsonReporter.logger.isInfoEnabled()) {
					JsonReporter.logger.info(String.format("append report into file: '%s'.", file));
				}
				
				mapper.append(file, Arrays.asList(report));
			}

			return true;
		} catch (final Exception ex) {
			JsonReporter.logger.error("can't save report.", ex);
			// FIXME throw an exception.
			return false;
		}
	}
}
