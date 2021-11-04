package de.simpleworks.staf.commons.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Property {
	String value();

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface NotNull {
		// nothing to define.
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface Default {
		String value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface ClassPath {
		// nothing to define.
	}
}
