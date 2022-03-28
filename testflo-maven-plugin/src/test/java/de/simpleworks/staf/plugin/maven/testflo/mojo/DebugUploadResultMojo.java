package de.simpleworks.staf.plugin.maven.testflo.mojo;

import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.beust.jcommander.JCommander;

import de.simpleworks.staf.plugin.maven.testflo.debug.DebugArgsUpload;

public class DebugUploadResultMojo extends UploadResultMojo {

	private static final Logger logger = LogManager.getLogger(DebugUploadResultMojo.class);

	protected DebugUploadResultMojo(final String testplanFile, final String reportFile, final URL urlTms,
			final URL jiraUrl) {
		super(testplanFile, reportFile, urlTms, jiraUrl);
	}

	public static void main(final String[] args) throws MojoExecutionException, MojoFailureException {
		DebugUploadResultMojo.logger.info("start..");

		final DebugArgsUpload arguments = new DebugArgsUpload();
		JCommander.newBuilder().addObject(arguments).build().parse(args);

		final DebugUploadResultMojo debug = new DebugUploadResultMojo(arguments.testplanFile, arguments.reportFile,
				arguments.urlTms, arguments.jiraUrl);
		debug.execute();

		DebugUploadResultMojo.logger.info("DONE.");
	}

}
