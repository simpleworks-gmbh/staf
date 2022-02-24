package de.simpleworks.staf.commons.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

public class JSONUtils {

	private static final Logger logger = LogManager.getLogger(JSONUtils.class);

	public static boolean isJSONArray(String jsonString) {

		if (Convert.isEmpty(jsonString)) {
			throw new IllegalArgumentException("jsonString cant't be null or empty string.");
		}

		boolean result = false;

		try {
			Object obj = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE).parse(jsonString);
			if (obj instanceof JSONObject) {
				@SuppressWarnings("unused")
				JSONObject object = (JSONObject) obj;
			} else {
				@SuppressWarnings("unused")
				JSONArray array = (JSONArray) obj;
				result = true;
			}
		} catch (Exception ex) {

			final String msg = String.format("can't check if jsonString is arrays or not, will return false '%s'.",
					jsonString);
			logger.error(msg, ex);
			result = false;
		}

		return result;
	}
}
