package de.simpleworks.staf.commons.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.simpleworks.staf.commons.utils.PropertiesReader;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Properties {
	Class<? extends PropertiesReader> value();
}