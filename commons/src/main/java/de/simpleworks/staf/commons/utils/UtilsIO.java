package de.simpleworks.staf.commons.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.utils.io.StafURLStreamHandlerClasspath;
import de.simpleworks.staf.commons.utils.io.StafURLStreamHandlerConfiguration;
import de.simpleworks.staf.commons.utils.io.StafURLStreamHandlerFactory;

public final class UtilsIO {
	private static final Logger logger = LogManager.getLogger(UtilsIO.class);
	private static final int STREAM_BUFFER_SIZE = 128 * 1024;
	private static final Charset ENCODING = StandardCharsets.UTF_8;

	static {
		if (UtilsIO.logger.isDebugEnabled()) {
			UtilsIO.logger.debug(String.format("set url stream handler factory to '%s'.",
					Convert.getClassFullName(StafURLStreamHandlerFactory.class)));
		}

		final StafURLStreamHandlerFactory factory = new StafURLStreamHandlerFactory();
		factory.addHandler(new StafURLStreamHandlerClasspath());
		factory.addHandler(new StafURLStreamHandlerConfiguration());
		URL.setURLStreamHandlerFactory(factory);
	}

	private UtilsIO() {
		throw new IllegalStateException("utility class.");
	}

	public static boolean checkIfFilesAreEqual(final File file1, final File file2) {
		if (file1 == null) {
			throw new IllegalArgumentException("file1 can't be null.");
		}

		if (!file1.exists()) {
			throw new IllegalArgumentException(
					String.format("the file at '%s' does not exist.", file1.getAbsolutePath()));
		}

		if (file2 == null) {
			throw new IllegalArgumentException("file2 can't be null.");
		}

		if (!file2.exists()) {
			throw new IllegalArgumentException(
					String.format("the file at '%s' does not exist.", file2.getAbsolutePath()));
		}

		boolean result = false;

		try {
			result = com.google.common.io.Files.asByteSource(file1)
					.contentEquals(com.google.common.io.Files.asByteSource(file2));
		} catch (final IOException ex) {
			UtilsIO.logger.error(String.format("the files at '%s', and '%s' can't be compared.",
					file1.getAbsolutePath(), file2.getAbsolutePath()), ex);
			result = false;
		}

		return result;
	}

	public static boolean convertBase64StringtoFile(final String base64, final File file) {
		if (Convert.isEmpty(base64)) {
			throw new IllegalArgumentException("base64 can't be null or empty string.");
		}

		if (file == null) {
			throw new IllegalArgumentException("file can't be null.");
		}

		boolean result = false;

		final byte[] data = Base64.getDecoder().decode(base64);

		try (OutputStream stream = new FileOutputStream(file)) {
			stream.write(data);
			result = true;
		} catch (final FileNotFoundException ex) {
			UtilsIO.logger.error(String.format("can't find '%s' on the system.", file.getAbsolutePath()), ex);
		} catch (final IOException ex) {
			UtilsIO.logger.error(String.format("can't write to '%s'.", file.getAbsolutePath()), ex);
		}

		return result;
	}

	public static void createParentDir(final File file) throws SystemException {
		if (file == null) {
			throw new IllegalArgumentException("file can't be null.");
		}

		final File dir = file.isDirectory() ? file : file.getParentFile();
		if (dir == null) {
			throw new SystemException(String.format("can't get parent of: '%s'.", file));
		}

		if (!dir.exists()) {
			if (UtilsIO.logger.isTraceEnabled()) {
				UtilsIO.logger.trace(String.format("create directory: '%s'.", dir));
			}

			if (!dir.mkdirs()) {
				throw new SystemException(String.format("can't create directory: '%s'.", dir));
			}
		}
	}

	public static void copyFile(final File source, final File target) throws SystemException {

		if (source == null) {
			throw new IllegalArgumentException("source can't be null.");
		}

		if (!source.exists()) {
			throw new IllegalArgumentException(
					String.format("source at \"%s\" does not exist.", source.getAbsolutePath()));
		}

		if (source.isDirectory()) {
			throw new IllegalArgumentException(
					String.format("source at \"%s\" is a driectory.", source.getAbsolutePath()));
		}

		if (target == null) {
			throw new IllegalArgumentException("target can't be null.");
		}

		if (!target.exists()) {

			if (UtilsIO.logger.isDebugEnabled()) {
				UtilsIO.logger.debug("target will be created.");
			}

			UtilsIO.createParentDir(target);
		}

		try (BufferedInputStream in = UtilsIO.createInputStream(source)) {
			try (OutputStream out = UtilsIO.createOutputStream(target)) {
				final byte[] buffer = new byte[UtilsIO.STREAM_BUFFER_SIZE];
				int lengthRead;
				while ((lengthRead = in.read(buffer)) > 0) {
					out.write(buffer, 0, lengthRead);
					out.flush();
				}
			}

		} catch (final Exception ex) {
			final String message = String.format("can't copy file from \"%s\" to \"%s\".", source.getAbsolutePath(),
					target.getAbsolutePath());
			UtilsIO.logger.error(message, ex);
			throw new SystemException(message);
		}

	}

