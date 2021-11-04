package de.simpleworks.staf.data.utils;

import de.simpleworks.staf.commons.utils.Convert;
import de.simpleworks.staf.commons.utils.UtilsFormat;

public abstract class DataUsed extends Data {
	private boolean used;
	private String usedBy;

	public boolean isUsed() {
		return used;
	}

	public void setUsed(final boolean used) {
		this.used = used;
	}

	public String getUsedBy() {
		return usedBy;
	}

	public void setUsedBy(final String usedBy) {
		this.usedBy = usedBy;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s, %s]", Convert.getClassName(DataUsed.class), UtilsFormat.format("used", used),
				UtilsFormat.format("usedBy", usedBy));
	}
}
