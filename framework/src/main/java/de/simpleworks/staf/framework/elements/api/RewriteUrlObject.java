package de.simpleworks.staf.framework.elements.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;
import de.simpleworks.staf.data.exception.InvalidDataConstellationExcpetion;
import de.simpleworks.staf.data.utils.Data;

public class RewriteUrlObject extends Data {
	private static final Logger logger = LogManager.getLogger(RewriteUrlObject.class);

	private String pattern;
	private String rewriteExpression;

	public RewriteUrlObject() {
		pattern = Convert.EMPTY_STRING;
		rewriteExpression = Convert.EMPTY_STRING;
	}

	public String getPattern() {
		return pattern;
	}

	public String getRewriteExpression() {
		return rewriteExpression;
	}

	public void setPattern(final String pattern) {
		this.pattern = pattern;
	}

	public void setRewriteExpression(final String rewriteExpression) {
		this.rewriteExpression = rewriteExpression;
	}

	@Override
	public void validate() throws InvalidDataConstellationExcpetion {
		if (RewriteUrlObject.logger.isDebugEnabled()) {
			RewriteUrlObject.logger.debug(String.format("validate: '%s'.", toString()));
		}

		if (Convert.isEmpty(pattern)) {
			throw new InvalidDataConstellationExcpetion("source can't be null or empty string.");
		}

		if (Convert.isEmpty(rewriteExpression)) {
			throw new InvalidDataConstellationExcpetion("target can't be null or empty string.");
		}
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s].", Convert.getClassName(RewriteUrlObject.class),
				UtilsFormat.format("pattern", pattern), UtilsFormat.format("rewriteExpression", rewriteExpression));
	}
}