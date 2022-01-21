package de.simpleworks.staf.framework.elements.commons;

public abstract class ATestResult {

	private boolean successfull;
	private String errormessage;

	public boolean isSuccessfull() {
		return successfull;
	}

	public void setSuccessfull(final boolean successfull) {
		this.successfull = successfull;
	}

	public String getErrormessage() {
		return errormessage;
	}

	public void setErrormessage(final String errormessage) {
		this.errormessage = errormessage;
	}
}
