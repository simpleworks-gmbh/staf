package de.simpleworks.staf.commons.utils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface IEnum {
	String getName();

	String getValue();

	List<? extends IEnum> getValues();

	default List<String> getKeys() {
		return getValues().stream().map(value -> value.getName()).collect(Collectors.toList());
	}

	default <Type extends IEnum> Type getEnumByKey(final String key) {
		if (Convert.isEmpty(key)) {
			throw new IllegalArgumentException("key can't be null or empty string.");
		}

		@SuppressWarnings("unchecked")
		final List<Type> values = (List<Type>) getValues();
		final Optional<Type> optional = values.stream().filter(value -> value.getName().equals(key)).findAny();
		if (optional.isPresent()) {
			return optional.get();
		}

		return null;
	}

	default <Type extends IEnum> Type getEnumByValue(final String value) {
		if (Convert.isEmpty(value)) {
			throw new IllegalArgumentException("key can't be null or empty string.");
		}

		return UtilsEnum.getEnumByValue(this, value);
	}
}
