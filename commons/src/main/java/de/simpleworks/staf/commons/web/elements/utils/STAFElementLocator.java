package de.simpleworks.staf.commons.web.elements.utils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Locatable;
import org.openqa.selenium.internal.WrapsElement;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.openqa.selenium.support.pagefactory.internal.LocatingElementHandler;

import de.simpleworks.staf.commons.web.stafelements.STAFElement;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * This class creates handles the calls of methods of custom webelements.
 **/
public class STAFElementLocator implements MethodInterceptor {
	private static final Logger logger = LogManager.getLogger(STAFElementLocator.class);

	/**
	 * The locator to get the webelement from the webpage.
	 **/
	private final ElementLocator locator;

	/**
	 * The constructor.
	 *
	 * @param locator The locator to get the webelement from the webpage.
	 **/
	public STAFElementLocator(final ElementLocator locator) {
		if (locator == null) {
			throw new IllegalArgumentException("locator can't be null.");
		}

		this.locator = locator;
	}

	/**
	 * Handles the method calls to a custom webelement.
	 *
	 * @param o           The object from which the method was called.
	 * @param method      The called method.
	 * @param objects     The parameter object of the value.
	 * @param methodProxy Used to call the method of the superclass.
	 **/
	@Override
	public Object intercept(final Object o, final Method method, final Object[] objects, final MethodProxy methodProxy)
			throws Throwable {
		// Configure a custom webelement (WebButton etc.)
		if (o instanceof STAFElement) {
			if (STAFElementLocator.logger.isTraceEnabled()) {
				STAFElementLocator.logger.trace(String.format(
						"STAFElement: call method: '%s' by proxy: '%s' with object: '%s'.", method, methodProxy, o));
			}

			// Invokes the method of the original object
			try {
				return methodProxy.invokeSuper(o, objects);
			} catch (final InvocationTargetException e) {
				STAFElementLocator.logger
						.error(String.format("STAFElement: can't call method: '%s' by proxy: '%s' with object: '%s'.",
								method, methodProxy, o), e);
				throw e.getCause();
			}
		}

		// Configure a normal webelement
		// Should never be called in the current usecase because it gets handled in the
		// CustomElementFieldDecorator class
		if (o instanceof WebElement) {
			// Only handle first displayed
			// Get the first default webelement which matches the locator
			final WebElement displayedElement = locateElement();
			if (displayedElement != null) {
				if (STAFElementLocator.logger.isTraceEnabled()) {
					STAFElementLocator.logger.trace(String.format(
							"WebElement: call method: '%s' with displayedElement: '%s'.", method, displayedElement));
				}
				return method.invoke(displayedElement, objects);
			}

			if (STAFElementLocator.logger.isTraceEnabled()) {
				STAFElementLocator.logger
						.trace(String.format("WebElement: call method: '%s' with object: '%s'.", method, o));
			}
			return methodProxy.invokeSuper(o, objects);
		}

		return null;
	}

	/**
	 * Get an instance of the webelement.
	 *
	 * @return Returns a proxy element which implements the webelement. This is
	 *         needed to call the isVisible and other methods on itself (the custom
	 *         web element) without getting a nasty exception.
	 **/
	private WebElement locateElement() {
		return STAFElementLocator.proxyForLocator(ElementLocator.class.getClassLoader(), locator);
	}

	/**
	 * Creates a dynamic proxy element for a webelement. Stolen form the
	 * DefaultFieldDecorator.class class in the Selenium lib. Further information on
	 * Proxies:
	 * https://docs.oracle.com/javase/7/docs/api/java/lang/reflect/Proxy.html
	 *
	 * @param loader  The class loader used to create the proxy.
	 * @param locator The element locator used to locate the webelement.
	 * @return The proxy webelement.
	 **/
	private static WebElement proxyForLocator(final ClassLoader loader, final ElementLocator locator) {
		final InvocationHandler handler = new LocatingElementHandler(locator);

		return (WebElement) Proxy.newProxyInstance(loader,
				new Class[] { WebElement.class, WrapsElement.class, Locatable.class }, handler);
	}
}
