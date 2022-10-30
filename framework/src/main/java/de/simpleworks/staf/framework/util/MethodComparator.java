package de.simpleworks.staf.framework.util;

import java.lang.reflect.Method;
import java.util.Comparator;

import de.simpleworks.staf.commons.annotation.Step;

public class MethodComparator implements Comparator<Method> {
	@Override
	public int compare(final Method method1, final Method method2) {
		if (method1.getAnnotation(Step.class) == null) {
			throw new RuntimeException(String.format("Method '%s' does not have the annotation '%s'.",
					method1.getName(), Step.class.getName()));
		}

		if (method2.getAnnotation(Step.class) == null) {
			throw new RuntimeException(String.format("Method '%s' does not have the annotation '%s'.",
					method2.getName(), Step.class.getName()));
		}

		if (method1.getAnnotation(Step.class).order() > method2.getAnnotation(Step.class).order()) {
			return 1;
		} else if (method1.getAnnotation(Step.class).order() < method2.getAnnotation(Step.class).order()) {
			return -1;
		} else {
			throw new RuntimeException(String.format(
					"Method '%s' and Method '%s' share the same order '%s', which is not provided.", method1.getName(),
					method2.getName(), Integer.toString(method1.getAnnotation(Step.class).order())));
		}
	}
}