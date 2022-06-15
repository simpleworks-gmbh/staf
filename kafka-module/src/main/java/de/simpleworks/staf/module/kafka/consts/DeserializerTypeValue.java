package de.simpleworks.staf.module.kafka.consts;

import de.simpleworks.staf.commons.utils.Convert;

public class DeserializerTypeValue {
	public static String UNKNOWN = Convert.EMPTY_STRING;
	public static String LONG_DESERIALIZER = "org.apache.kafka.common.serialization.LongDeserializer";
	public static String STRING_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";
}