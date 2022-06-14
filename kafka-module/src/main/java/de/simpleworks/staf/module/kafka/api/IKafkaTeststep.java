package de.simpleworks.staf.module.kafka.api;

import de.simpleworks.staf.commons.interfaces.ITeststep;

public interface IKafkaTeststep<RequestType> extends ITeststep {
	IKafkaRequest<RequestType> getRequest();
	void setRequest(RequestType request);
}
