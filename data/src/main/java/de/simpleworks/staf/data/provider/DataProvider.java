package de.simpleworks.staf.data.provider;

import com.google.inject.Provider;

import de.simpleworks.staf.data.utils.Data;

public interface DataProvider extends Provider<Data> {
	@Override
	Data get();
}