	public static BufferedWriter createWriter(final File file, final Charset charset, final boolean append)
			throws SystemException {
		if (file == null) {
			throw new IllegalArgumentException("file can't be null.");
		}

		if (UtilsIO.logger.isDebugEnabled()) {
			UtilsIO.logger.debug(String.format("create BufferedWriter for file: '%s'.", file));
		}

		UtilsIO.createParentDir(file);

		try {
			return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append), charset),
					UtilsIO.STREAM_BUFFER_SIZE);
		} catch (final IOException ex) {
			final String message = String.format("can't create output stream for: '%s'.", file.getAbsolutePath());
			UtilsIO.logger.error(message, ex);
			throw new SystemException(message);
		}
	}

	public static BufferedWriter createWriter(final File file, final boolean append) throws SystemException {
		return UtilsIO.createWriter(file, UtilsIO.ENCODING, append);
	}

	public static BufferedWriter createWriter(final File file) throws SystemException {
		return UtilsIO.createWriter(file, false);
	}

	public static void deleteFile(final File file) throws SystemException {
		if (file == null) {
			throw new IllegalArgumentException("file can't be null.");
		}

		try {
			if (file.exists()) {
				file.delete();
			} else {
				if (UtilsIO.logger.isDebugEnabled()) {
					UtilsIO.logger.debug(String.format(
							"The file at \"%s\" can't be deleted, because it does not exist.", file.getAbsolutePath()));
				}
			}
		} catch (final Exception ex) {
			final String message = String.format("cannot delete file at '%s'.", file.getAbsolutePath());
			UtilsIO.logger.error(message, ex);
			throw new SystemException(message);
		}
	}

	public static void cleanUp(final File directory, final FileFilter exceptFilter) throws SystemException {
		if (directory == null) {
			throw new IllegalArgumentException("directory can't be null.");
		}

		if (exceptFilter == null) {
			throw new IllegalArgumentException("exceptFilter can't be null.");
		}

		if (UtilsIO.logger.isDebugEnabled()) {
			UtilsIO.logger.debug(String.format("clean up directory: '%s'.", directory));
		}

		final File[] files = directory.listFiles();
		if (files == null) {
			return;
		}

		if (UtilsIO.logger.isDebugEnabled()) {
			UtilsIO.logger
					.debug(String.format("delete all files and directories in directory '%s', using filter: '%s'.",
							directory, exceptFilter));
		}

		for (final File file : files) {
			if (exceptFilter.accept(file)) {
				continue;
			}

			if (file.isFile()) {
				if (UtilsIO.logger.isDebugEnabled()) {
					UtilsIO.logger.debug(String.format("delete file: '%s'.", file));
				}

				if (!file.delete()) {
					throw new SystemException(String.format("can't delete file: '%s'.", file));
				}

				continue;
			}

			if (file.isDirectory()) {
				if (UtilsIO.logger.isDebugEnabled()) {
					UtilsIO.logger.debug(String.format("delete directory: '%s'.", file));
				}

				try {
					FileUtils.deleteDirectory(file);
				} catch (final IOException ex) {
					final String message = String.format("can't delete directory: '%s'.", file);
					UtilsIO.logger.error(message, ex);
					throw new SystemException(message);
				}
			}
		}
	}

	public static void cleanUp(final File directory, final File exceptFile) throws SystemException {
		if (directory == null) {
			throw new IllegalArgumentException("directory can't be null.");
		}

		if (exceptFile == null) {
			throw new IllegalArgumentException("exceptFile can't be null.");
		}

		final FileFilter exceptFilter = new FileFilter() {
			@Override
			public boolean accept(final File pathname) {
				return exceptFile.getAbsolutePath().equals(pathname.getAbsolutePath());
			}

			@Override
			public String toString() {
				return String.format("['%s': exceptFile '%s'.", Convert.getClassName(FileFilter.class), exceptFile);
			}
		};

		UtilsIO.cleanUp(directory, exceptFilter);
	}

	public static void cleanUp(final File directory) throws SystemException {
		if (directory == null) {
			throw new IllegalArgumentException("directory can't be null.");
		}

		final FileFilter exceptFilter = new FileFilter() {
			@Override
			public boolean accept(final File pathname) {
				return false;
			}

			@Override
			public String toString() {
				return String.format("'%s': always false.", Convert.getClassName(FileFilter.class));
			}
		};

		UtilsIO.cleanUp(directory, exceptFilter);
	}

	public static OutputStream createOutputStream(final URL url) throws SystemException {
		if (url == null) {
			throw new IllegalArgumentException("url can't be null.");
		}

		if (UtilsIO.logger.isDebugEnabled()) {
			UtilsIO.logger.debug(String.format("create OutputStream for url: '%s'.", url));
		}

		final File file = new File(url.getFile());
		UtilsIO.createParentDir(file);

		try {
			return new BufferedOutputStream(new FileOutputStream(file), UtilsIO.STREAM_BUFFER_SIZE);
		} catch (final IOException ex) {
			final String message = String.format("can't create output stream for file: '%s'.", file);
			UtilsIO.logger.error(message, ex);
			throw new SystemException(message);
		}
	}

	public static OutputStream createOutputStream(final File file, final boolean append) throws SystemException {
		if (file == null) {
			throw new IllegalArgumentException("file can't be null.");
		}

		if (UtilsIO.logger.isDebugEnabled()) {
			UtilsIO.logger.debug(String.format("create OutputStream for file: '%s'.", file));
		}

		UtilsIO.createParentDir(file);

		try {
			return new BufferedOutputStream(new FileOutputStream(file, append), UtilsIO.STREAM_BUFFER_SIZE);
		} catch (final IOException ex) {
			final String message = String.format("can't create output stream for file: '%s'.", file);
			UtilsIO.logger.error(message, ex);
			throw new SystemException(message);
		}
	}

	public static OutputStream createOutputStream(final File file) throws SystemException {
		return UtilsIO.createOutputStream(file, false);
	}

	public static BufferedInputStream createInputStream(final File file) throws SystemException {
		if (file == null) {
			throw new IllegalArgumentException("file can't be null.");
		}

		if (UtilsIO.logger.isDebugEnabled()) {
			UtilsIO.logger.debug(String.format("create InputStream for file: '%s'.", file));
		}

		try {
			return new BufferedInputStream(new FileInputStream(file), UtilsIO.STREAM_BUFFER_SIZE);
		} catch (final IOException ex) {
			final String message = String.format("can't create input stream for file: '%s'.", file);
			UtilsIO.logger.error(message, ex);
			throw new SystemException(message);
		}
	}

	@SuppressWarnings("resource")
	public static BufferedInputStream createInputStream(final String resource) throws SystemException {
		if (Convert.isEmpty(resource)) {
			throw new IllegalArgumentException("resource can't be null or empty string.");
		}

		if (UtilsIO.logger.isDebugEnabled()) {
			UtilsIO.logger.debug(String.format("create InputStream for: '%s'.", resource));
		}

		InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
		if (input == null) {
			if (UtilsIO.logger.isDebugEnabled()) {
				UtilsIO.logger
						.debug(String.format("can't get InputStream by ClassLoader for '%s', try by URL.", resource));
			}

			try {
				final URL url = new URL(resource);
				input = url.openStream();
			} catch (final IOException ex) {
				final String message = String.format("can't get InputStream for '%s'.", resource);
				UtilsIO.logger.error(message, ex);
				throw new SystemException(message);
			}
		}

		return new BufferedInputStream(input, UtilsIO.STREAM_BUFFER_SIZE);
	}

	public static BufferedInputStream createInputStream(final URL url) throws SystemException {
		if (url == null) {
			throw new IllegalArgumentException("url can't be null.");
		}

		if (UtilsIO.logger.isDebugEnabled()) {
			UtilsIO.logger.debug(String.format("create InputStream for url: '%s'.", url));
		}

		try {
			return new BufferedInputStream(url.openStream(), UtilsIO.STREAM_BUFFER_SIZE);
		} catch (final IOException ex) {
			final String message = String.format("can't create input stream for url: '%s'.", url);
			UtilsIO.logger.error(message, ex);
			throw new SystemException(message);
		}
	}

	public static Properties readProperties(final String resource) throws SystemException {
		final Properties result = new Properties();

		try (InputStream stream = UtilsIO.createInputStream(resource);) {
			if (UtilsIO.logger.isDebugEnabled()) {
				UtilsIO.logger.debug(String.format("read properties from resource: '%s'.", resource));
			}

			result.load(stream);

		} catch (final IOException ex) {
			final String message = String.format("can't read data from resource: '%s'.", resource);
			UtilsIO.logger.error(message, ex);
			throw new SystemException(message);
		}

		return result;
	}

	public static Properties readProperties(final File file) throws SystemException {
		final Properties result = new Properties();

		try (InputStream stream = UtilsIO.createInputStream(file);) {
			if (UtilsIO.logger.isDebugEnabled()) {
				UtilsIO.logger.debug(String.format("read properties from file: '%s'.", file));
			}

			result.load(stream);
		} catch (final IOException ex) {
			final String message = String.format("can't read data from file: '%s'.", file);
			UtilsIO.logger.error(message, ex);
			throw new SystemException(message);
		}

		return result;
	}

	public static void writeProperties(final File file, final Properties properties) throws SystemException {
		try (OutputStream stream = UtilsIO.createOutputStream(file);) {
			properties.store(stream, null);
		} catch (final IOException ex) {
			final String message = String.format("can't write data into file: '%s'.", file);
			UtilsIO.logger.error(message, ex);
			throw new SystemException(message);
		}
	}

	public static BufferedReader createReader(final InputStream stream) {
		if (stream == null) {
			throw new IllegalArgumentException("stream can't be null.");
		}

		return new BufferedReader(new InputStreamReader(stream), UtilsIO.STREAM_BUFFER_SIZE);
	}

	public static BufferedReader createReader(final InputStream stream, final Charset encoding) {
		if (stream == null) {
			throw new IllegalArgumentException("stream can't be null.");
		}

		if (encoding == null) {
			throw new IllegalArgumentException("encoding can't be null.");
		}

		return new BufferedReader(new InputStreamReader(stream, encoding), UtilsIO.STREAM_BUFFER_SIZE);
	}

	@SuppressWarnings("resource")
	public static BufferedReader createReader(final String resource, final Charset encoding) throws SystemException {
		if (encoding == null) {
			throw new IllegalArgumentException("encoding can't be null.");
		}

		return UtilsIO.createReader(UtilsIO.createInputStream(resource), encoding);
	}

	public static BufferedReader createReader(final File file) throws SystemException {
		if (file == null) {
			throw new IllegalArgumentException("file can't be null.");
		}

		return new BufferedReader(new InputStreamReader(UtilsIO.createInputStream(file)), UtilsIO.STREAM_BUFFER_SIZE);
	}

	@SuppressWarnings("resource")
	public static BufferedReader createReader(final File file, final Charset encoding) throws SystemException {
		return UtilsIO.createReader(UtilsIO.createInputStream(file), encoding);
	}

	public static List<String> read(final String resource, final Charset encoding) throws SystemException {
		if (UtilsIO.logger.isDebugEnabled()) {
			UtilsIO.logger.debug(String.format("read data from resource: '%s'.", resource));
		}

		if (encoding == null) {
			throw new IllegalArgumentException("encoding can't be null.");
		}

		try (BufferedInputStream stream = UtilsIO.createInputStream(resource);) {
			return IOUtils.readLines(stream, encoding);
		} catch (final IOException ex) {
			final String message = String.format("can't read from resource: '%s'.", resource);
			UtilsIO.logger.error(message, ex);
			throw new SystemException(message);
		}
	}

	public static List<String> read(final String resource) throws SystemException {
		return UtilsIO.read(resource, UtilsIO.ENCODING);
	}

	public static List<String> read(final File file, final Charset encoding) throws SystemException {
		if (file == null) {
			throw new IllegalArgumentException("file can't be null.");
		}

		if (encoding == null) {
			throw new IllegalArgumentException("encoding can't be null.");
		}

		if (UtilsIO.logger.isDebugEnabled()) {
			UtilsIO.logger.debug(String.format("read data from file: '%s'.", file));
		}

		try (BufferedInputStream stream = UtilsIO.createInputStream(file);) {
			return IOUtils.readLines(stream, encoding);
		} catch (final IOException ex) {
			final String message = String.format("can't read from file: '%s'.", file);
			UtilsIO.logger.error(message, ex);
			throw new SystemException(message);
		}
	}

	public static void write(final List<String> data, final File file, final Charset encoding) throws SystemException {
		if (data == null) {
			throw new IllegalArgumentException("data can't be null.");
		}

		if (file == null) {
			throw new IllegalArgumentException("file can't be null.");
		}

		if (encoding == null) {
			throw new IllegalArgumentException("encoding can't be null.");
		}

		if (UtilsIO.logger.isDebugEnabled()) {
			UtilsIO.logger.debug(String.format("write data into file: '%s'.", file));
		}

		try (OutputStream stream = UtilsIO.createOutputStream(file);) {
			IOUtils.writeLines(data, null, stream, encoding);
		} catch (final IOException ex) {
			final String message = String.format("can't read from file: '%s'.", file);
			UtilsIO.logger.error(message, ex);
			throw new SystemException(message);
		}
	}

	public static void append(final List<String> data, final File file, final Charset encoding) throws SystemException {
		if (data == null) {
			throw new IllegalArgumentException("data can't be null.");
		}

		if (file == null) {
			throw new IllegalArgumentException("file can't be null.");
		}

		if (encoding == null) {
			throw new IllegalArgumentException("encoding can't be null.");
		}

		if (UtilsIO.logger.isDebugEnabled()) {
			UtilsIO.logger.debug(String.format("write data into file: '%s'.", file));
		}

		try (OutputStream stream = UtilsIO.createOutputStream(file, true);) {
			IOUtils.writeLines(data, null, stream, encoding);
		} catch (final IOException ex) {
			final String message = String.format("can't read from file: '%s'.", file);
			UtilsIO.logger.error(message, ex);
			throw new SystemException(message);
		}
	}

	public static List<File> listFiles(final File directory, final String wildcard) throws SystemException {
		if (directory == null) {
			throw new IllegalArgumentException("directory can't be null.");
		}

		if (!directory.exists()) {
			throw new SystemException(String.format("directory '%s' don't exists.", directory));
		}

		if (!directory.isDirectory()) {
			throw new SystemException(String.format("directory '%s' is not a directory.", directory));
		}

		if (Convert.isEmpty(wildcard)) {
			throw new IllegalArgumentException("wildcard can't be null or empty string.");
		}

		final List<File> result = new ArrayList<>();

		for (File file : Arrays.asList(directory.listFiles())) {

			if (file.isDirectory()) {
				result.addAll(listFiles(file, wildcard));
			}
			try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory.toPath(), wildcard)) {
				dirStream.forEach(path -> result.add(path.toFile()));
			} catch (final IOException ex) {
				final String msg = String.format("can't read files from path '%s' and wildcard '%s'.",
						directory.toString(), wildcard);
				UtilsIO.logger.error(msg, ex);
				throw new SystemException(msg);
			}

		}

		return result;
	}

	public static void createFile(final File file) throws SystemException {

		if (file == null) {
			throw new IllegalArgumentException("file can't be null.");
		}

		try {
			UtilsIO.createParentDir(file);
			file.createNewFile();
		} catch (final Exception ex) {
			final String msg = String.format("can't create file '%s'.", file);
			UtilsIO.logger.error(msg, ex);
			throw new SystemException(msg);
		}
	}

	public static String getAllContentFromFile(final File file) throws SystemException {
		if (file == null) {
			throw new IllegalArgumentException("file can't be null.");
		}

		if (!file.exists()) {
			throw new IllegalArgumentException(String.format("The file at %s does not exist.", file.getAbsolutePath()));
		}

		final List<String> contents = new ArrayList<>();

		try (final BufferedReader reader = UtilsIO.createReader(file)) {
			String line;
			while ((line = reader.readLine()) != null) {
				contents.add(line);
			}
		} catch (final IOException ex) {
			final String msg = String.format("Cannot operate on the file at '%s'.", file);
			UtilsIO.logger.error(msg, ex);
			throw new SystemException(msg);
		}

		return String.join(Convert.EMPTY_STRING, contents);
	}

	/**
	 * @brief returns the content of an {@code InputStream} stream.
	 * @return content of the stream, empty String, if an error happened.
	 */
	public static String getAllContentFromInputStream(final InputStream stream) {
		if (stream == null) {
			throw new IllegalArgumentException("stream can't be null.");
		}

		String result = Convert.EMPTY_STRING;

		try {
			result = UtilsIO.getAllContentFromBytesArray(IOUtils.toByteArray(stream));
		} catch (final IOException ex) {
			UtilsIO.logger.error("can't fetch content -> return emtpy string.", ex);
		}

		return result;
	}

	/**
	 * @brief returns the content of an {@code byte[]} bytes.
	 * @return content of the stream, empty String, if an error happened.
	 */
	public static String getAllContentFromBytesArray(final byte[] bytes) {
		if (bytes == null) {
			throw new IllegalArgumentException("stream can't be null.");
		}

		final List<String> contents = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)))) {
			String line;
			while ((line = reader.readLine()) != null) {
				contents.add(line);
			}
		} catch (final IOException ex) {
			final String msg = "Cannot operate on the stream.";
			UtilsIO.logger.error(msg, ex);
			contents.clear();
		}

		return String.join(Convert.EMPTY_STRING, contents);
	}

	/**
	 * @brief returns the content of an {@code InputStream} stream in its literals.
	 * @return content of the stream, empty List, if an error happened.
	 */
	public static List<String> getContentsFromInputStream(final InputStream stream) {
		if (stream == null) {
			throw new IllegalArgumentException("stream can't be null.");
		}

		final List<String> contents = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
			String line;
			while ((line = reader.readLine()) != null) {
				contents.add(line);
			}
		} catch (final IOException ex) {
			final String msg = "Cannot operate on the stream.";
			UtilsIO.logger.error(msg, ex);
			contents.clear();
		}

		return contents;
	}

	public static void putAllContentToFile(final File file, final String content) throws SystemException {
		UtilsIO.putAllContentToFile(file, content, UtilsIO.ENCODING);
	}

	public static void putAllContentToFile(final File file, final String content, final Charset charset)
			throws SystemException {
		if (file == null) {
			throw new IllegalArgumentException("file can't be null.");
		}

		if (!file.exists()) {
			throw new IllegalArgumentException(
					String.format("The file at '%s' does not exist.", file.getAbsolutePath()));
		}

		if (Convert.isEmpty(content)) {
			throw new IllegalArgumentException("content can't be null or empty string.");
		}

		if (charset == null) {
			throw new IllegalArgumentException("charset can't be null.");
		}

		try (final OutputStream outputStream = UtilsIO.createOutputStream(file)) {
			outputStream.write(content.getBytes(charset));
		} catch (final IOException ex) {
			final String msg = String.format("cannot write to file '%s'.", file);
			UtilsIO.logger.error(msg, ex);
			throw new SystemException(msg);
		}
	}

	/**
	 * @return file {@param ressource} from classloader
	 * @throws SystemException, if file {@param ressource}, cannot be fetched
	 */
	public static File getRessourceFromClassloader(final String ressource) throws SystemException {
		if (Convert.isEmpty(ressource)) {
			throw new IllegalArgumentException("ressource can't be null or empty string.");
		}

		File result;
		try {
			final URL url = UtilsIO.class.getClassLoader().getResource(ressource);
			result = Paths.get(url.toURI()).toFile();
		} catch (final Throwable th) {
			final String msg = String.format("Can't fetch ressource '%s'.", ressource);
			UtilsIO.logger.error(msg, th);
			throw new SystemException(msg);
		}

		return result;
	}

	public static File createTempFile() {
		final String fileName = String.format(UUID.randomUUID().toString(), ".tmp");
		return UtilsIO.createTempFile(fileName);
	}

	public static File createTempFile(final String tempFileName) {
		if (Convert.isEmpty(tempFileName)) {
			throw new IllegalArgumentException("tempFileName can't be null or empty string.");
		}

		// TODO compare to File.createTempFile()
		final String temporaryFile = System.getProperty("java.io.tmpdir");
		if (Convert.isEmpty(temporaryFile)) {
			if (UtilsIO.logger.isDebugEnabled()) {
				UtilsIO.logger
						.debug(String.format("Cannot determine temporary directory of the JVM, will return null."));
			}

			return null;
		}

		final File tempFilePath = new File(temporaryFile);

		if (!tempFilePath.exists()) {
			if (UtilsIO.logger.isDebugEnabled()) {
				UtilsIO.logger
						.debug(String.format("Cannot determine temporary directory of the JVM, will return null."));
			}

			return null;
		}

		return new File(temporaryFile, tempFileName);
	}

}
