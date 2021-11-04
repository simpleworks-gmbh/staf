package de.simpleworks.staf.data.utils;

import de.simpleworks.staf.data.exception.InvalidDataConstellationExcpetion;

public abstract class Data {
	public abstract void validate() throws InvalidDataConstellationExcpetion;
}
