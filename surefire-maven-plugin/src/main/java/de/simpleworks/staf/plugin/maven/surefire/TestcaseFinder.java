package de.simpleworks.staf.plugin.maven.surefire;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.annotation.Testcase;
import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsCollection;

public class TestcaseFinder {
	static final Logger logger = LogManager.getLogger(TestcaseFinder.class);

	private static final String SUFFIX_FILE_JAR = ".jar";
	private static final String SUFFIX_FILE_CLASS = ".class";
	private static final String FILE_MODULE_INFO = "module-info.class";
	private static final String SEPARATOR_FILE = "\\" + File.separator;
	private static final String SEPARATOR_PACKAGE = "\\.";
	private static final String SEPARATOR_JAR_PATH = "\\/";

	private final Map<String, Class<?>> map;
	private URLClassLoader loader;

	public TestcaseFinder() {
		map = new HashMap<>();
	}

	public static final String getClassName(final String className) {
		final int pos1 = className.lastIndexOf(TestcaseFinder.SUFFIX_FILE_CLASS);
		final String name = 0 < pos1 ? className.substring(0, pos1) : className;
		return name.replaceAll(TestcaseFinder.SEPARATOR_FILE, TestcaseFinder.SEPARATOR_PACKAGE);
	}

	private static File toFile(final URL url) throws SystemException {
		try {
			return new File(url.toURI());
		} catch (final URISyntaxException ex) {
			final String msg = String.format("can't convert url: '%s' to File.", url);
			TestcaseFinder.logger.error(msg, ex);
			throw new SystemException(msg);
		}
	}

	private static URL[] getUrls(final List<Object> classpath) throws SystemException {
		final List<URL> urls = new ArrayList<>();

		for (final Object ob : classpath) {
			try {
				final URL url = (new File((String) ob)).toURI().toURL();
				urls.add(url);
				if (TestcaseFinder.logger.isTraceEnabled()) {
					TestcaseFinder.logger.trace(String.format("url: '%s'.", url));
				}
			} catch (final MalformedURLException ex) {
				final String msg = String.format("can't convert '%s' to URL.", ob);
				TestcaseFinder.logger.error(msg, ex);
				throw new SystemException(msg);
			}
		}

		return UtilsCollection.toArray(URL.class, urls);

	}

	void checkClazz(final String className) throws IOException {
		if (TestcaseFinder.logger.isTraceEnabled()) {
			TestcaseFinder.logger.trace(String.format("check class: '%s'.", className));
		}

		final Class<?> clazz;
		try {
			clazz = loader.loadClass(className);
		} catch (final Throwable th) {
			// only debug info.
			if (TestcaseFinder.logger.isTraceEnabled()) {
				TestcaseFinder.logger.trace(String.format("can't load class: '%s'.", className), th);
				TestcaseFinder.logger.trace(String.format("skip class: '%s'.", className));
			}
			return;
		}

		final Testcase testcase = clazz.getAnnotation(Testcase.class);
		// class without annotation -> nothing to do.
		if (testcase == null) {
			return;
		}

		final String id = testcase.id();
		if (TestcaseFinder.logger.isTraceEnabled()) {
			TestcaseFinder.logger.trace(String.format("class '%s' has annotation: '%s' with id: '%s'.", clazz.getName(),
					Testcase.class.getName(), id));
		}

		final Class<?> found = map.get(id);
		if ((found != null) && !found.getName().equals(clazz.getName())) {
			throw new IOException(
					String.format("found multiple classes that are annotaed with id '%s' (class: '%s' and '%s').", id,
							found.getName(), clazz.getName()));
		}

		map.put(id, clazz);
	}

