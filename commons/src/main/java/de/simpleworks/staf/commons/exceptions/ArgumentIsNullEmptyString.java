package de.simpleworks.staf.commons.exceptions;

public class ArgumentIsNullEmptyString extends IllegalArgumentException {
	private static final long serialVersionUID = -2235606358063039269L;

	// TODO use it!
	public ArgumentIsNullEmptyString(final String argument) {
		super(String.format("%s can't be null or empty string.", argument));
	}
}
