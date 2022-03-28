package de.simpleworks.staf.plugin.maven.testflo.debug;

import java.io.File;
import java.net.URL;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.beust.jcommander.JCommander;

import de.simpleworks.staf.commons.elements.TestPlan;
import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.mapper.elements.MapperTestplan;
import de.simpleworks.staf.commons.utils.UtilsIO;

public class DebugTestPlanStart extends DebugTestFLOClientMojo {

	private static final Logger logger = LogManager.getLogger(DebugTestPlanStart.class);

	protected DebugTestPlanStart(final String testPlanId, final String fileName, final URL urlTms, final URL jiraURL) {
		super(testPlanId, fileName, urlTms, jiraURL);
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (DebugTestPlanStart.logger.isInfoEnabled()) {
			DebugTestPlanStart.logger.info(String.format("start test plan: '%s'.", testPlanId));
		}

		try {
			clientNG.moveTestPlanToNextIteration(testPlanId);

			final TestPlan testPlan = clientNG.readTestPlan(testPlanId);

			clientNG.startTestPlan(testPlan);

			final File file = new File(fileName);
			if (DebugTestPlanStart.logger.isInfoEnabled()) {
				DebugTestPlanStart.logger.info(String.format("write test plan into file: '%s'.", file));
			}

			UtilsIO.deleteFile(file);
			new MapperTestplan().write(file, Arrays.asList(testPlan));
		} catch (final SystemException ex) {
			final String message = String.format("can't start test plan: '%s'.", testPlanId);
			DebugTestPlanStart.logger.error(message, ex);
			throw new MojoExecutionException(message);
		}
	}

	public static void main(final String[] args) throws MojoExecutionException, MojoFailureException {
		DebugTestPlanStart.logger.info("start..");

		final DebugArgsFetch arguments = new DebugArgsFetch();
		JCommander.newBuilder().addObject(arguments).build().parse(args);

		new DebugTestPlanStart(arguments.id, arguments.file, arguments.urlTms, arguments.jiraUrl).execute();

		DebugTestPlanStart.logger.info("DONE.");
	}
}
