package de.simpleworks.staf.framework.elements.composited;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Module;

import de.simpleworks.staf.commons.annotation.Property;
import de.simpleworks.staf.commons.annotation.Step;
import de.simpleworks.staf.commons.api.APITeststep;
import de.simpleworks.staf.commons.composited.CompositedTeststep;
import de.simpleworks.staf.commons.database.DbTeststep;
import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.interfaces.ITeststep;
import de.simpleworks.staf.commons.mapper.api.MapperAPITeststep;
import de.simpleworks.staf.commons.mapper.composited.MapperCompositedTeststep;
import de.simpleworks.staf.commons.mapper.database.MapperDbTeststep;
import de.simpleworks.staf.commons.report.artefact.Artefact;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.Scanner;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.commons.utils.UtilsDate;
import de.simpleworks.staf.commons.utils.UtilsEnum;
import de.simpleworks.staf.commons.utils.UtilsIO;
import de.simpleworks.staf.framework.api.httpclient.TeststepProvider;
import de.simpleworks.staf.framework.elements.api.APITestCase;
import de.simpleworks.staf.framework.elements.api.RewriteUrlObject;
import de.simpleworks.staf.framework.elements.commons.TestCase;
import de.simpleworks.staf.framework.elements.composited.template.APITestcaseTemplate;
import de.simpleworks.staf.framework.elements.composited.template.DBTestcaseTemplate;
import de.simpleworks.staf.framework.elements.database.DbTestCase;
import net.lightbody.bmp.BrowserMobProxyServer;

public class CompositedTestCase extends TestCase {

	private static final Logger logger = LoggerFactory.getLogger(CompositedTestCase.class);
	private static final MapperCompositedTeststep mapper = new MapperCompositedTeststep();

	public final static String ENVIRONMENT_VARIABLES_NAME = "CompositedTestCase";

	private final TeststepProvider<CompositedTeststep> provider;

	private final Map<String, String> props;

	private APITestCase apiTestCase;
	private DbTestCase dbTestCase;

	private CompositedTeststep currentCompositedteststep;

	protected CompositedTestCase(final String resource, final ACompositedTestCase[] testcases, final Module... modules)
			throws Exception {

		super(modules);

		if (Convert.isEmpty(resource)) {
			throw new IllegalArgumentException("resource can't be null or empty string.");
		}

		if (Convert.isEmpty(testcases)) {
			throw new IllegalArgumentException("testcases can't be null or empty string.");
		}

		props = initEnvironmentVariables();

		for (ACompositedTestCase testcase : testcases) {

			switch (testcase.getTestcasekind()) {

			case API_TESTCASE:

				APICompositedTestCase apiCompositedTestCase = (APICompositedTestCase) testcase;

				final List<APITeststep> apiTeststeps = mapper.read(resource).stream().map(step -> step.getApiteststep())
						.filter(Objects::nonNull).collect(Collectors.toList());
				;

				apiTestCase = initAPITestCase(apiCompositedTestCase, apiTeststeps);

				break;

			case DATABASE_TESTCASE:

				DBCompositedTestCase databaseCompositedTestCase = (DBCompositedTestCase) testcase;

				final List<DbTeststep> dbTeststeps = mapper.read(resource).stream().map(step -> step.getDbteststep())
						.filter(Objects::nonNull).collect(Collectors.toList());

				dbTestCase = initDBTestCase(databaseCompositedTestCase, dbTeststeps);

				break;

			default:
				throw new IllegalArgumentException(
						String.format("Testcase Type '%s' is not implemented yet.", testcase.getTestcasekind()));
			}
		}

		try {

			if (CompositedTestCase.logger.isDebugEnabled()) {
				CompositedTestCase.logger.debug(String.format("read steps from: '%s'.", resource));
			}

			final List<CompositedTeststep> steps = mapper.read(resource);

			// FEHLER!!!!
			final List<Step> methodSteps = UtilsCollection.toList(this.getClass().getMethods()).stream()
					.map(method -> method.getAnnotation(Step.class)).filter(Objects::nonNull)
					.collect(Collectors.toList());

			provider = new TeststepProvider<>(steps, methodSteps);

		} catch (final Exception ex) {
			final String msg = String.format("can't initialize instance of class '%s'.",
					Convert.getClassFullName(this));
			CompositedTestCase.logger.error(msg, ex);
			throw new SystemException(msg);
		}
	}

