package net.aeten.core.util;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jcip.annotations.ThreadSafe;

/**
 * @author Thomas Pérennou
 * 
 * @see Pattern
 * @see Matcher
 * @see ExtendedResourceBundleControl
 */
@ThreadSafe
public class RegexBuilder<T extends Enum<?>> {
	private final String description;
	private final Pattern[] pattern;

	public RegexBuilder(Class<T> clazz,
			Locale locale,
			ResourceBundle.Control resourceBundleControl) {
		description = "RegexBuilder for " + clazz.getName () + ", with locale " + locale;
		final T[] enumConstants = clazz.getEnumConstants ();
		ResourceBundle resourceBundle = ResourceBundle.getBundle (clazz.getName (), locale, clazz.getClassLoader (), resourceBundleControl);
		pattern = new Pattern[enumConstants.length];
		for (int i = 0; i < enumConstants.length; i++) {
			pattern[i] = Pattern.compile (resourceBundle.getString (enumConstants[i].name ()));
		}
	}

	public RegexBuilder(Class<T> clazz,
			Locale locale) {
		this (clazz, locale, new ExtendedResourceBundleControl ());
	}

	public RegexBuilder(Class<T> clazz,
			ResourceBundle.Control resourceBundleControl) {
		this (clazz, Locale.getDefault (), resourceBundleControl);
	}

	public RegexBuilder(Class<T> clazz) {
		this (clazz, Locale.getDefault ());
	}

	/** @see Pattern#matcher(CharSequence) */
	public Matcher matcher(T key,
			CharSequence input,
			Object... args) {
		return pattern[key.ordinal ()].matcher (input);
	}

	public Pattern getPattern(T key) {
		return pattern[key.ordinal ()];
	}

	@Override
	public String toString() {
		return description;
	}
}
