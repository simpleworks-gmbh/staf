package de.simpleworks.staf.commons.utils;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.simpleworks.staf.commons.elements.TestPlan;
import de.simpleworks.staf.commons.exceptions.SystemException;

public class UtilsTestplan {
	private static final Logger logger = LogManager.getLogger(UtilsTestplan.class);

	/**
	 * @return null, if file has not been read correctly, otherwise if not.
	 */
	public static TestPlan readTestplan(final File file) {
		if (file == null) {
			throw new IllegalArgumentException("file can't be null.");
		}

		if (!file.exists()) {
			throw new IllegalArgumentException(String.format("file does not exist at '%s'.", file.getAbsolutePath()));
		}

		TestPlan result = null;

		final Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			result = gson.fromJson(UtilsIO.getAllContentFromFile(file), TestPlan.class);
		} catch (final SystemException e) {
			UtilsTestplan.logger.error(String.format("can't read test plan from file: '%s'.", file), e);
			// FIXME throw an exception.
		}

		return result;
	}

	/**
	 * @return true, if file has been written correctly, false if not.
	 */
	public static boolean writeTestplan(final File file, final TestPlan testplan) {
		if (testplan == null) {
			throw new IllegalArgumentException("file can't be null.");
		}

		if (!file.exists()) {
			throw new IllegalArgumentException(String.format("file does not exist at %s", file.getAbsolutePath()));
		}

		final boolean result = false;

		// TODO why do we need builder here?
		final GsonBuilder builder = new GsonBuilder();
		builder.serializeNulls();

		final Gson gson = new GsonBuilder().setPrettyPrinting().create();
		final String json = gson.toJson(testplan);

		try {
			if (!file.exists()) {
				UtilsIO.createFile(file);
			}

			UtilsIO.putAllContentToFile(file, json);
		} catch (final SystemException e) {
			UtilsTestplan.logger.error(String.format("can't write test plan '%s' into file: '%s'.", testplan, file), e);
			// FIXME throw an exception.
		}

		return result;
	}

}
