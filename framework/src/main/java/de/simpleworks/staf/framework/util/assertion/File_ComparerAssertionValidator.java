package de.simpleworks.staf.framework.util.assertion;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.api.Assertion;
import de.simpleworks.staf.commons.api.HttpResponse;
import de.simpleworks.staf.commons.enums.AllowedValueEnum;
import de.simpleworks.staf.commons.enums.ValidateMethodEnum;
import de.simpleworks.staf.commons.utils.UtilsIO;

public class File_ComparerAssertionValidator extends AssertionUtils<HttpResponse> {

	private static final Logger logger = LogManager.getLogger(File_ComparerAssertionValidator.class);

	@Override
	public Map<String, String> validateAssertion(final HttpResponse response, final Assertion assertion) {
		AssertionUtils.check(response, assertion, ValidateMethodEnum.FILE_COMPARER);

		final Map<String, String> result = new HashMap<>();

		final String filePath = assertion.getValue();
		if (File_ComparerAssertionValidator.logger.isDebugEnabled()) {
			File_ComparerAssertionValidator.logger.debug(String.format("using filePath '%s'.", filePath));
		}

		final AllowedValueEnum allowedValueEnum = assertion.getAllowedValue();
		if (File_ComparerAssertionValidator.logger.isDebugEnabled()) {
			File_ComparerAssertionValidator.logger
					.debug(String.format("using allowedValue '%s'.", allowedValueEnum.getValue()));
		}

		switch (allowedValueEnum) {
		case EXACT_VALUE:
			final String base64String = response.getBase64Body();
			final File tmpFile = UtilsIO.createTempFile();
			final boolean conversionSucceeded = UtilsIO.convertBase64StringtoFile(base64String, tmpFile);
			if (!conversionSucceeded) {
				throw new RuntimeException(
						"The assertion was not met. The file to be comapred, can't be transformed, from base64 back to a file.");
			}

			if (!UtilsIO.checkIfFilesAreEqual(tmpFile, new File(filePath))) {
				throw new RuntimeException("The assertion was not met. The files do not match as it was expected.");
			}

			result.put(assertion.getId(), base64String);
			break;

		default:
			throw new IllegalArgumentException(
					String.format("The allowedValueEnum '%s' is not implemented yet.", allowedValueEnum.getValue()));
		}

		return result;
	}
}
