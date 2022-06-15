package de.simpleworks.staf.module.kafka.api;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public class KafkaConsumeResponse implements IKafkaResponse<KafkaConsumeResponse> {

	private static final Logger logger = LogManager.getLogger(KafkaConsumeResponse.class);

	private KafkaConsumeRecord[] records;

	public KafkaConsumeResponse() {
		records = new KafkaConsumeRecord[0];
	}

	public KafkaConsumeRecord[] getRecords() {
		return records;
	}

	public void setRecords(KafkaConsumeRecord[] records) {
		this.records = records;
	}

	@Override
	public boolean validate() {

		if (KafkaConsumeResponse.logger.isDebugEnabled()) {
			KafkaConsumeResponse.logger.debug("validate KafkaConsumeResponse...");
		}

		boolean result = true;

		if (Convert.isEmpty(records)) {

			for (KafkaConsumeRecord record : Arrays.asList(records)) {

				if (record == null) {
					KafkaConsumeResponse.logger.error("record can't be null.");
					result = false;
				}

				if (!record.validate()) {
					KafkaConsumeResponse.logger.error(String.format("record '%s' is invalid.", record));
					result = false;
				}
			}
		}

		return result;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s]", Convert.getClassName(KafkaConsumeResponse.class), UtilsFormat.format("records",
				String.join(",", Arrays.asList(records).stream().map(a -> a.toString()).collect(Collectors.toList()))));
	}

	@Override
	public Class<KafkaConsumeResponse> getType() {
		return KafkaConsumeResponse.class;
	}

}
