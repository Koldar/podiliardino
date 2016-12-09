package com.massimobono.podiliardino.util;

import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class I18N {
	
	private static final Logger LOG = LogManager.getLogger(I18N.class);
	/**
	 * the name of the package (+ properties base name) where the translations are placed
	 */
	private static final String I18N_QUALIFIED_NAME = "com.massimobono.podiliardino.i18n.Lang";

	/**
	 * Access to the translations for different languages
	 */
	private static ResourceBundle i18n;
	
	public static void set(String lang) {
		LOG.info("Language set to {}", lang);
		i18n = ResourceBundle.getBundle(I18N_QUALIFIED_NAME, Locale.forLanguageTag(lang));
	}
	
	public static ResourceBundle get() {
		return i18n;
	}
	
	public static String get(String key) {
		return i18n.getString(key);
	}
	
	
}
