package de.simpleworks.staf.framework.reporter.manager;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.report.TestcaseReport;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.framework.reporter.properties.ReporterProperties;

public abstract class ReporterManager {
	
	private static final Logger logger = LogManager.getLogger(ReporterManager.class);

	final ReporterProperties properties = ReporterProperties.getInstance();
	
	/**
	 * @param TestcaseReport report, to be safed
	 * @return true is {@param report} was safed successfully.
	 */
	public abstract boolean saveReport(TestcaseReport report);

	/**
	 * @param TestcaseReport report, to be safed
	 * @return file to save the report to.
	 */
	public abstract File setUpFilePath(TestcaseReport report);
	
	
	public ReporterManager() {
		
		if (properties.isOverrideReport()) {
			
			final Path reportdirectory = properties.getReportdirectory();
			final String reportname = properties.getReportname();

			File result = null;
			if (reportdirectory != null && (!Convert.isEmpty(reportname))) {
				result = Paths.get(reportdirectory.toFile().getAbsolutePath(), reportname).toFile();
				
				if(result.exists()) {
					if(result.delete()) {
						ReporterManager.logger.error(String.format("can't delete results file '%s'.", result.getAbsolutePath()));
					}
				}
				
			}	
		} 
		
	}
}
