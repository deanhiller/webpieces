package org.webpieces.ctx.api;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.webpieces.util.file.ResourceBundleUtf8;

public class Messages {

	private String bundleName;
	private String globalBundleName;

	public Messages(String bundleName, String globalBundleName) {
		this.bundleName = bundleName;
		this.globalBundleName = globalBundleName;
	}
	
	public String get(String key, Locale locale) {
		//TODO: We need to fix this so we are not throwing exceptions when bundles are not found
		if(bundleName != null) {
			try {
				ResourceBundle b = ResourceBundleUtf8.load(bundleName, locale);
				String value = b.getString(key);
				if(value != null)
					return value;
			} catch(MissingResourceException e) {}
		}
		
		try {
			ResourceBundle global = ResourceBundleUtf8.load(globalBundleName, locale);
			return global.getString(key);
		} catch(MissingResourceException e) {
			return null;
		}
	}
}
