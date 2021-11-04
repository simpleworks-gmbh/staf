package de.simpleworks.staf.commons.api;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.interfaces.IPojo;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public class MultipartFormFileParameter implements IPojo {
	private static final Logger logger = LogManager.getLogger(MultipartFormFileParameter.class);

	private String mimeType;
	private String file;
	private String name;

	public MultipartFormFileParameter() {
		mimeType = Convert.EMPTY_STRING;
		file = Convert.EMPTY_STRING;
		name = Convert.EMPTY_STRING;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(final String mimeType) {
		this.mimeType = mimeType;
	}

	public String getFile() {
		return file;
	}

	public void setFile(final String file) {
		this.file = file;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public boolean validate() {
		if (MultipartFormFileParameter.logger.isDebugEnabled()) {
			MultipartFormFileParameter.logger.debug(String.format("validate: '%s'.", toString()));
		}

		boolean result = true;

		if (Convert.isEmpty(mimeType)) {
			MultipartFormFileParameter.logger.error("mimeType can't be null or empty string.");
			result = false;
		}

		if (Convert.isEmpty(file)) {
			MultipartFormFileParameter.logger.error("name can't be null or empty string.");
			result = false;
		}

		if (!(new File(file).exists())) {
			MultipartFormFileParameter.logger.error(String.format("The file at '%s' does not exist.", file));
			result = false;
		}

		if (Convert.isEmpty(name)) {
			MultipartFormFileParameter.logger.error("name can't be null or empty string.");
			result = false;
		}

		return result;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s, %s].", Convert.getClassName(MultipartFormFileParameter.class),
				UtilsFormat.format("name", name), UtilsFormat.format("file", file),
				UtilsFormat.format("mimeType", mimeType));
	}
}
