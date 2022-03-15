package de.simpleworks.staf.framework.util.assertion.functions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class STAFFunction {

	private static final Logger logger = LogManager.getLogger(STAFFunction.class);

	public Integer add(Integer summand1, Integer summand2) {

		if (STAFFunction.logger.isInfoEnabled()) {
			STAFFunction.logger.info(String.format("%s: Add %s to %s.", this.getClass(), summand1, summand2));
		}

		Integer result = summand1 + summand2;
		return result;
	}

	public Integer add(String summand1, Integer summand2) {

		if (STAFFunction.logger.isInfoEnabled()) {
			STAFFunction.logger.info(String.format("%s: Add %s to %s.", this.getClass(), summand1, summand2));
		}

		Integer result = new Integer(Integer.parseInt(summand1)) + summand2;
		return result;
	}

	public Integer add(String summand1, String summand2) {

		if (STAFFunction.logger.isInfoEnabled()) {
			STAFFunction.logger.info(String.format("%s: Add %s to %s.", this.getClass(), summand1, summand2));
		}

		Integer result = new Integer(Integer.parseInt(summand1)) + new Integer(Integer.parseInt(summand2));
		return result;
	}

	public Integer sub(String subtrahend1, Integer subtrahend2) {

		if (STAFFunction.logger.isInfoEnabled()) {
			STAFFunction.logger.info(String.format("%s: Add %s to %s.", this.getClass(), subtrahend1, subtrahend2.toString()));
		}

		Integer result = new Integer(Integer.parseInt(subtrahend1)) - subtrahend2;
		return result;
	}

	
	public String concat(String element1, String element2) {

		if (STAFFunction.logger.isInfoEnabled()) {
			STAFFunction.logger.info(String.format("%s: Add %s to %s.", this.getClass(), element1, element2));
		}

		String result = String.format("%s%s", element1, element2);
		return result;
	}

	public int max(Integer integer1, Integer integer2) {

		if (STAFFunction.logger.isInfoEnabled()) {
			STAFFunction.logger
					.info(String.format("%s: Detemrine max between %s and %s.", this.getClass(), integer1, integer2));
		}

		Integer result = Math.max(integer1, integer2);
		return result;
	}
}