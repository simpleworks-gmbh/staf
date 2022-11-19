package de.simpleworks.staf.framework.util.assertion;

import de.simpleworks.staf.commons.database.QueuedDbResult;

public class DbViolatedAssertionException extends RuntimeException{

	private static final long serialVersionUID = 1L;
	
	private final QueuedDbResult result;
	
	public DbViolatedAssertionException(final String mesage, final QueuedDbResult result) {
		super(mesage);
		
		this.result = result;
	}
	
	public QueuedDbResult getResult() {
		return result;
	}
}