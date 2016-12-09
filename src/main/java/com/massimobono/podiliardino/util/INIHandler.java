package com.massimobono.podiliardino.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class that reads an ini file containing local data.
 * 
 * An INI file contains information that are not data, but they are just customization of a particular installation. For example the language used
 * to display the application is not data critical for the application, but it's just a customization. Custom skin can be another example.
 * 
 * INI files follows the following <a href="https://en.wikipedia.org/wiki/INI_file">structure</a>.
 * This implementation follows some specific rules:
 * <ol>
 * 	<li>No values can stay of a section</li>
 * 	<li>blank lines are allowed</li>
 * 	<li>spaces at the beginning and at the end of every line are trashed out</li>
 * 	<li>Sections can't be nested</li>
 * 	<li>spaces or tabulations can be put between the key, the "=" sign and the value. For example <pre>foo = bar</pre> is accepted as <pre>foo    =     bar</pre></li>
 * 	<li>sections names, key names are alphanumeric;</li>
 * 	<li>values are alphanumeric with more allowed characters: <code>" + - .<code>
 * </ol>
 * 
 * @author massi
 *
 */
public class INIHandler {
	
	private static final Pattern COMMENT_REGEX = Pattern.compile("^;.+$");
	private static final Pattern SECTION_REGEX = Pattern.compile("^\\[(?<section>[a-zA-Z0-9]+)\\]$");
	private static final Pattern VALUE_REGEX = Pattern.compile("^(?<key>[a-zA-Z0-9]+)\\s*=\\s*(?<value>[\\\"\\.\\+\\-a-zA-Z0-9]+)$");
	private File iniFile;
	/**
	 * the structure containing al lthe information inside the INI file. The first map index content by <b>section</b> while the inner map
	 * is just a key-value mapping.
	 * 
	 * For example if the ini is:
	 * <pre><code>
	 * [s1]
	 * foo=bar
	 * lang=en
	 * [s2]
	 * name=Max
	 * </code></pre>
	 * 
	 * this structure will be: <tt>{s1: {foo:bar, lang:en}, s2: {name:Max}}</tt>
	 * 
	 */
	private Map<String, Map<String,String>> content;
	/**
	 * the section we're currently exploring.
	 * <b>Can be null</b>
	 */
	private String currentSection;
	
	/**
	 * Creates a new handler on the file <tt>iniFile</tt>
	 * 
	 * @param iniFile the file containing the settings. Has to exists
	 * @param autoload true if you want to immediately load the settings inside the ini file. If you set it to false you can use {@link #load()} afterwards
	 * @throws IOException if <tt>initFile<tt> does not exist or is not a INI compliant file
	 * 
	 * @see {@link #load()}
	 */
	public INIHandler(File iniFile, boolean autoload) throws IOException {
		super();
		this.iniFile = iniFile;
		this.content = new HashMap<>();
		this.currentSection = null;
		
		if (autoload) {
			this.load();
		}
	}
	
	/**
	 * like {@link #INIHandler(File, boolean)} but accepts a filename instead of a {@link File}
	 * @param iniFileName
	 * @param autoload
	 * @throws IOException
	 */
	public INIHandler(String iniFileName, boolean autoload) throws IOException {
		this(new File(iniFileName), autoload);
	}
	
	/**
	 * like {@link #INIHandler(File, boolean)} but automatically loads the ini file
	 * @param iniFile
	 * @throws IOException
	 */
	public INIHandler(File iniFile) throws IOException {
		this(iniFile, true);
	}
	
	public void load() throws IOException {
		String line = "";
		Matcher m = null;
		try (BufferedReader br = new BufferedReader(new FileReader(this.iniFile))) {
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (COMMENT_REGEX.matcher(line).matches()){
					continue;
				}
				m = SECTION_REGEX.matcher(line);
				if (m.matches()) {
					this.content.putIfAbsent(m.group("section"), new HashMap<>());
					this.currentSection = m.group("section");
				}
				m = VALUE_REGEX.matcher(line);
				if (m.matches()) {
					if (this.currentSection == null) {
						throw new IOException("Can't add a key-value pair without a section. Please add a INI section.");
					}
					this.content.get(this.currentSection).put(m.group("key"), m.group("value"));
				}
				//other lines (like blanks) are trashed out
			}
		}
	}
	
	public Iterable<String> sections() {
		return this.content.keySet();
	}
	
	public Map<String, String> getKeyValueIn(String section) {
		return this.content.get(section);
	}
	
	public Optional<String> getString(String section, String key) {
		if (!this.content.containsKey(section)) {
			return  Optional.empty();
		}
		if (!this.content.get(section).containsKey(key)) {
			return Optional.empty();
		}
		return Optional.of(this.content.get(section).get(key));
	}
	
	public void updateIni(String section, String key, String value) throws IOException {
		this.content.get(section).put(key, value);
		
		this.saveFile();
	}
	
	/**
	 * Puts all the data inside {@link #content} inside the {@link #iniFile}
	 * 
	 * @throws FileNotFoundException
	 */
	private void saveFile() throws FileNotFoundException {
		try (PrintWriter pw = new PrintWriter(this.iniFile)) {
			for (String asection : this.content.keySet()) {
				pw.println(String.format("[%s]", asection));
				for (Entry<String, String> keyValue : this.content.get(asection).entrySet()) {
					pw.println(String.format("%s = %s", keyValue.getKey(), keyValue.getValue()));
				}
			}
		}
	}
}
