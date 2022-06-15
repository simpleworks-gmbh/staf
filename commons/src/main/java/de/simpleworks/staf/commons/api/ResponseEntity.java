package de.simpleworks.staf.commons.api;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.interfaces.IPojo;
import de.simpleworks.staf.commons.mapper.Mapper;
import de.simpleworks.staf.commons.utils.Convert;

public class ResponseEntity implements IPojo {

	private static final Logger logger = LogManager.getLogger(ResponseEntity.class);

	private String classname;
	private String mapperClassname;
	private Object[] entities;

	public ResponseEntity() {
		this.classname = Convert.EMPTY_STRING;
		this.mapperClassname = Convert.EMPTY_STRING;
		this.entities = new Object[0];
	}

	public ResponseEntity(ResponseEntity entity) {
		this.classname = entity.getClassname();
		this.mapperClassname = entity.getMapperClassname();
		this.entities = entity.getEntities();
	}

	public String getClassname() {
		return classname;
	}

	public void setClassname(String classname) {
		this.classname = classname;
	}

	public String getMapperClassname() {
		return mapperClassname;
	}

	public void setMapperClassname(String mapperClassname) {
		this.mapperClassname = mapperClassname;
	}

	public Object[] getEntities() {
		return entities;
	}

	public void setEntities(Object[] entities) {
		this.entities = entities;
	}

	public Class<?> getEntityClass() {

		if (!this.validate()) {
			ResponseEntity.logger.error("current response entity is invalid, can't return entity class '%s'.");
			return null;
		}

		Class<?> result = null;

		try {
			result = Class.forName(classname);
		} catch (ClassNotFoundException e) {
			ResponseEntity.logger.error("can't find class '%s' , will return null.");
		}

		return result;
	}

	@SuppressWarnings("deprecation")
	public Mapper<?> getMapper() {

		if (!this.validate()) {
			ResponseEntity.logger.error("current response entity is invalid, can't return mapper '%s'.");
			return null;
		}

		Mapper<?> result = null;
		Class<?> mapperClass = null;

		try {
			mapperClass = Class.forName(mapperClassname);
			result = (Mapper<?>) mapperClass.newInstance();
		} catch (Exception ex) {
			ResponseEntity.logger.error("can't create Mapper, will return null.", ex);
		}

		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((classname == null) ? 0 : classname.hashCode());
		result = (prime * result) + ((mapperClassname == null) ? 0 : mapperClassname.hashCode());
		result = (prime * result) + Arrays.hashCode(entities);
		return result;
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean validate() {

		if (ResponseEntity.logger.isDebugEnabled()) {
			ResponseEntity.logger.debug(String.format("validate: '%s'.", toString()));
		}

		boolean result = true;

		if (Convert.isEmpty(classname)) {
			ResponseEntity.logger.error("classname can't be null or empty string.");
			result = false;
		}

		if (Convert.isEmpty(mapperClassname)) {
			ResponseEntity.logger.error("mapperClassname can't be null or empty string.");
			result = false;
		}

		Class<?> mapperClass = null;

		try {
			mapperClass = Class.forName(mapperClassname);
		} catch (ClassNotFoundException e) {
			ResponseEntity.logger.error(String.format("can't find class '%s'.", mapperClassname));
			result = false;
		}

		Object mapperOb;

		try {

			if (mapperClass != null) {
				mapperOb = mapperClass.newInstance();

				if (!(mapperOb instanceof Mapper)) {
					ResponseEntity.logger
							.error(String.format("mapperOb does not extend '%s'.", Mapper.class.getName()));
					result = false;
				}
			}

		} catch (InstantiationException e) {
			ResponseEntity.logger.error(String.format("can't initialize mapper from class '%s'.", mapperClassname), e);
			result = false;
		} catch (IllegalAccessException e) {
			ResponseEntity.logger.error("can't access mapper.", e);
			result = false;
		}

		return result;
	}

}
