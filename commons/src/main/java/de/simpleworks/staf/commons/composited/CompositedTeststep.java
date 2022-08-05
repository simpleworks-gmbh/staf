package de.simpleworks.staf.commons.composited;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.api.APITeststep;
import de.simpleworks.staf.commons.database.DbTeststep;
import de.simpleworks.staf.commons.interfaces.ITeststep;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public class CompositedTeststep implements ITeststep {

	private static final Logger logger = LogManager.getLogger(CompositedTeststep.class);
  
	private APITeststep apiteststep;
	private DbTeststep dbteststep;

	public CompositedTeststep() {
		apiteststep = null;
		dbteststep = null;
	}

	public APITeststep getApiteststep() {
		return apiteststep;
	}

	public void setApiteststep(APITeststep apiteststep) {
		this.apiteststep = apiteststep;
	}

	public DbTeststep getDbteststep() {
		return dbteststep;
	}

	public void setDbteststep(DbTeststep dbteststep) {
		this.dbteststep = dbteststep;
	}
	
	@Override
	public String getName() {

		String result = Convert.EMPTY_STRING;

		if ((apiteststep != null)) {
			result = apiteststep.getName();
		}

		if ((dbteststep != null)) {
			result = dbteststep.getName();
		}

		return result;
	}

	@Override
	public int getOrder() {

		int result = -1;

		if ((apiteststep != null)) {
			result = apiteststep.getOrder();
		}

		if ((dbteststep != null)) {
			result = dbteststep.getOrder();
		}

		return result;
	}

	@Override
	public boolean validate() {
		if (CompositedTeststep.logger.isDebugEnabled()) {
			CompositedTeststep.logger.debug("validate CompositedTeststep...");
		}

		boolean result = true;

		if ((apiteststep == null) && (dbteststep == null)) {
			CompositedTeststep.logger.error("apiteststep and dbteststep can't be null at the same time.");
			result = false;
		}

		if ((apiteststep != null) && (dbteststep != null)) {
			CompositedTeststep.logger.error("apiteststep and dbteststep can't be set at the same time.");
			result = false;
		}

		if ((apiteststep != null)) {
			if ((!apiteststep.validate())) {
				CompositedTeststep.logger.error(String.format("apiteststep is invalid '%s'.", apiteststep));
			}
		}

		if ((dbteststep != null)) {
			if ((!dbteststep.validate())) {
				CompositedTeststep.logger.error(String.format("dbteststep is invalid '%s'.", dbteststep));
			}
		}

		return result;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s]", Convert.getClassName(CompositedTeststep.class),
				UtilsFormat.format("apiteststep", apiteststep), UtilsFormat.format("dbteststep", dbteststep));
	}

}
