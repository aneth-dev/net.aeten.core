package net.aeten.core.util;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Pérennou
 * 
 * @see MessageFormat
 * @see ExtendedResourceBundleControl
 */
@ThreadSafe
public class Internationalizer<T extends Enum<?>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(Internationalizer.class);

	private final String description;

	private final MessageFormat[] messageFormat;
	private final String[] pattern;

	private static class NullResourceBundle extends ResourceBundle {
		@Override
		public Enumeration<String> getKeys() {
			return null;
		}

		@Override
		protected Object handleGetObject(String key) {
			if (key == null) { throw new NullPointerException("Key should not be null"); }
			return null;
		}
	}

	public Internationalizer(Class<T> clazz, Locale locale, ResourceBundle.Control resourceBundleControl) {
		description = "Internationalizer for " + clazz.getName() + ", with locale " + locale;
		final T[] enumConstants = clazz.getEnumConstants();
		ResourceBundle resourceBundle;
		try {
			resourceBundle = ResourceBundle.getBundle(clazz.getName(), locale, clazz.getClassLoader(), resourceBundleControl);
		} catch (MissingResourceException exception) {
			LOGGER.error("Resource for " + clazz.getName() + ", with locale " + locale + " is missing", exception);
			resourceBundle = new NullResourceBundle();
		}

		messageFormat = new MessageFormat[enumConstants.length];
		pattern = new String[enumConstants.length];
		for (int i = 0; i < enumConstants.length; i++) {
			try {
				pattern[i] = resourceBundle.getString(enumConstants[i].name());
			} catch (MissingResourceException exception) {
				if (!(resourceBundle instanceof NullResourceBundle)) {
					LOGGER.error("Pattern not found for " + clazz.getName() + "." + enumConstants[i].name() + ", locale " + locale, exception);
				}
				String result = enumConstants[i].name().replace('_', ' ').trim();
				pattern[i] = result.substring(0, 1).toUpperCase(Locale.ENGLISH) + result.substring(1).toLowerCase(Locale.ENGLISH);
			}
			messageFormat[i] = new MessageFormat(pattern[i]);
		}
	}

	public Internationalizer(Class<T> clazz, Locale locale) {
		this(clazz, locale, new ExtendedResourceBundleControl());
	}

	public Internationalizer(Class<T> clazz, ResourceBundle.Control resourceBundleControl) {
		this(clazz, Locale.getDefault(), resourceBundleControl);
	}

	public Internationalizer(Class<T> clazz) {
		this(clazz, Locale.getDefault());
	}

	/** @see MessageFormat#format(Object) */
	public String format(T key_p, Object... args_p) {
		MessageFormat messageFormat = this.messageFormat[key_p.ordinal()];
		synchronized (messageFormat) {
			return messageFormat.format(args_p);
		}
	}

	public String getPattern(T key_p) {
		return pattern[key_p.ordinal()];
	}

	@Override
	public String toString() {
		return description;
	}
}
