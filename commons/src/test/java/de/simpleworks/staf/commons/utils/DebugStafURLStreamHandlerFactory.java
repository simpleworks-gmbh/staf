package de.simpleworks.staf.commons.utils;

import java.io.InputStream;
import java.net.URL;

import de.simpleworks.staf.commons.utils.io.StafURLStreamHandlerClasspath;
import de.simpleworks.staf.commons.utils.io.StafURLStreamHandlerFactory;

public class DebugStafURLStreamHandlerFactory {

	public static void openInputStream(final String resource) throws Exception {
		System.out.println(String.format("open '%s' with getResourceAsStream().", resource));

		try (final InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);) {
			if (input == null) {
				System.out.println("input is null.");
			} else {
				System.out.println("input is NOT null.");
			}
		}

		System.out.println(String.format("open '%s' with URL.openStream().", resource));
		final URL url = new URL(resource);
		try (final InputStream input = url.openStream();) {
			if (input == null) {
				System.out.println("input is null.");
			} else {
				System.out.println("input is NOT null.");
			}
		}
	}

	public static void main(final String[] args) throws Exception {
		System.out.println("start..");

		final StafURLStreamHandlerFactory factory = new StafURLStreamHandlerFactory();
		factory.addHandler(new StafURLStreamHandlerClasspath());
		URL.setURLStreamHandlerFactory(factory);

		DebugStafURLStreamHandlerFactory.openInputStream("classpath:test.properties");
		DebugStafURLStreamHandlerFactory.openInputStream("file:./src/test/resources/test.properties");

		System.out.println("DONE.");
	}
}
