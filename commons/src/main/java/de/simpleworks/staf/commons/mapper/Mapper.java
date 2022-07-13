package de.simpleworks.staf.commons.mapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jayway.jsonpath.JsonPath;

import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.JSONUtils;
import de.simpleworks.staf.commons.utils.UtilsIO;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

public abstract class Mapper<T> {
	private static final Logger logger = LogManager.getLogger(Mapper.class);
	private static final Charset ENCODING = StandardCharsets.UTF_8;

	private static final String PATH_INSTANCE_FIRST = "$.[0].instance";
	private static final String PATH_INSTANCE_ANY = "$.[*].instance";

	protected abstract GsonBuilder createBuilder();

	protected abstract Class<T> getTypeofGeneric();

	private List<T> readAll(final BufferedReader reader) {
		final Class<T> clazz = getTypeofGeneric();
		final Type type = TypeToken.getParameterized(List.class, clazz).getType();

		final List<T> result = getGson().fromJson(reader, type);
		if (Convert.isEmpty(result)) {
			if (Mapper.logger.isWarnEnabled()) {
				Mapper.logger.warn("result is null.");
				Mapper.logger.warn("result will be transformed to empty list.");
			}

			return new ArrayList<>();
		}

		return result;

	}

	public final List<T> readAll(final File file) throws SystemException {
		if (file == null) {
			throw new IllegalArgumentException("file can't be null.");
		}

		if (!file.exists()) {
			throw new IllegalArgumentException(
					String.format("The file at '%s' does not exist.", file.getAbsolutePath()));
		}

		try (BufferedReader reader = UtilsIO.createReader(file, Mapper.ENCODING);) {
			return readAll(reader);
		} catch (final Exception ex) {
			final String message = String.format("can't read data from file '%s'.", file.getAbsolutePath());
			Mapper.logger.error(message, ex);
			throw new SystemException(message);
		}
	}

	public final List<T> read(final String resource) throws SystemException {
		if (Convert.isEmpty(resource)) {
			throw new IllegalArgumentException("resource can't be null or empty string.");
		}

		try (BufferedReader reader = UtilsIO.createReader(resource, Mapper.ENCODING);) {
			return readAll(reader);
		} catch (final Exception ex) {
			final String message = String.format("can't read data from resource '%s'.", resource);
			Mapper.logger.error(message, ex);
			throw new SystemException(message);
		}
	}

	public final List<T> readAll(String jsonstring) throws SystemException {
		if (Convert.isEmpty(jsonstring)) {
			throw new IllegalArgumentException("jsonstring can't be null or empty string.");
		}

		if (!JSONUtils.isJSONArray(jsonstring)) {
			jsonstring = String.format("[%s]", jsonstring);
		}

		List<T> result = new ArrayList<>();

		try {
			final Class<T> clazz = getTypeofGeneric();
			final Type type = TypeToken.getParameterized(List.class, clazz).getType();

			result = getGson().fromJson(jsonstring, type);
		} catch (final Exception ex) {
			final String message = String.format("can't read data from jsonstring '%s'.", jsonstring);
			Mapper.logger.error(message, ex);
			throw new SystemException(message);
		}

		return result;
	}

	public final String read(final T element) throws SystemException {
		if (element == null) {
			throw new IllegalArgumentException("element can't be null.");
		}

		String result = Convert.EMPTY_STRING;

		try {
			final Class<T> clazz = getTypeofGeneric();
			result = getGson().toJson(element, clazz);
		} catch (final Exception ex) {
			final String message = String.format("can't read data from element '%s'.", element);
			Mapper.logger.error(message, ex);
			throw new SystemException(message);
		}

		return result;
	}

	public final String getJsonString(final T element) throws SystemException {
		if (element == null) {
			throw new IllegalArgumentException("element can't be null.");
		}

		final String readElement = read(element);
		final JSONArray array = JsonPath.read(readElement, Mapper.PATH_INSTANCE_FIRST);
		if (array == null) {
			throw new SystemException(String.format("can't find element(s) for path '%s' in json: '%s'.",
					Mapper.PATH_INSTANCE_FIRST, readElement));
		}

		return array.toString();
	}

	public final static String getJsonString(final String element) throws SystemException {
		if (Convert.isEmpty(element)) {
			throw new IllegalArgumentException("element can't be null or empty.");
		}

		final LinkedHashMap<String, String> map = JsonPath.read(element, Mapper.PATH_INSTANCE_FIRST);
		if (map == null) {
			throw new SystemException(String.format("can't find element(s) for path '%s' in json: '%s'.",
					Mapper.PATH_INSTANCE_FIRST, element));
		}

		final JSONObject json = new JSONObject();

		map.keySet().forEach(key -> json.appendField(key, map.get(key)));

		return json.toJSONString();
	}

	public final String readAll(final List<T> list) throws SystemException {
		if (Convert.isEmpty(list)) {
			throw new IllegalArgumentException("list can't be null or empty string.");
		}

		final String result;
		try {
			final Class<T> clazz = getTypeofGeneric();
			final Type type = TypeToken.getParameterized(List.class, clazz).getType();

			result = getGson().toJson(list, type);
		} catch (final Exception ex) {
			final String message = String.format("can't read data from list '%s'.", list);
			Mapper.logger.error(message, ex);
			throw new SystemException(message);
		}

		return result;
	}

	/**
	 * @note: this method should only be applied to pojo, that has been deserialized
	 *        by an instance of the Mapper class!
	 */
	public final String getAllJsonString(final List<T> list) throws SystemException {
		if (Convert.isEmpty(list)) {
			throw new IllegalArgumentException("list can't be null or empty string.");
		}

		final String readElement = readAll(list);

		final JSONArray array = JsonPath.read(readElement, Mapper.PATH_INSTANCE_ANY);
		if (array == null) {
			throw new SystemException(String.format("can't find element(s) for path '%s' in json: '%s'.",
					Mapper.PATH_INSTANCE_ANY, readElement));
		}

		return array.toJSONString();
	}

	public final void write(final File file, final List<T> array) throws SystemException {
		if (file == null) {
			throw new IllegalArgumentException("file can't be null.");
		}

		if (Convert.isEmpty(array)) {
			throw new IllegalArgumentException("array can't be null.");
		}

		final String content = getGson().toJson(array);

		try (BufferedWriter writer = UtilsIO.createWriter(file, Mapper.ENCODING, false)) {
			writer.write(content);
		} catch (final IOException ex) {
			final String message = String.format("can't write data to file '%s'.", file.getAbsolutePath());
			Mapper.logger.error(message, ex);
			throw new SystemException(message);
		}
	}

	public final void append(final File file, final List<T> array) throws SystemException {
		if (file == null) {
			throw new IllegalArgumentException("file can't be null.");
		}

		final List<T> result = readAll(file);
		if (!result.addAll(array)) {
			throw new SystemException("can't add elements.");
		}

		final String content = getGson().toJson(array);

		try (BufferedWriter writer = UtilsIO.createWriter(file, Mapper.ENCODING, true)) {
			writer.write(content);
		} catch (final IOException ex) {
			final String message = String.format("can't append data to file '%s'.", file.getAbsolutePath());
			Mapper.logger.error(message, ex);
			throw new SystemException(message);
		}
	}

	private final Gson getGson() {
		final GsonBuilder builder = createBuilder();

		if (builder == null) {
			throw new IllegalArgumentException("builder can't be null.");
		}

		return builder.setPrettyPrinting().disableHtmlEscaping().create();
	}
}
