package net.aeten.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link ResourceBundle.Control} subclass that allows loading of bundles in XML
 * format and {@link Charset} defined properties files (*.charset.properties
 * where charset is the charset name). The bundles are searched first as Java
 * classes, then as properties files (these two methods are the standard search
 * mechanism of ResourceBundle), then as XML properties files. The filename
 * extension of the XML properties files is assumed to be *.properties.xml
 * 
 * @author Thomas Pérennou
 */
public class ExtendedResourceBundleControl extends
		Control {
	private static final String FORMAT_PROPERTY_SUFFIX = "properties";
	private static final String FORMAT_XML_SUFFIX = "properties.xml";
	private static final String FORMAT_XML = "java." + FORMAT_XML_SUFFIX;
	private static final List <String> FORMATS;

	private static String getFormatProperty (String charset) {
		return "java." + charset + "." + FORMAT_PROPERTY_SUFFIX;
	}

	static {
		List <String> formats = new ArrayList <String> (FORMAT_DEFAULT);
		formats.add (FORMAT_XML);
		for (String charset: Charset.availableCharsets ().keySet ()) {
			formats.add (getFormatProperty (charset));
		}
		FORMATS = Collections.unmodifiableList (formats);
	}

	@Override
	public List <String> getFormats (String baseName) {
		return FORMATS;
	}

	@Override
	public ResourceBundle newBundle (String baseName,
												Locale locale,
												String format,
												ClassLoader loader,
												boolean reload) throws IllegalAccessException,
																	InstantiationException,
																	IOException {
		format = format.toLowerCase ();
		if (!FORMAT_XML.equals (format) && !format.endsWith (FORMAT_PROPERTY_SUFFIX)) return super.newBundle (baseName, locale, format, loader, reload);

		String bundleName = toBundleName (baseName, locale);

		Pattern pattern = Pattern.compile ("java\\.(.+)\\." + FORMAT_PROPERTY_SUFFIX);
		Matcher matcher = pattern.matcher (format);
		String charset = matcher.matches ()? matcher.group (1): null;
		if (format.endsWith (FORMAT_PROPERTY_SUFFIX) && (charset == null)) {
			return super.newBundle (baseName, locale, format, loader, reload);
		}

		String resourceName = toResourceName (bundleName, format.endsWith (FORMAT_PROPERTY_SUFFIX)? ((charset == null)? "": (charset + ".") + FORMAT_PROPERTY_SUFFIX): FORMAT_XML_SUFFIX);
		final URL resourceURL = loader.getResource (resourceName);
		if (resourceURL == null) return null;

		InputStream stream = getResourceInputStream (resourceURL, reload);

		try {
			if (format.endsWith (FORMAT_PROPERTY_SUFFIX)) {
				PropertyResourceBundle result = new PropertyResourceBundle ();
				result.load (stream, charset);
				return result;
			}
			PropertyXMLResourceBundle result = new PropertyXMLResourceBundle ();
			result.load (stream);
			return result;
		} finally {
			stream.close ();
		}
	}

	private InputStream getResourceInputStream (	final URL resourceURL,
																boolean reload) throws IOException {
		if (!reload) return resourceURL.openStream ();

		try {
			// This permission has already been checked by
			// ClassLoader.getResource(String), which will return null
			// in case the code has not enough privileges.
			return AccessController.doPrivileged (new PrivilegedExceptionAction <InputStream> () {
				public InputStream run () throws IOException {
					URLConnection connection = resourceURL.openConnection ();
					connection.setUseCaches (false);
					return connection.getInputStream ();
				}
			});
		} catch (PrivilegedActionException x) {
			throw (IOException) x.getCause ();
		}
	}

	/**
	 * ResourceBundle that loads definitions from an XML properties file.
	 */
	public static class PropertyXMLResourceBundle extends
			ResourceBundle {
		private final Properties properties = new Properties ();

		public void load (InputStream stream) throws IOException {
			properties.loadFromXML (stream);
		}

		@Override
		protected Object handleGetObject (String key) {
			return properties.getProperty (key);
		}

		@Override
		public Enumeration <String> getKeys () {
			final Enumeration <Object> keys = properties.keys ();
			return new Enumeration <String> () {
				public boolean hasMoreElements () {
					return keys.hasMoreElements ();
				}

				public String nextElement () {
					return (String) keys.nextElement ();
				}
			};
		}
	}

	/**
	 * ResourceBundle that loads definitions from an properties file.
	 */
	public static class PropertyResourceBundle extends
			ResourceBundle {
		private final Properties properties = new Properties ();

		public void load (InputStream stream,
								String charset) throws IOException {
			properties.load (new InputStreamReader (stream, Charset.forName (charset)));
		}

		@Override
		protected Object handleGetObject (String key) {
			return properties.getProperty (key);
		}

		@Override
		public Enumeration <String> getKeys () {
			final Enumeration <Object> keys = properties.keys ();
			return new Enumeration <String> () {
				public boolean hasMoreElements () {
					return keys.hasMoreElements ();
				}

				public String nextElement () {
					return (String) keys.nextElement ();
				}
			};
		}
	}
}