package de.simpleworks.staf.plugin.maven.testflo.debug;

import java.net.URL;

import com.beust.jcommander.Parameter;

public class DebugArgsFetch {
	@Parameter(names = "--id", required = true)
	public String id;

	@Parameter(names = "--file", required = true)
	public String file;

	@Parameter(names = "--urlTms", required = true)
	public URL urlTms;
}
