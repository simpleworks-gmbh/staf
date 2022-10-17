package de.simpleworks.staf.framework.elements.commons;

import de.simpleworks.staf.commons.report.StepReport;
import de.simpleworks.staf.commons.report.artefact.Artefact;
import net.lightbody.bmp.BrowserMobProxyServer;

public interface ITestCase {
	
	boolean start();
	
    boolean stop(final StepReport stepReport); 
	
	abstract void bootstrap() throws Exception;

	abstract void shutdown() throws Exception;
	
	abstract void executeTestStep() throws Exception;
	
	void writeDownResults();
	
	abstract BrowserMobProxyServer getProxy();	
	
	/**
	 * @brief method that starts the Testcase (manages execution of
	 *        {@code bootstrap})
	 * @return artefact {@code Artefact} of the current test(-step), null if no
	 *         artefact was generated.
	 */
	@SuppressWarnings("rawtypes")
	abstract Artefact createArtefact();
}
