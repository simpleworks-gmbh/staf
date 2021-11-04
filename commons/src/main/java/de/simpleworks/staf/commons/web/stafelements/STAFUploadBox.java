package de.simpleworks.staf.commons.web.stafelements;

import java.io.File;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class STAFUploadBox extends STAFElement {
	public STAFUploadBox(final WebDriver webDriver, final By by) {
		super(webDriver, by);
	}

	public void interactToFileDialog(final File file) {
		if (file == null) {
			throw new IllegalArgumentException("file can't be null.");
		}

		if (!file.exists()) {
			throw new IllegalArgumentException(
					String.format("the file at '%s' does not exist.", file.getAbsolutePath()));
		}

		getWebDriver().findElement(getBy()).sendKeys(file.getAbsolutePath());
	}
}
