package de.simpleworks.staf.commons.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomClassLoader extends ClassLoader {
	private static final Logger logger = LoggerFactory.getLogger(CustomClassLoader.class);

	private static byte[] loadClassFromFile(final String fileName) throws ClassNotFoundException {
		final File file = new File(fileName);
		if (!file.exists()) {
			throw new ClassNotFoundException(String.format("The file at '%s' does not exist.", fileName));
		}

		if (CustomClassLoader.logger.isTraceEnabled()) {
			CustomClassLoader.logger.trace(String.format("load class from file: '%s'.", file));
		}

		final byte[] result;
		// TODO use buffered stream.
		try (FileInputStream stream = new FileInputStream(file)) {
			final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			int nextValue = 0;
			while ((nextValue = stream.read()) != -1) {
				byteStream.write(nextValue);
			}

			result = byteStream.toByteArray();
		} catch (final Throwable th) {
			final String msg = String.format("can't read data from file: '%s'.", file);
			CustomClassLoader.logger.error(msg, th);
			throw new ClassNotFoundException(msg);
		}

		return result;
	}

	@Override
	public Class<?> findClass(final String name) throws ClassNotFoundException {
		if (Convert.isEmpty(name)) {
			throw new IllegalArgumentException("name can't be null or empty string.");
		}

		final byte[] b = CustomClassLoader.loadClassFromFile(name);

		return defineClass(name, b, 0, b.length);
	}
}
