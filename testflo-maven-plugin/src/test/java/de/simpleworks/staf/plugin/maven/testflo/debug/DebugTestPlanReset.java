package de.simpleworks.staf.plugin.maven.testflo.debug;

import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.beust.jcommander.JCommander;

import de.simpleworks.staf.commons.exceptions.SystemException;

public class DebugTestPlanReset extends DebugTestFLOClientMojo {

	private static final Logger logger = LogManager.getLogger(DebugTestPlanReset.class);

	protected DebugTestPlanReset(final String testPlanId, final String fileName, final URL urlTms, final URL jiraUrl) {
		super(testPlanId, fileName, urlTms, jiraUrl);
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (DebugTestPlanReset.logger.isInfoEnabled()) {
			DebugTestPlanReset.logger.info(String.format("reset test plan: '%s'.", testPlanId));
		}

		try {
			clientNG.testPlanReset(testPlanId);
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(final String[] args) throws MojoExecutionException, MojoFailureException, SystemException {
		DebugTestPlanReset.logger.info("start..");

		final DebugArgsFetch arguments = new DebugArgsFetch();
		JCommander.newBuilder().addObject(arguments).build().parse(args);

		new DebugTestPlanReset(arguments.id, arguments.file, arguments.urlTms, arguments.jiraUrl).execute();

		DebugTestPlanReset.logger.info("DONE.");
	}
}
