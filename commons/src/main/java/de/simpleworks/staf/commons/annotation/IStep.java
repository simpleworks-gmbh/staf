package de.simpleworks.staf.commons.annotation;

import de.simpleworks.staf.commons.exceptions.SystemException;

public interface IStep {
	void assertStep() throws SystemException;
}
