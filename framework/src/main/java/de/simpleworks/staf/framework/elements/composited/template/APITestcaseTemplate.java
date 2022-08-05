package de.simpleworks.staf.framework.elements.composited.template;

import de.simpleworks.staf.commons.annotation.Testcase;
import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.framework.elements.api.APITestCase;
import de.simpleworks.staf.framework.elements.api.RewriteUrlObject;
import com.google.inject.Module;

@Testcase(id = "APITestcaseTemplate")
public class APITestcaseTemplate extends APITestCase {

	public APITestcaseTemplate(final String resource, final Module... modules)
			throws SystemException {
		super(resource, modules);
	}


	@Override
	public java.util.List<RewriteUrlObject> getRewriteUrls() {
		// TODO Auto-generated method stub
		return null;
	}

}