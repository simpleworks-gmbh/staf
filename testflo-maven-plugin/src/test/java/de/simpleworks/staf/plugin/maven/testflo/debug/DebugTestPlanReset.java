package de.simpleworks.staf.plugin.maven.testflo.debug;

import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.beust.jcommander.JCommander;

public class DebugTestPlanReset extends DebugTestFLOClientMojo {

	private static final Logger logger = LogManager.getLogger(DebugTestPlanReset.class);

	protected DebugTestPlanReset(final String testPlanId, final String fileName, final URL urlTms) {
		super(testPlanId, fileName, urlTms);
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (DebugTestPlanReset.logger.isInfoEnabled()) {
			DebugTestPlanReset.logger.info(String.format("reset test plan: '%s'.", testPlanId));
		}

		clientNG.testPlanReset(testPlanId);
	}

	public static void main(final String[] args) throws MojoExecutionException, MojoFailureException {
		DebugTestPlanReset.logger.info("start..");

		final DebugArgsFetch arguments = new DebugArgsFetch();
		JCommander.newBuilder().addObject(arguments).build().parse(args);

		new DebugTestPlanReset(arguments.id, arguments.file, arguments.urlTms).execute();

		DebugTestPlanReset.logger.info("DONE.");
	}
}