	private APITestCase initAPITestCase(APICompositedTestCase apiCompositedTestcase, List<APITeststep> apiteststeps)
			throws Exception {

		File tempFile = UtilsIO.createTempFile(String.format("%s-%s-%s.json", APITestCase.class.toString(),
				UtilsDate.getCurrentTime(), getTestCaseName()));

		if (tempFile == null) {
			final String msg = "can't create temp file.";
			CompositedTestCase.logger.error(msg);
			throw new SystemException(msg);
		}

		tempFile.createNewFile();

		final String content = (new MapperAPITeststep()).readAll(apiteststeps);

		UtilsIO.putAllContentToFile(tempFile, content, StandardCharsets.UTF_8);

		final List<ITeststep> steps = apiteststeps.stream().map(apiteststep -> (ITeststep) apiteststep)
				.collect(Collectors.toList());

		CompositedTestcaseEquipper.equipClass("de.simpleworks.staf.framework.elements.composited.template.APITestcaseTemplate", steps,
				getExtractedValues().get(ENVIRONMENT_VARIABLES_NAME), props);

		Object result = new APITestcaseTemplate(tempFile.getAbsoluteFile().getAbsolutePath(),
				apiCompositedTestcase.getModules());

		if (!(result instanceof APITestCase)) {
			final String msg = String.format("'%s' is no instance of '%s'.", result.getClass(), DbTestCase.class);
			CompositedTestCase.logger.error(msg);
			throw new SystemException(msg);
		}

		return (APITestCase) result;
	}

	private DbTestCase initDBTestCase(DBCompositedTestCase dbCompositedTestcase, List<DbTeststep> dbteststeps)
			throws Exception {

		File tempFile = UtilsIO.createTempFile(String.format("%s-%s-%s.json", DbTestCase.class.toString(),
				UtilsDate.getCurrentTime(), getTestCaseName()));

		if (tempFile == null) {
			final String msg = "can't create temp file.";
			CompositedTestCase.logger.error(msg);
			throw new SystemException(msg);
		}

		tempFile.createNewFile();

		final String content = (new MapperDbTeststep()).readAll(dbteststeps);

		UtilsIO.putAllContentToFile(tempFile, content, StandardCharsets.UTF_8);

		final List<ITeststep> steps = dbteststeps.stream().map(dbteststep -> (ITeststep) dbteststep)
				.collect(Collectors.toList());
  
		CompositedTestcaseEquipper.equipClass("de.simpleworks.staf.framework.elements.composited.template.DBTestcaseTemplate", steps,
				getExtractedValues().get(ENVIRONMENT_VARIABLES_NAME), props);

		Object result = new DBTestcaseTemplate(tempFile.getAbsoluteFile().getAbsolutePath(),
				dbCompositedTestcase.getModules());

		if (!(result instanceof DbTestCase)) {
			final String msg = String.format("'%s' is no instance of '%s'.", result.getClass(), DbTestCase.class);
			CompositedTestCase.logger.error(msg);
			throw new SystemException(msg);
		}

		return (DbTestCase) result;
	}

	@Override
	public void bootstrap() throws Exception {

		if (CompositedTestCase.logger.isDebugEnabled()) {
			CompositedTestCase.logger
					.debug(String.format("bootstrap APITestCase '%s'.", apiTestCase.getTestCaseName()));
		}

		apiTestCase.bootstrap();

		if (CompositedTestCase.logger.isDebugEnabled()) {
			CompositedTestCase.logger.debug(String.format("bootstrap DbTestCase '%s'.", dbTestCase.getTestCaseName()));
		}

		dbTestCase.bootstrap();
	}

	@Override
	public void shutdown() throws Exception {
		if (CompositedTestCase.logger.isDebugEnabled()) {
			CompositedTestCase.logger.debug(String.format("shutdown APITestCase '%s'.", apiTestCase.getTestCaseName()));
		}

		apiTestCase.shutdown();

		if (CompositedTestCase.logger.isDebugEnabled()) {
			CompositedTestCase.logger.debug(String.format("shutdown DbTestCase '%s'.", dbTestCase.getTestCaseName()));
		}

		dbTestCase.shutdown();
	}

	protected TeststepProvider<CompositedTeststep> getProvider() {
		return provider;
	}

