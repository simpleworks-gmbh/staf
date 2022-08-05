package de.simpleworks.staf.framework.elements.composited.template;

import com.google.inject.Module;

import de.simpleworks.staf.commons.annotation.Testcase;
import de.simpleworks.staf.commons.exceptions.SystemException; 
import de.simpleworks.staf.framework.elements.database.DbTestCase;

@Testcase(id = "DBTestcaseTemplate")
public class DBTestcaseTemplate extends DbTestCase {

	public DBTestcaseTemplate(final String resource, final Module... modules)
			throws SystemException {
		super(resource, modules);
	}
}