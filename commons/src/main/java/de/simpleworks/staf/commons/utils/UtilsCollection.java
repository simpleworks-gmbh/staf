package de.simpleworks.staf.commons.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class UtilsCollection {
	private static final Logger logger = LogManager.getLogger(UtilsCollection.class);

	private UtilsCollection() {
		throw new IllegalStateException("utility class.");
	}

	@SuppressWarnings("rawtypes")
	public static <T> T[] add(final Class clazz, final T[] array, final T element) {
		final List<T> list = UtilsCollection.toList(array);

		list.add(element);

		return UtilsCollection.toArray(clazz, list);
	}

	@SafeVarargs
	@SuppressWarnings("rawtypes")
	public static <T> T[] add(final Class clazz, final T[] array, final T... elements) {
		final List<T> list = UtilsCollection.toList(array);

		for (final T element : UtilsCollection.toList(elements)) {
			list.add(element);
		}

		return UtilsCollection.toArray(clazz, list);
	}

	public static <T> List<T> toList(final T[] array) {
		final List<T> result = new ArrayList<>();

		if (Convert.isEmpty(array)) {
			return result;
		}

		for (final T element : array) {
			result.add(element);
		}

		return result;
	}

	public static <T> List<T> toList(final Collection<T> collection) {
		final List<T> result = new ArrayList<>();

		if (Convert.isEmpty(collection)) {
			if (UtilsCollection.logger.isDebugEnabled()) {
				UtilsCollection.logger.debug(String.format("collection is empty, will return an empty List."));
			}

			return result;
		}

		for (final T element : collection) {
			result.add(element);
		}

		return result;
	}

	public static <T> List<T> toList(final Set<T> set) {
		if (set == null) {
			if (UtilsCollection.logger.isDebugEnabled()) {
				UtilsCollection.logger.debug(String.format("set is null, will return an empty List."));
			}

			return null;
		}

		final Iterator<T> iterator = set.iterator();
		return UtilsCollection.toList(iterator);
	}

	public static <T> List<T> toList(final Iterator<T> iterator) {
		if (iterator == null) {
			throw new IllegalArgumentException("iterator can't be null.");
		}

		final List<T> result = new ArrayList<>();
		iterator.forEachRemaining(result::add);
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T[] toArray(final Class clazz, final List<T> list) {
		if (clazz == null) {
			if (UtilsCollection.logger.isDebugEnabled()) {
				UtilsCollection.logger.debug(String.format("clazz is null, will return an empty array."));
			}

			return null;
		}

		final T[] result = (T[]) Array.newInstance(clazz, list.size());

		if (Convert.isEmpty(list)) {
			return result;
		}

		for (int itr = 0; itr < list.size(); itr += 1) {
			result[itr] = list.get(itr);
		}

		return result;
	}

	@SuppressWarnings("rawtypes")
	public static <T> T[] toArray(final Class clazz, Set<T> keySet) {

		final List<T> list = UtilsCollection.toList(keySet);
		return toArray(clazz, list);
	}

	public static <T> Collection<T> getIntersection(final Collection<T> col1, final Collection<T> col2) {
		if (col1 == null) {
			if (UtilsCollection.logger.isDebugEnabled()) {
				UtilsCollection.logger.debug(String.format("clazz is null, will return an empty array."));
			}

			return new ArrayList<>();
		}

		if (col2 == null) {
			if (UtilsCollection.logger.isDebugEnabled()) {
				UtilsCollection.logger.debug(String.format("clazz is null, will return an empty array."));
			}

			return new ArrayList<>();
		}

		final Collection<T> result = new ArrayList<>();

		for (final T elem : col1) {
			if (col2.contains(elem)) {
				result.add(elem);
			}
		}

		if (UtilsCollection.logger.isDebugEnabled()) {
			if (result.isEmpty()) {
				UtilsCollection.logger
						.debug(String.format("The collections '%s' and '%s' do not share any element in common."));
			}
		}

		return result;
	}

	/**
	 * @brief method will substract all elements, which col1 , does not share with
	 *        col2
	 */
	public static <T> Collection<T> substractCollectonsFromEachOther(final Collection<T> col1,
			final Collection<T> col2) {
		if (Convert.isEmpty(col1)) {
			if (UtilsCollection.logger.isDebugEnabled()) {
				UtilsCollection.logger.debug(String.format("col1 is null, will return an empty array."));
			}

			return new ArrayList<>();
		}

		if (Convert.isEmpty(col2)) {
			if (UtilsCollection.logger.isDebugEnabled()) {
				UtilsCollection.logger.debug(String.format("col2 is null, will return an empty array."));
			}

			return new ArrayList<>();
		}

		final Collection<T> intersectedCollection = UtilsCollection.getIntersection(col1, col2);

		final Collection<T> result = new ArrayList<>();
		for (final T elem : col1) {

			if (!intersectedCollection.contains(elem)) {
				result.add(elem);
			}
		}

		if (UtilsCollection.logger.isDebugEnabled()) {
			if (result.isEmpty()) {
				UtilsCollection.logger
						.debug(String.format("The collections do not share any element in common.\nWill return col1."));
			}
		}

		return result;
	}

}
