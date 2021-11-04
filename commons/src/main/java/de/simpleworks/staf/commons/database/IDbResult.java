package de.simpleworks.staf.commons.database;

import de.simpleworks.staf.commons.enums.DbResultsEnum;
import de.simpleworks.staf.commons.interfaces.IPojo;

public interface IDbResult<Type> extends IPojo {

	// get
	public Type getResult();

	// get
	public DbResultsEnum getDbResultsEnum();
}
