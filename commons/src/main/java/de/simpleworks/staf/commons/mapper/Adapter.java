package de.simpleworks.staf.commons.mapper;

import java.lang.reflect.Type;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import de.simpleworks.staf.commons.consts.AdapterConst;

public class Adapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {
	private static final Logger logger = LogManager.getLogger(Adapter.class);

	@Override
	public JsonElement serialize(final T src, final Type typeOfSrc, final JsonSerializationContext context) {
		final JsonObject result = new JsonObject();

		result.addProperty(AdapterConst.CLASSNAME, src.getClass().getName());
		final JsonElement element = new Gson().toJsonTree(src, src.getClass());
		result.add(AdapterConst.INSTANCE, element);

		return result;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
			throws JsonParseException {

		if (json == null) {
			throw new IllegalArgumentException("json can't be null.");
		}

		if (typeOfT == null) {
			throw new IllegalArgumentException("typeOfT can't be null.");
		}

		if (context == null) {
			throw new IllegalArgumentException("context can't be null.");
		}

		final JsonObject jsonObject = json.getAsJsonObject();
		final JsonPrimitive prim = (JsonPrimitive) jsonObject.get(AdapterConst.CLASSNAME);
		if (prim == null) {
			throw new JsonParseException(
					String.format("json for type: '%s' without '%s' : '%s'.", typeOfT, AdapterConst.CLASSNAME, json));
		}

		final String className = prim.getAsString();
		Class<?> instance = null;

		try {
			instance = Class.forName(className);
		} catch (final ClassNotFoundException ex) {
			final String message = String.format("can't create instance for class: '%s'.", className);
			Adapter.logger.error(message, ex);
			throw new JsonParseException(message);
		}

		final JsonElement element = jsonObject.get(AdapterConst.INSTANCE);
		final Gson gson = new Gson();

		return (T) gson.fromJson(element, instance);
	}

}
