package de.simpleworks.staf.module.kafka.consts;

import de.simpleworks.staf.commons.utils.Convert;

public class SerializerTypeValue {
	public static String UNKNOWN = Convert.EMPTY_STRING;
	public static String LONG_SERIALIZER = "org.apache.kafka.common.serialization.LongSerializer";
	public static String STRING_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
}