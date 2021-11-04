package de.simpleworks.staf.commons.web.stafelements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebElement;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public class STAFRow<Type extends WebElement> extends HashMap<String, Type> {
	private static final long serialVersionUID = 7144701675953428963L;
	private static final Logger logger = LogManager.getLogger(STAFRow.class);

	public STAFRow(final List<String> keys, final List<Type> values) {
		if (Convert.isEmpty(keys)) {
			throw new IllegalArgumentException("keys can't be null.");
		}

		if (Convert.isEmpty(values)) {
			throw new IllegalArgumentException("values can't be null.");
		}

		if (keys.size() != values.size()) {
			if (STAFRow.logger.isDebugEnabled()) {
				STAFRow.logger.debug(String.format("keys and values can't be from different sizes: keys %d, values %d.",
						Integer.valueOf(keys.size()), Integer.valueOf(values.size())));
			}
		}

		// @WORKAROUND https://simpleworks.atlassian.net/browse/STAF-50
		for (int itr = 0; itr < values.size(); itr += 1) {
			final String key = keys.get(itr);
			final String value = ((WebElement) values.get(itr)).getText();

			if (STAFRow.logger.isDebugEnabled()) {
				STAFRow.logger.debug(String.format("adding key '%s' with value '%s'.", key, value));
			}

			this.put(keys.get(itr), values.get(itr));
		}
	}

	@Override
	public String toString() {
		final List<String> message = new ArrayList<>();

		this.keySet().stream().forEach(key -> {
			message.add(String.format("[Key : '%s', Value : '%s']", key, this.get(key)));
		});

		return String.format("[%s': '%s']", Convert.getClassName(STAFRow.class),
				UtilsFormat.format("message", String.join(",", message)));
	}
}
