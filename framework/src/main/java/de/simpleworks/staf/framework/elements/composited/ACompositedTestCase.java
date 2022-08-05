package de.simpleworks.staf.framework.elements.composited;
 
import com.google.inject.Module;

import de.simpleworks.staf.framework.enums.TestcaseKindEnum;

public abstract class ACompositedTestCase{
	
	private Module[] modules;

	protected ACompositedTestCase(Module...  modules) {
		this.modules = modules;
	}
	
	public abstract TestcaseKindEnum getTestcasekind();
	
	public Module[] getModules() {
		return modules;
	}
}