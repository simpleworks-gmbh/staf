package de.simpleworks.staf.module.kafka.api;

import de.simpleworks.staf.commons.interfaces.IPojo;

public interface IKafkaResponse<T> extends IPojo {
	public Class<T> getType();
}
