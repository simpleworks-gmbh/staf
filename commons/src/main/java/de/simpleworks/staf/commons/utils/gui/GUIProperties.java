package de.simpleworks.staf.commons.utils.gui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.annotation.Property;
import de.simpleworks.staf.commons.annotation.Property.Default;
import de.simpleworks.staf.commons.consts.CommonsConsts;
import de.simpleworks.staf.commons.utils.PropertiesReader;

public class GUIProperties extends PropertiesReader {
	private static final Logger logger = LogManager.getLogger(GUIProperties.class);

	private static GUIProperties instance = null;

	@Default("60")
	@Property(CommonsConsts.GUI_TIMEOUT)
	private int timeout;

	/**
	 * @brief return timeout in seconds
	 */
	public int getTimeout() {
		return timeout;
	}

	@Override
	protected Class<?> getClazz() {
		return GUIProperties.class;
	}

	public static final synchronized GUIProperties getInstance() {
		if (GUIProperties.instance == null) {
			if (GUIProperties.logger.isDebugEnabled()) {
				GUIProperties.logger.debug("create instance.");
			}

			GUIProperties.instance = new GUIProperties();
		}

		return GUIProperties.instance;
	}
}
