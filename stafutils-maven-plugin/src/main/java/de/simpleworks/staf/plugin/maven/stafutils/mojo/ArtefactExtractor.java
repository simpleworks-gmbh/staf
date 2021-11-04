package de.simpleworks.staf.plugin.maven.stafutils.mojo;

import java.io.File;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import de.simpleworks.staf.commons.mapper.report.MapperTestcaseReport;
import de.simpleworks.staf.commons.report.StepReport;
import de.simpleworks.staf.commons.report.TestcaseReport;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsArtefact;
import de.simpleworks.staf.commons.utils.UtilsDate;
import de.simpleworks.staf.commons.utils.UtilsIO;

@Mojo(name = "extractArtefacts", defaultPhase = LifecyclePhase.INITIALIZE, threadSafe = true)
public class ArtefactExtractor extends AbstractMojo {

	private final static Logger logger = LogManager.getLogger(ArtefactExtractor.class);
	private final static MapperTestcaseReport mapper = new MapperTestcaseReport();

	@Parameter(property = "result", required = true)
	private String result;

	@Parameter(property = "targetDirectory", required = true)
	private String targetDirectory;

	private List<TestcaseReport> reports;

	private void init() throws Exception {

		final File resultFile = new File(result);

		if (!resultFile.exists()) {
			throw new IllegalArgumentException(
					String.format("resultFile at \"%s\" does not exist.", resultFile.getAbsolutePath()));
		}

		reports = ArtefactExtractor.mapper.readAll(resultFile).stream().filter(Objects::nonNull)
				.collect(Collectors.toList());

		if (Convert.isEmpty(reports)) {
			throw new IllegalArgumentException("reports can't be null or empty.");
		}

		for (final TestcaseReport report : reports) {
			if (!report.validate()) {
				if (ArtefactExtractor.logger.isDebugEnabled()) {
					ArtefactExtractor.logger.debug(String.format("The report \"%s\" is invalid.", report));
				}
			}
		}
	}

	private void writeArtefacts() throws Exception {

		for (final TestcaseReport report : reports) {
			for (final StepReport stepreport : report.getSteps()) {
				if (stepreport.getArtefact() == null) {
					if (ArtefactExtractor.logger.isDebugEnabled()) {
						ArtefactExtractor.logger
								.debug(String.format("The stepreport \"%s\" has no artefact.", stepreport));
					}
					continue;
				}

				final File attachement = UtilsArtefact.saveAttachment(stepreport);

				if (attachement == null) {
					if (ArtefactExtractor.logger.isDebugEnabled()) {
						ArtefactExtractor.logger
								.debug(String.format("can't save artefact of stepreport \"%s\".", stepreport));
					}
					continue;
				}

				UtilsIO.copyFile(attachement, Paths.get(targetDirectory, report.getId(), stepreport.getDescription(),
						UtilsDate.getCurrentTimeFormatted(new Date()), attachement.getName()).toFile());
			}
		}
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			// init variables
			init();

			writeArtefacts();

		} catch (final Exception ex) {
			ArtefactExtractor.logger.error(ex);
			throw new MojoExecutionException("can't create Testclass.");
		}
	}
}