package de.simpleworks.staf.commons.web.stafelements;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import de.simpleworks.staf.commons.exceptions.SystemException;
import de.simpleworks.staf.commons.utils.Convert;

public class STAFTable extends STAFElement {
	private static final Logger logger = LogManager.getLogger(STAFTable.class);

	final List<WebElement> rows;
	final List<WebElement> headers;

	public STAFTable(final WebDriver webDriver, final By by) {
		super(webDriver, by);

		rows = new ArrayList<>();
		headers = new ArrayList<>();
	}

	public List<WebElement> getHeader() {
		if (Convert.isEmpty(headers)) {
			final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(getTimeout()));
			wait.until(ExpectedConditions.visibilityOfElementLocated(getBy()));

			try {
				headers.addAll(getChildElements(By.tagName("th")));
			} catch (final SystemException ex) {
				final String msg = "can't fetch headers.";
				STAFTable.logger.error(msg, ex);
				headers.clear();
			}

			if (STAFTable.logger.isDebugEnabled()) {
				STAFTable.logger.debug(String.format("Found %d columns.", Integer.valueOf(headers.size())));
			}
		}

		return headers;
	}

	public List<WebElement> getRows() {
		if (Convert.isEmpty(rows)) {
			final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(getTimeout()));
			wait.until(ExpectedConditions.visibilityOfElementLocated(getBy()));

			try {
				rows.addAll(getChildElements(By.tagName("tr")));
			} catch (final SystemException ex) {
				final String msg = "can't fetch rows.";
				STAFTable.logger.error(msg, ex);
				rows.clear();
			}

			if (STAFTable.logger.isDebugEnabled()) {
				STAFTable.logger.debug(String.format("Found %d rows", Integer.valueOf(rows.size())));
			}
		}

		return rows;
	}

	public STAFRow<WebElement> getSTAFRowByIndex(final int index) {
		return getSTAFRowByIndex(index, getHeader());
	}

	public STAFRow<WebElement> getSTAFRowByIndex(final int index, final List<WebElement> header) {
		if (Convert.isEmpty(header)) {
			throw new IllegalArgumentException("header can't be null or empty.");
		}

		final List<WebElement> cells = getRowByIndex(index).findElements(By.tagName("td"));
		if (STAFTable.logger.isDebugEnabled()) {
			STAFTable.logger.debug(String.format("cells contains %d values.", Integer.valueOf(cells.size())));
		}

		final STAFRow<WebElement> row = new STAFRow<>(
				header.stream().map(cell -> cell.getText()).collect(Collectors.toList()), cells);

		return row;
	}

	public List<STAFRow<WebElement>> getSTAFRowByCellValue(final String cellValue) {
		if (Convert.isEmpty(cellValue)) {
			throw new IllegalArgumentException("cellValue can't be null or empty.");
		}

		return getSTAFRowByCellValue(cellValue, getHeader());
	}

	public List<STAFRow<WebElement>> getSTAFRowByCellValue(final String cellValue, final List<WebElement> header) {

		if (Convert.isEmpty(cellValue)) {
			throw new IllegalArgumentException("cellValue can't be null or empty.");
		}

		if (Convert.isEmpty(header)) {
			throw new IllegalArgumentException("header can't be null or empty.");
		}

		final List<WebElement> array = getRowsByCellValue(cellValue);
		if (STAFTable.logger.isDebugEnabled()) {
			STAFTable.logger.debug(String.format("Found %d rows.", Integer.valueOf(array.size())));
		}

		final List<STAFRow<WebElement>> stafRows = new ArrayList<>();
		array.stream().forEach(row -> {
			stafRows.add(new STAFRow<>(header.stream().map(cell -> cell.getText()).collect(Collectors.toList()),
					row.findElements(By.tagName("td"))));
		});

		if (STAFTable.logger.isDebugEnabled()) {
			stafRows.forEach(row -> {
				STAFTable.logger.debug(String.format("Found %d rows.", Integer.valueOf(array.size())));
			});
		}

		return stafRows;
	}

	private WebElement getRowByIndex(final int index) {
		if (index < 0) {
			throw new IllegalArgumentException(
					String.format("index can't be less than 0, currently is %d.", Integer.valueOf(index)));
		}

		final List<WebElement> array = getRows();
		if (index >= array.size()) {
			throw new IllegalArgumentException(
					String.format("index can't be more than rows size, current index is %d and rows size is %d.",
							Integer.valueOf(index), Integer.valueOf(array.size())));
		}

		final WebElement row = array.get(index);
		return row;
	}

	public List<WebElement> getRowsByCellValue(final String cellValue) {
		if (Convert.isEmpty(cellValue)) {
			if (STAFTable.logger.isDebugEnabled()) {
				STAFTable.logger.debug("cellValue is empty.");
			}
		}

		final List<WebElement> matchingRows = getRows().stream().filter(row -> {
			if (STAFTable.logger.isDebugEnabled()) {
				STAFTable.logger
						.debug(String.format("Searching for Key '%s' in the row at '%s'.", cellValue, row.toString()));
			}

			final WebDriverWait wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(getTimeout()));
			wait.until(ExpectedConditions.visibilityOfElementLocated(getBy()));

			return row.findElements(By.xpath(String.format(".//*[contains(text(), \"%s\")]", cellValue))).size() > 0;
		}).collect(Collectors.toList());

		if (STAFTable.logger.isDebugEnabled()) {
			STAFTable.logger.debug(String.format("Found %d rows, that contain any cell '%s'.",
					Integer.valueOf(matchingRows.size()), cellValue));
		}

		return matchingRows;
	}
}
