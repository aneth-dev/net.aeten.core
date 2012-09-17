package net.aeten.core.util;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.aeten.core.logging.LogLevel;
import net.aeten.core.logging.Logger;
import net.jcip.annotations.ThreadSafe;

/**
 * @author Thomas Pérennou
 * 
 * @see MessageFormat
 * @see ExtendedResourceBundleControl
 */
@ThreadSafe
public class Internationalizer<T extends Enum<?>> {
	private final String description;

	private final MessageFormat[] messageFormat;
	private final String[] pattern;

	private static class NullResourceBundle extends
			ResourceBundle {
		@Override
		public Enumeration<String> getKeys() {
			return null;
		}

		@Override
		protected Object handleGetObject(String key) {
			if (key == null) {
				throw new NullPointerException ("Key should not be null");
			}
			return null;
		}
	}

	public Internationalizer(Class<T> clazz,
			Locale locale,
			ResourceBundle.Control resourceBundleControl) {
		this.description = "Internationalizer for " + clazz.getName () + ", with locale " + locale;
		final T[] enumConstants = clazz.getEnumConstants ();
		ResourceBundle resourceBundle;
		try {
			resourceBundle = ResourceBundle.getBundle (clazz.getName (), locale, clazz.getClassLoader (), resourceBundleControl);
		} catch (MissingResourceException exception) {
			Logger.log (this, LogLevel.ERROR, exception);
			resourceBundle = new NullResourceBundle ();
		}

		this.messageFormat = new MessageFormat[enumConstants.length];
		this.pattern = new String[enumConstants.length];
		for (int i = 0; i < enumConstants.length; i++) {
			try {
				this.pattern[i] = resourceBundle.getString (enumConstants[i].name ());
			} catch (MissingResourceException exception) {
				if (!(resourceBundle instanceof NullResourceBundle)) {
					Logger.log (this, LogLevel.ERROR, "Pattern not found for " + clazz.getName () + "." + enumConstants[i].name () + ", locale " + locale, exception);
				}
				String result = enumConstants[i].name ().replace ('_', ' ').trim ();
				this.pattern[i] = result.substring (0, 1).toUpperCase (Locale.ENGLISH) + result.substring (1).toLowerCase (Locale.ENGLISH);
			}
			this.messageFormat[i] = new MessageFormat (this.pattern[i]);
		}
	}

	public Internationalizer(Class<T> clazz,
			Locale locale) {
		this (clazz, locale, new ExtendedResourceBundleControl ());
	}

	public Internationalizer(Class<T> clazz,
			ResourceBundle.Control resourceBundleControl) {
		this (clazz, Locale.getDefault (), resourceBundleControl);
	}

	public Internationalizer(Class<T> clazz) {
		this (clazz, Locale.getDefault ());
	}

	/** @see MessageFormat#format(Object) */
	public String format(T key,
			Object... args) {
		MessageFormat messageFormat = this.messageFormat[key.ordinal ()];
		synchronized (messageFormat) {
			return messageFormat.format (args);
		}
	}

	public String getPattern(T key) {
		return this.pattern[key.ordinal ()];
	}

	@Override
	public String toString() {
		return this.description;
	}
}
