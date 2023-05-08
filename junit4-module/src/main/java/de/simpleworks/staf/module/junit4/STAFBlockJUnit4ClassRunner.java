package de.simpleworks.staf.module.junit4;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.runner.Description;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.utils.Scanner;
import de.simpleworks.staf.framework.elements.commons.ATestCaseImpl;
import de.simpleworks.staf.framework.util.STAFUtils;

/**
 * @brief STAF Unit Runner to substitute
 *        org.junit.runners.BlockJUnit4ClassRunner
 */
public class STAFBlockJUnit4ClassRunner extends BlockJUnit4ClassRunner {

	private static final Logger logger = LogManager.getLogger(STAFBlockJUnit4ClassRunner.class);

	private final Class<? extends ATestCaseImpl> testClass;
	protected Object test;

	@SuppressWarnings("unchecked")
	public STAFBlockJUnit4ClassRunner(final Class<?> clazz) throws InitializationError {
		super(clazz);

		if (!Scanner.doesClassExtendSpecificClass(clazz, ATestCaseImpl.class)) {
			throw new InitializationError(String.format("clazz needs to extends '%s'.", ATestCaseImpl.class.getName()));
		}

		this.testClass = (Class<? extends ATestCaseImpl>) clazz;
	}

	@Override
	protected Object createTest() throws Exception {
		if (test == null) {
			test = super.createTest();
		}

		return test;
	}

	@Override
	protected Statement methodInvoker(final FrameworkMethod method, final Object test2) {
		if (test == null) {
			try {
				test = new ReflectiveCallable() {
					@Override
					protected Object runReflectiveCall() throws Throwable {
						return createTest();
					}
				}.run();
			} catch (final Throwable e) {
				return new Fail(e);
			}
		}

		return new STAFInvokeMethod(method, test);
	}

	@Override
	protected List<FrameworkMethod> getChildren() {
		// adding debug mode, if not testplan is available

		List<FrameworkMethod> result = new ArrayList<>();
		try {
			result = STAFUtils.sortMethods(this.testClass).stream().map(method -> new FrameworkMethod(method))
					.collect(Collectors.toList());
		} catch (final SystemException ex) {
			final String msg = "can't determine methods for test execution.";
			STAFBlockJUnit4ClassRunner.logger.error(msg, ex);
			throw new IllegalStateException(msg);
		}

		return result;
	}

	@Override
	protected Description describeChild(final FrameworkMethod method) {
		return Description.createTestDescription(testClass.getName(), method.getName(), method.getAnnotations());
	}
	
}