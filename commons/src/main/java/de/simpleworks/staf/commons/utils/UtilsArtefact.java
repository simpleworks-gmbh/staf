package de.simpleworks.staf.commons.utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.report.StepReport;
import de.simpleworks.staf.commons.report.artefact.Artefact;

public class UtilsArtefact {

	private static final Logger logger = LogManager.getLogger(UtilsArtefact.class);

	private static File createFile(final StepReport step, final String suffix) throws SystemException {
		final String prefix = String.format("step_%d_", Integer.valueOf(step.getOrder()));
		try {
			final Path tempFile = Files.createTempFile(prefix, suffix);
			final File result = tempFile.toFile();
			result.deleteOnExit();
			return result;
		} catch (final Exception ex) {
			final String msg = String.format("can't create file with prefix '%s' and suffix: '%s'.", prefix, suffix);
			UtilsArtefact.logger.error(msg, ex);
			throw new SystemException(msg);
		}
	}

	public static File saveAttachment(final StepReport step) throws SystemException {
		if (step == null) {
			throw new IllegalArgumentException("step can't be null.");
		}

		@SuppressWarnings("rawtypes")
		final Artefact artefact = step.getArtefact();
		if (artefact == null) {
			if (UtilsArtefact.logger.isDebugEnabled()) {
				UtilsArtefact.logger.debug("artefact is null -> nothing to do.");
			}

			return null;
		}

		final File result;

		switch (artefact.getType()) {
		case SCREENSHOT:
			result = UtilsArtefact.createFile(step, ".png");
			ImageExtractor.createImage(result, (String) step.getArtefact().getArtefact());
			break;

		case HARFILE:
			result = UtilsArtefact.createFile(step, ".har");
			HarFileExtractor.createHarFile(result, (String) step.getArtefact().getArtefact());
			break;
			
		case CSVFILE:
			result = UtilsArtefact.createFile(step, ".csv");
			CsvFileExtractor.createCsvFile(result, (String) step.getArtefact().getArtefact());
			break;

		default:
			UtilsArtefact.logger
					.error(String.format("artefact type '%s' is not implemented yet.", artefact.getType().getValue()));
			result = null;
			break;
		}

		return result;
	}
}