	@Override
	public void executeTestStep() throws Exception {
		TeststepProvider<CompositedTeststep> provider = getProvider();

		if (provider == null) {
			throw new IllegalArgumentException("provider can't be null.");
		}

		currentCompositedteststep = provider.get();

		if (currentCompositedteststep == null) {
			return;
		}

		if (getExtractedValues() == null) {
			throw new IllegalStateException("extractedValues can't be null.");
		}

		if (CompositedTestCase.logger.isDebugEnabled()) {
			CompositedTestCase.logger.debug(String.format("next acompositedteststep '%s'.", currentCompositedteststep));
		}

		if (currentCompositedteststep.getApiteststep() != null) {
			apiTestCase.executeTestStep();
			String currentstepname = currentCompositedteststep.getName();
			Map<String, String> map = apiTestCase.getExtractedValues().get(currentstepname);
			addExtractedValues(DbTestCase.ENVIRONMENT_VARIABLES_NAME, map);
		}

		if (currentCompositedteststep.getDbteststep() != null) {
			dbTestCase.executeTestStep();
			String currentstepname = currentCompositedteststep.getName();
			Map<String, String> map = dbTestCase.getExtractedValues().get(currentstepname);
			addExtractedValues(APITestCase.ENVIRONMENT_VARIABLES_NAME, map);
		}
	}

	// FIXME: Boilerplate
	protected Map<String, String> initEnvironmentVariables() throws Exception {

		final Map<String, String> map = new HashMap<>();
		final Map<String, String> propertyMap = new HashMap<>();

		for (Field field : Scanner.getAnnotatedFields(this.getClass(), Property.class)) {
			field.setAccessible(true);

			Property property = field.getAnnotation(Property.class);
			if (property != null) {

				final String propertyKey = property.value();
				final String name = field.getName();

				String value = System.getProperty(propertyKey, Convert.EMPTY_STRING);

				if (field.getAnnotation(Inject.class) != null) {
					try {
						if (Convert.isEmpty(value)) {
							value = (String) field.get(this);
						}
					} catch (Exception ex) {
						final String msg = "can't determine value.";
						CompositedTestCase.logger.error(msg, ex);
						throw new SystemException(msg);
					}
				}

				final Class<?> type = field.getType();

				if (int.class.equals(type)) {
					field.set(this, Integer.valueOf(value));
				} else if (double.class.equals(type)) {
					field.set(this, Double.valueOf(value));
				} else if (boolean.class.equals(type)) {
					field.set(this, Boolean.valueOf(value));
				} else if (float.class.equals(type)) {
					field.set(this, Float.valueOf(value));
				} else if (long.class.equals(type)) {
					field.set(this, Long.valueOf(value));
				} else if (String.class.equals(type)) {
					field.set(this, value);
				} else if (Object.class.equals(type)) {
					if (Convert.isEmpty(value)) {
						value = null;
						field.set(this, value);
					}
				} else if (type.isEnum()) {
					final Object typeValue = UtilsEnum.getEnum(type, value);
					field.set(this, typeValue);
				} else {
					throw new IllegalArgumentException(
							String.format("Cannot handle type: '%s', value '%s'.", type, value));
				}

				map.put(name, value);
				propertyMap.put(name, propertyKey);

				if (CompositedTestCase.logger.isDebugEnabled()) {
					CompositedTestCase.logger.debug(String.format("save map \"%s:%s\".", name, value));
				}

			} else {
				throw new InstantiationError(String.format("can't set up variable %s.", ENVIRONMENT_VARIABLES_NAME));
			}

			addExtractedValues(ENVIRONMENT_VARIABLES_NAME, map);
		}

		return propertyMap;
	}

	// FIXME: Boilerplate
	protected void addExtractedValues(final String key, final Map<String, String> values) {
		if (Convert.isEmpty(key)) {
			throw new IllegalArgumentException("key can't be null or empty string.");
		}

		if (values == null) {
			return;
		}

		if (CompositedTestCase.logger.isDebugEnabled()) {
			CompositedTestCase.logger.debug(String.format("add variables for key: '%s'.", key));
		}

		if (getExtractedValues() == null) {
			throw new IllegalStateException("extractedValues can't be null.");
		}

		if (getExtractedValues().containsKey(key)) {
			final Map<String, String> tmp = getExtractedValues().get(key);

			tmp.keySet().stream().forEach(k -> {
				if (!values.containsKey(k)) {

					final String str = tmp.get(k);
					values.put(k, str);
				}
			});
		}

		getExtractedValues().put(key, values);
	}

	@Override
	public BrowserMobProxyServer getProxy() {
		return apiTestCase.getProxy();
	}

	@Override
	public List<RewriteUrlObject> getRewriteUrls() {
		return apiTestCase.getRewriteUrls();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Artefact createArtefact() {

		if (currentCompositedteststep == null) {
			return null;
		}

		Artefact result = null;

		if (currentCompositedteststep.getApiteststep() != null) {
			result = apiTestCase.createArtefact();
		}

		if (currentCompositedteststep.getDbteststep() != null) {
			result = dbTestCase.createArtefact();
		}

		return result;
	}

}