	private void loadDirectory(final File directory) throws SystemException {
		if (!directory.exists()) {
			if (TestcaseFinder.logger.isTraceEnabled()) {
				TestcaseFinder.logger.trace(String.format("nothing to do, directory: '%s' does not exist.", directory));
			}

			return;
		}

		final Path path = directory.toPath();
		if (TestcaseFinder.logger.isTraceEnabled()) {
			TestcaseFinder.logger.trace(String.format("scanning directory (path): '%s'.", path));
		}

		final int pos1 = path.toString().length() + 1;
		try {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
					final String fileName = file.toString();
					if (TestcaseFinder.logger.isTraceEnabled()) {
						TestcaseFinder.logger.trace(String.format("check fileName: '%s'.", fileName));
					}

					if (attrs.isRegularFile() && fileName.endsWith(TestcaseFinder.SUFFIX_FILE_CLASS)) {
						checkClazz(TestcaseFinder.getClassName(fileName.substring(pos1)));
					}

					return FileVisitResult.CONTINUE;
				}
			});
		} catch (final IOException ex) {
			final String msg = String.format("can't scan directory: '%s'.", directory);
			TestcaseFinder.logger.error(msg, ex);
			throw new SystemException(msg);
		}
	}

	private void loadJar(final File file) throws SystemException {
		if (!file.exists()) {
			if (TestcaseFinder.logger.isTraceEnabled()) {
				TestcaseFinder.logger.trace(String.format("nothing to do, jar file: '%s' does not exist.", file));
			}

			return;
		}

		if (TestcaseFinder.logger.isTraceEnabled()) {
			TestcaseFinder.logger.trace(String.format("scanning jar file: '%s'.", file));
		}

		try (JarFile jarFile = new JarFile(file)) {
			
			
			List<JarEntry> list = Collections.list(jarFile.entries());
			
			
			list.stream().parallel().forEach(e -> {
				final JarEntry jarEntry = e;
				final String name = jarEntry.getName();
				if (name.endsWith(TestcaseFinder.SUFFIX_FILE_CLASS)
						&& !name.endsWith(TestcaseFinder.FILE_MODULE_INFO)) {
					final String className = name
							.replaceAll(TestcaseFinder.SEPARATOR_JAR_PATH, TestcaseFinder.SEPARATOR_PACKAGE)
							.replace(TestcaseFinder.SUFFIX_FILE_CLASS, "");
					try {
						checkClazz(className);
					} catch (final IOException ex) {
						final String message = String.format("can't check class: '%s' from jar: '%s'.", className,
								file);
						TestcaseFinder.logger.error(message, ex);
						//throw new SystemException(message);
					}
				}
			});
			
			
		} catch (final IOException ex) {
			final String message = String.format("can't read jar: '%s'.", file);
			TestcaseFinder.logger.error(message, ex);
			throw new SystemException(message);
		}

	}

	private static void loadClass(final File file) throws SystemException {
		if (!file.exists()) {
			if (TestcaseFinder.logger.isTraceEnabled()) {
				TestcaseFinder.logger.trace(String.format("nothing to do, class file: '%s' does not exist.", file));
			}

			return;
		}

		throw new SystemException("currently is load single classes not supported.");
	}

	private void load(final URL url) throws SystemException {
		if (TestcaseFinder.logger.isTraceEnabled()) {
			TestcaseFinder.logger.trace(String.format("load class(es) from url: '%s'.", url));
		}

		final File file = TestcaseFinder.toFile(url);
		if (TestcaseFinder.logger.isTraceEnabled()) {
			TestcaseFinder.logger.trace(String.format("load class(es) from file: '%s'.", file));
		}

		if (!file.exists()) {
			if (TestcaseFinder.logger.isTraceEnabled()) {
				TestcaseFinder.logger.trace(String.format("nothing to do, file: '%s' does not exist.", file));
			}

			return;
		}

		if (file.isDirectory()) {
			loadDirectory(file);
			return;
		}

		if (file.isFile()) {
			final String fileName = file.getName();
			if (TestcaseFinder.logger.isTraceEnabled()) {
				TestcaseFinder.logger.trace(String.format("work for file: '%s'.", fileName));
			}

			if (fileName.endsWith(TestcaseFinder.SUFFIX_FILE_JAR)) {
				loadJar(file);
				return;
			}

			if (fileName.endsWith(TestcaseFinder.SUFFIX_FILE_CLASS)) {
				TestcaseFinder.loadClass(file);
				return;
			}

			throw new SystemException(
					String.format("unsupported file: '%s' (supported are jar and class files).", file));
		}

		throw new SystemException(
				String.format("unsupported file: '%s' (supported are directories and regular files).", file));
	}

	public final void load(final List<Object> classpath) throws SystemException {
		if (TestcaseFinder.logger.isInfoEnabled()) {
			TestcaseFinder.logger.info("loading classes..");
		}

		final URL[] urls = TestcaseFinder.getUrls(classpath);
		try (URLClassLoader l = URLClassLoader.newInstance(urls, this.getClass().getClassLoader());) {
			loader = l;
			for (final URL url : urls) {
				load(url);
			}
		} catch (final IOException ex) {
			final String msg = "can't close class loader.";
			TestcaseFinder.logger.error(msg, ex);
			throw new SystemException(msg);
		} finally {
			loader = null;
		}
	}

	public final Class<?> get(final String id) {
		if (Convert.isEmpty(id)) {
			throw new IllegalArgumentException("id can't be null or empty string.");
		}

		return map.get(id);
	}
}
