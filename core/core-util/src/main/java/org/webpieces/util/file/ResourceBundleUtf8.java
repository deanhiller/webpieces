package org.webpieces.util.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.function.Supplier;

import org.digitalforge.sneakythrow.SneakyThrow;

public class ResourceBundleUtf8 extends Control {

	/**
	 * Use this or have to use the nasty nativetoascii tool to convert property files.  This allows one to just
	 * write the property file in their native language without having to then run it through some build time 
	 * converter (which then by the way has to be converted back again...seems like a waste)
	 * 
	 * @param baseName
	 * @param locale
	 * @return
	 */
	public static ResourceBundle load(String baseName, Locale locale) {
		return ResourceBundle.getBundle(baseName, locale, new ResourceBundleUtf8());
	}

	//from http://stackoverflow.com/questions/4659929/how-to-use-utf-8-in-resource-properties-with-resourcebundle
	//but modified to use try with resource which has way better failure semantics when input stream and close both fail as
	//it sends back the first exception which is way more important
	public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
			throws IllegalAccessException, InstantiationException, IOException {
		// The below is a copy of the default implementation.
		String bundleName = toBundleName(baseName, locale);
		String resourceName = toResourceName(bundleName, "properties");
		ResourceBundle bundle = null;
		if (reload) {
			URL url = loader.getResource(resourceName);
			if (url != null) {
				URLConnection connection = url.openConnection();
				if (connection != null) {
					connection.setUseCaches(false);
					bundle = readBundle(() -> getInputStream(connection));
				}
			}
		} else {
			bundle = readBundle(() -> loader.getResourceAsStream(resourceName));
		}
		return bundle;
	}
	
	private InputStream getInputStream(URLConnection connection) {
		try {
			return connection.getInputStream();
		} catch (IOException e) {
			throw SneakyThrow.sneak(e);
		}
	}

	private ResourceBundle readBundle(Supplier<InputStream> supplier) throws UnsupportedEncodingException, IOException {
		try (InputStream stream = supplier.get();
			Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8.name())) {
			// Only this line is changed to make it to read properties files as UTF-8.
            return new PropertyResourceBundle(reader);
		}
	}
	
}
