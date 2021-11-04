package de.simpleworks.staf.plugin.maven.testflo.mojo;

import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.beust.jcommander.JCommander;

import de.simpleworks.staf.plugin.maven.testflo.debug.DebugArgsFetch;

public class DebugFetchTestPlanMojo extends FetchTestPlanMojo {

	private static final Logger logger = LogManager.getLogger(DebugFetchTestPlanMojo.class);

	public DebugFetchTestPlanMojo(final String testplanId, final String fileName, final URL urlTms) {
		super(testplanId, fileName, urlTms);
	}

	public static void main(final String[] args) throws MojoExecutionException, MojoFailureException {
		DebugFetchTestPlanMojo.logger.info("start..");

		final DebugArgsFetch arguments = new DebugArgsFetch();
		JCommander.newBuilder().addObject(arguments).build().parse(args);

		final DebugFetchTestPlanMojo debug = new DebugFetchTestPlanMojo(arguments.id, arguments.file, arguments.urlTms);
		debug.execute();

		DebugFetchTestPlanMojo.logger.info("DONE.");
	}
}
