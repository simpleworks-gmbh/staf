package de.simpleworks.staf.framework.reporter.properties;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.annotation.Property;
import de.simpleworks.staf.commons.annotation.Property.Default;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.PropertiesReader;
import de.simpleworks.staf.framework.consts.FrameworkConsts;

public class ReporterProperties extends PropertiesReader {
	private static final Logger logger = LogManager.getLogger(ReporterProperties.class);

	private static ReporterProperties instance = null;

	@Default("results")
	@Property(FrameworkConsts.REPORTER_MANAGER_REPORT_DIRECTORY)
	private String reportdirectory;

	@Default("result.json")
	@Property(FrameworkConsts.REPORTER_MANAGER_REPORT_NAME)
	private String reportname;

	@Default("true")
	@Property(FrameworkConsts.REPORTER_MANAGER_OVERRIDE_REPORT)
	private boolean overrideReport;

	public Path getReportdirectory() {
		if (Convert.isEmpty(reportdirectory)) {
			return null;
		}

		return Paths.get(reportdirectory);
	}

	public String getReportname() {
		return reportname;
	}

	public boolean isOverrideReport() {
		return overrideReport;
	}

	@Override
	protected Class<?> getClazz() {
		return ReporterProperties.class;
	}

	public static final synchronized ReporterProperties getInstance() {
		if (ReporterProperties.instance == null) {
			if (ReporterProperties.logger.isDebugEnabled()) {
				ReporterProperties.logger.debug("create instance.");
			}

			ReporterProperties.instance = new ReporterProperties();
		}

		return ReporterProperties.instance;
	}
}
