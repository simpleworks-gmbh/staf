package de.simpleworks.staf.framework.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays; 
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.simpleworks.staf.framework.elements.commons.ATestCaseImpl;
import de.simpleworks.staf.commons.annotation.Step; 

public class TestCaseUtils {
	private static final Logger logger = LogManager.getLogger(TestCaseUtils.class);

	public TestCaseUtils() {
		throw new IllegalStateException("utility class.");
	}

	/**
	 * @return sorted list of methods, annotated with
	 *         de.simpleworks.commons.annotation.Step
	 */
	public static List<Method> fetchStepMethods(final Class<? extends ATestCaseImpl> testClass) {
		if (testClass == null) {
			throw new IllegalArgumentException("testClass can't be null.");
		}

		final List<Method> methods = TestCaseUtils.collectMethods(testClass);
		final List<Method> result = methods.stream().filter(method -> (method.getAnnotation(Step.class) != null))
				.collect(Collectors.toList());
		result.sort(new MethodComparator());

		return result;
	}

	@SuppressWarnings("rawtypes")
	public static List<Method> collectMethods(final Class forClass) {
		if (forClass == null) {
			throw new IllegalArgumentException("forClass can't be null.");
		}

		final ArrayList<Method> result = new ArrayList<>();
		Class clazz = forClass;

		do {
			if (TestCaseUtils.logger.isTraceEnabled()) {
				TestCaseUtils.logger.trace(String.format("collectMethods from class '%s'.", clazz.getSimpleName()));
			}

			final List<Method> methodsCurrentClass = Arrays.asList(clazz.getDeclaredMethods());

			// Take the methods at the deepest Level first and disregard the methods in the
			// superclasses
			final ArrayList<Method> methodsToConsider = new ArrayList<>();
			for (final Method m : methodsCurrentClass) {
				boolean methodAlreadyOverriden = false;
				for (final Method n : result) {
					if (m.getName().equals(n.getName()) && m.isAnnotationPresent(Step.class)
							&& n.isAnnotationPresent(Step.class)
							&& (m.getAnnotation(Step.class).order() == n.getAnnotation(Step.class).order())
							&& (m.getAnnotation(Step.class).description()
									.equals(n.getAnnotation(Step.class).description()))) {
						methodAlreadyOverriden = true;
						break;
					}
				}

				if (!methodAlreadyOverriden) {
					methodsToConsider.add(m);
				}
			}

			if (TestCaseUtils.logger.isTraceEnabled()) {
				TestCaseUtils.logger.trace(String.format("Adding the following methods '%s'.", String.join(", ",
						methodsToConsider.stream().map(method -> method.getName()).collect(Collectors.toList()))));
			}

			result.addAll(methodsToConsider);
			clazz = clazz.getSuperclass();
		} while ((clazz != null));

		if (TestCaseUtils.logger.isDebugEnabled()) {
			TestCaseUtils.logger.debug(String.format("Collected Methods '%s'.",
					String.join(", ", result.stream().map(method -> method.getName()).collect(Collectors.toList()))));
		}

		return result;
	}
}
