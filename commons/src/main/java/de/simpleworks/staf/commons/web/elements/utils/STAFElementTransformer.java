package de.simpleworks.staf.commons.web.elements.utils;

import java.lang.reflect.Field;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.By.ByClassName;
import org.openqa.selenium.By.ByCssSelector;
import org.openqa.selenium.By.ById;
import org.openqa.selenium.By.ByLinkText;
import org.openqa.selenium.By.ByName;
import org.openqa.selenium.By.ByPartialLinkText;
import org.openqa.selenium.By.ByTagName;
import org.openqa.selenium.By.ByXPath;
import org.openqa.selenium.support.FindBy;

/**
 * Offers transformation methods and locator access.
 **/
public class STAFElementTransformer {
	private final static Logger logger = LogManager.getLogger(STAFElementTransformer.class);

	/**
	 * The different selenium locators.
	 **/
	public enum LocatorType {
		ID, CSS, TAG_NAME, NAME, XPATH, LINK_TEXT, PARTIAL_LINK_TEXT, CLASS_NAME
	}

	/**
	 * Transforms a FindBy annotation to a By locator.
	 *
	 * @param findBy The FindBy annotation which should be transformed to a By
	 *               locator.
	 * @return The locator which was created from the given parameter.
	 **/
	public static By transformFindByToBy(final FindBy findBy) {
		if (findBy == null) {
			return null;
		} else if ((findBy.id() != null) && !findBy.id().isEmpty()) {
			return By.id(findBy.id());
		} else if ((findBy.name() != null) && !findBy.name().isEmpty()) {
			return By.name(findBy.name());
		} else if ((findBy.xpath() != null) && !findBy.xpath().isEmpty()) {
			return By.xpath(findBy.xpath());
		} else if ((findBy.css() != null) && !findBy.css().isEmpty()) {
			return By.cssSelector(findBy.css());
		} else if ((findBy.className() != null) && !findBy.className().isEmpty()) {
			return By.className(findBy.className());
		} else if ((findBy.linkText() != null) && !findBy.linkText().isEmpty()) {
			return By.linkText(findBy.linkText());
		} else if ((findBy.partialLinkText() != null) && !findBy.partialLinkText().isEmpty()) {
			return By.partialLinkText(findBy.partialLinkText());
		} else if ((findBy.tagName() != null) && !findBy.tagName().isEmpty()) {
			return By.tagName(findBy.tagName());
		}

		throw new IllegalArgumentException(String.format("FindBy could not be mapped to By: '%s'.", findBy));
	}

	/**
	 * Returns the used type of a given by locator.
	 *
	 * @param locator Locator for which the used type should be returned.
	 * @return Returns the used type of a given by locator.
	 **/
	public static LocatorType getLocatorType(final By locator) {
		if (locator == null) {
			throw new IllegalArgumentException("locator can't be null.");
		}

		if (locator instanceof ById) {
			return LocatorType.ID;
		} else if (locator instanceof ByXPath) {
			return LocatorType.XPATH;
		} else if (locator instanceof ByClassName) {
			return LocatorType.CLASS_NAME;
		} else if (locator instanceof ByName) {
			return LocatorType.NAME;
		} else if (locator instanceof ByTagName) {
			return LocatorType.TAG_NAME;
		} else if (locator instanceof ByCssSelector) {
			return LocatorType.CSS;
		} else if (locator instanceof ByLinkText) {
			return LocatorType.LINK_TEXT;
		} else if (locator instanceof ByPartialLinkText) {
			return LocatorType.PARTIAL_LINK_TEXT;
		}

		throw new IllegalArgumentException(String.format("The locator '%s' could not be recognized.", locator));
	}

	/**
	 * Returns the locator value of a locator.
	 *
	 * @param locator The locator which value should be returned.
	 * @param type    The type of the locator.
	 * @return The value of the locator.
	 **/
	public static String getLocatorValue(final By locator, final LocatorType type) {
		if (locator == null) {
			throw new IllegalArgumentException("locator can't be null.");
		}

		if (type == null) {
			throw new IllegalArgumentException("type can't be null.");
		}

		Field locatorField;

		// Get the proper field
		try {
			final Class<?> clazz = locator.getClass();
			if (STAFElementTransformer.logger.isTraceEnabled()) {
				STAFElementTransformer.logger.trace(String.format("loaded class: '%s'.", clazz));
			}

			switch (type) {
			case ID:
				locatorField = clazz.getDeclaredField("id");
				break;
			case CLASS_NAME:
				locatorField = clazz.getDeclaredField("className");
				break;
			case CSS:
				locatorField = clazz.getDeclaredField("selector");
				break;
			case LINK_TEXT:
				locatorField = clazz.getDeclaredField("linkText");
				break;
			case NAME:
				locatorField = clazz.getDeclaredField("name");
				break;
			case PARTIAL_LINK_TEXT:
				locatorField = clazz.getDeclaredField("linkText");
				break;
			case TAG_NAME:
				locatorField = clazz.getDeclaredField("name");
				break;
			case XPATH:
				locatorField = clazz.getDeclaredField("xpathExpression");
				break;
			default:
				throw new IllegalArgumentException(
						String.format("The locator value for '%s' with the type '%s' was not found.", locator, type));
			}
		} catch (NoSuchFieldException | SecurityException e) {
			final String message = String.format("can't receive the value for the locator '%s' with the type '%s'.",
					locator, type);
			STAFElementTransformer.logger.error(message, e);
			throw new RuntimeException(message);
		}

		// Make private field accessible to be able to access its value
		locatorField.setAccessible(true);

		// Return the value of the locator
		try {
			return (String) locatorField.get(locator);
		} catch (final IllegalAccessException e) {
			final String message = String.format("can't receive the value for the locator '%s' with the type '%s'.",
					locator, type);
			STAFElementTransformer.logger.error(message, e);
			throw new RuntimeException(message);
		}
	}
}
