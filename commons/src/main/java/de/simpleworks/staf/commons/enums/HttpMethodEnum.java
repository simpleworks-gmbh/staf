package de.simpleworks.staf.commons.enums;

import java.util.List;

import de.simpleworks.staf.commons.consts.HttpMethodValue;
import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.IEnum;
import de.simpleworks.staf.commons.utils.IHttpMethodEnum;
import de.simpleworks.staf.commons.utils.UtilsCollection;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public enum HttpMethodEnum implements IEnum, IHttpMethodEnum {
	GET("GET", HttpMethodValue.GET) {
		@Override
		public boolean hasRequestBody() {
			return false;
		}
	},
	DELETE("DELETE", HttpMethodValue.DELETE) {
		@Override
		public boolean hasRequestBody() {
			return false;
		}
	},
	PUT("PUT", HttpMethodValue.PUT) {
		@Override
		public boolean hasRequestBody() {
			return true;
		}
	},
	POST("POST", HttpMethodValue.POST) {
		@Override
		public boolean hasRequestBody() {
			return true;
		}
	},
	PATCH("PATCH", HttpMethodValue.PATCH) {
		@Override
		public boolean hasRequestBody() {
			return true;
		}
	};

	final private String name;
	final private String value;

	HttpMethodEnum(final String name, final String value) {
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
		return UtilsCollection.toList(HttpMethodEnum.values());
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s]", Convert.getClassName(HttpMethodEnum.class),
				UtilsFormat.format("name", name), UtilsFormat.format("value", value));
	}
}
