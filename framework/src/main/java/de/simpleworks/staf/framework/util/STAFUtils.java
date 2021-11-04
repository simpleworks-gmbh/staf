package de.simpleworks.staf.framework.util;

import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.framework.elements.commons.TestCase;

public class STAFUtils {
	private static final Logger logger = LoggerFactory.getLogger(STAFUtils.class);

	public STAFUtils() {
		throw new IllegalStateException("utility class.");
	}

	/**
	 * @brief the STAF-Framework, executes the test methods, in the order, that is
	 *        defined in the specific testcase description
	 * @param list of methods of a
	 * @return "STAF-compliant" sorted list, null or empty list, if methods, does
	 *         not comply at all.
	 * @throws SystemException is thrown, if any method, does violate the
	 *                         "STAF-compliance"
	 */
	public static List<Method> sortMethods(final Class<? extends TestCase> testClass) throws SystemException {
		final List<Method> result = TestCaseUtils.fetchStepMethods(testClass);

		if (Convert.isEmpty(result)) {
			final String msg = "stepmethods can't be null or empty.";
			STAFUtils.logger.error(msg);
			throw new SystemException(msg);
		}

		return result;
	}
}
