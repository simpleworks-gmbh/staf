package de.simpleworks.staf.commons.api;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.interfaces.IPojo;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public class RawFileParameter implements IPojo {
	private static final Logger logger = LogManager.getLogger(RawFileParameter.class);

	private String file;

	public RawFileParameter() {
		file = Convert.EMPTY_STRING;
	}

	public String getFile() {
		return file;
	}

	public void setFile(final String file) {
		this.file = file;
	}

	@Override
	public boolean validate() {
		if (RawFileParameter.logger.isDebugEnabled()) {
			RawFileParameter.logger.debug(String.format("validate: '%s'.", toString()));
		}

		boolean result = true;

		if (Convert.isEmpty(file)) {
			RawFileParameter.logger.error("file can't be null or empty string.");
			result = false;
		}

		if (!(new File(file).exists())) {
			RawFileParameter.logger.error(String.format("The file at '%s' does not exist.", file));
			result = false;
		}

		return result;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s].", Convert.getClassName(RawFileParameter.class),
				UtilsFormat.format("file", file));
	}
}
