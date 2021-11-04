package de.simpleworks.staf.data.exception;

import de.simpleworks.staf.commons.exceptions.SystemException;

public class InvalidDataConstellationExcpetion extends SystemException {
	private static final long serialVersionUID = -6787952111323776689L;

	public InvalidDataConstellationExcpetion(final String message) {
		super(message);
	}
}
