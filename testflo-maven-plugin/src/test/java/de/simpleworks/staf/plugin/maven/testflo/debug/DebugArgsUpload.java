package de.simpleworks.staf.plugin.maven.testflo.debug;

import java.net.URL;

import com.beust.jcommander.Parameter;

public class DebugArgsUpload {
	@Parameter(names = "--reportFile", required = true)
	public String reportFile;

	@Parameter(names = "--testplanFile", required = true)
	public String testplanFile;

	@Parameter(names = "--urlTms", required = true)
	public URL urlTms;
}
