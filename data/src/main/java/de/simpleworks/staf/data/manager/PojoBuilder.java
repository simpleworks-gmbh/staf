package de.simpleworks.staf.data.manager;

import com.google.inject.Provider;

import de.simpleworks.staf.data.utils.Data;

public abstract class PojoBuilder<Pojo extends Data> implements Provider<Pojo> {
	public abstract Class<Pojo> getType();
}
