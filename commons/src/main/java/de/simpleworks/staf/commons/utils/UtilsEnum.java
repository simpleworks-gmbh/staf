package de.simpleworks.staf.commons.utils;

import java.util.List;
import java.util.Optional;

public class UtilsEnum {
	private UtilsEnum() {
		throw new IllegalStateException("utility class.");
	}

	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> T getEnum(final Class<?> type, final String value) {
		return Enum.valueOf((Class<T>) type, value);
	}

	public static <Type extends IEnum> Type getEnumByValue(final IEnum type, final String value) {
		if (type == null) {
			throw new IllegalArgumentException("type can't be null.");
		}

		if (Convert.isEmpty(value)) {
			throw new IllegalArgumentException("key can't be null or empty string.");
		}

		@SuppressWarnings("unchecked")
		final List<Type> values = (List<Type>) type.getValues();

		final Optional<Type> optional = values.stream().filter(val -> val.getValue().equals(value)).findAny();

		return optional.isPresent() ? optional.get() : null;
	}

	public static <Type extends IEnum> Type getEnumByKey(final Type type, final String key) {
		if (type == null) {
			throw new IllegalArgumentException("type can't be null.");
		}

		if (Convert.isEmpty(key)) {
			throw new IllegalArgumentException("key can't be null or empty string.");
		}

		return type.getEnumByKey(key);
	}
}
