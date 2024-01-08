package de.simpleworks.staf.commons.enums;

import java.util.List;

import de.simpleworks.staf.commons.consts.ContentTypeValue;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.IEnum;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public enum ContentTypeEnum implements IEnum {
	NONE("NONE", ContentTypeValue.NONE), UNKNOWN("unkown", ContentTypeValue.UNKNOWN),
	OCTET_STREAM("application/octet-stream", ContentTypeValue.OCTET_STREAM),
	JSON("application/json", ContentTypeValue.JSON), ZIP("application/zip", ContentTypeValue.ZIP),
	PDF("application/pdf", ContentTypeValue.PDF), DOC("application/msword", ContentTypeValue.DOC),
	DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ContentTypeValue.DOCX),
	DOTX("application/vnd.openxmlformats-officedocument.wordprocessingml.template", ContentTypeValue.DOTX),
	DOCM("application/vnd.ms-word.document.macroEnabled.12", ContentTypeValue.DOCM),
	DOTM("application/vnd.ms-word.template.macroEnabled.12", ContentTypeValue.DOTM),
	XLS("application/vnd.ms-excel", ContentTypeValue.XLS),
	XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ContentTypeValue.XLSX),
	XLTX("application/vnd.openxmlformats-officedocument.spreadsheetml.template", ContentTypeValue.XLTX),
	XLSM("application/vnd.ms-excel.sheet.macroEnabled.12", ContentTypeValue.XLSM),
	XLTM("application/vnd.ms-excel.template.macroEnabled.12", ContentTypeValue.XLSM),
	XLAM("application/vnd.ms-excel.addin.macroEnabled.12", ContentTypeValue.XLAM),
	XLSB("application/vnd.ms-excel.sheet.binary.macroEnabled.12", ContentTypeValue.XLSB),
	PPT("application/vnd.ms-powerpoint", ContentTypeValue.PPT),
	PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation", ContentTypeValue.PPTX),
	POTX("application/vnd.openxmlformats-officedocument.presentationml.template", ContentTypeValue.POTX),
	PPSX("application/vnd.openxmlformats-officedocument.presentationml.slideshow", ContentTypeValue.PPSX),
	PPAM("application/vnd.ms-powerpoint.addin.macroEnabled.12", ContentTypeValue.PPAM),
	PPTM("application/vnd.ms-powerpoint.presentation.macroEnabled.12", ContentTypeValue.PPTM),
	POTM("application/vnd.ms-powerpoint.template.macroEnabled.12", ContentTypeValue.POTM),
	PPSM("application/vnd.ms-powerpoint.slideshow.macroEnabled.12", ContentTypeValue.PPSM),
	PNG("image/png", ContentTypeValue.PNG), JPG("image/jpg", ContentTypeValue.JPG),
	JPEG("image/jpeg", ContentTypeValue.JPEG),
	FORM_URLENCODED("application/x-www-form-urlencoded", ContentTypeValue.FORM_URLENCODED),
	MULTIPART_FORM_DATA("multipart/form-data", ContentTypeValue.MULTIPART_FORM_DATA),
	TEXT("text/plain", ContentTypeValue.TEXT), CSV("text/csv", ContentTypeValue.CSV),
	HTML("text/html", ContentTypeValue.HTML),
	SPRINGBOOT_ACTUATOR_V2_JSON("application/vnd.spring-boot.actuator.v2+json", ContentTypeValue.SPRINGBOOT_ACTUATOR_V2_JSON),
	SPRINGBOOT_ACTUATOR_V3_JSON("application/vnd.spring-boot.actuator.v3+json", ContentTypeValue.SPRINGBOOT_ACTUATOR_V3_JSON),
	PROBLEM_JSON("application/problem+json", ContentTypeValue.PROBLEM_JSON);

	final private String name;
	final private String value;

	ContentTypeEnum(final String name, final String value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public List<IEnum> getValues() {
		return UtilsCollection.toList(ContentTypeEnum.values());
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s]", Convert.getClassName(ContentTypeEnum.class),
				UtilsFormat.format("name", name), UtilsFormat.format("value", value));
	}
}
