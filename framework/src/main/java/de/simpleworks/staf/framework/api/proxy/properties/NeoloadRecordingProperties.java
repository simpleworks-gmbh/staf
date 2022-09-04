package de.simpleworks.staf.framework.api.proxy.properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.annotation.Property;
import de.simpleworks.staf.commons.annotation.Property.Default;
import de.simpleworks.staf.commons.utils.PropertiesReader;
import de.simpleworks.staf.framework.consts.FrameworkConsts;

public class NeoloadRecordingProperties extends PropertiesReader {
	private static final Logger logger = LogManager.getLogger(NeoloadRecordingProperties.class);

	private static NeoloadRecordingProperties instance = null;

	@Default("UserPath")
	@Property(FrameworkConsts.NEOLOAD_USER_PATH)
	private String neoloadUserPath;

	@Property(FrameworkConsts.NEOLOAD_PROJECT_PATH)
	private String neoloadProjectPath;

	public String getNeoloadUserPath() {
		return neoloadUserPath;
	}

	public String getNeoloadProjectPath() {
		return neoloadProjectPath;
	}

	@Override
	protected Class<?> getClazz() {
		return NeoloadRecordingProperties.class;
	}

	public static final synchronized NeoloadRecordingProperties getInstance() {
		if (NeoloadRecordingProperties.instance == null) {
			if (NeoloadRecordingProperties.logger.isDebugEnabled()) {
				NeoloadRecordingProperties.logger.debug("create instance.");
			}

			NeoloadRecordingProperties.instance = new NeoloadRecordingProperties();
		}

		return NeoloadRecordingProperties.instance;
	}
}