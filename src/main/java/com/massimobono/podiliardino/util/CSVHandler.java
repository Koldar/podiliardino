package com.massimobono.podiliardino.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A utility class used to easily write CSV files
 * 
 * CSV files are text files formatted in to columns; an example might be:
 * 
 * <pre><code>
 * 	sep=;
 * 	COLUMN1;COLUMN2;COLUMN3;COLUMN4
 * 	1;2;3;4
 *  5;6;7;8
 *  9;10;11;12
 * </code></pre>
 * 
 * CSV are divided into 3 sectors:
 * <ol>
 * 	<li>an option section, where you can establish some option values. For example you may set the delimiter of each column (in the example ";");</li>
 * 	<li>an header where you define the columns of the CSV</li>
 * 	<li>a list fo rows: for each column, you define a single value</li>
 * </ol>
 * 
 * <h1>How to use the class</h1>
 * 
 * Using this class is easy: you create a new {@link CSVHandler}, you setup the options you want to set (recommended only if you want to improve
 * CSV portability) and then you call {@link #printRow(String...)} as many times as rows you want to add.
 * 
 * <pre><code>
 *  header = new String[] {"X", "Y", "Z"};
 *  this.csvFile = new File(...);
 * 	try (CSVHandler csvHandler = new CSVHandler(this.csvFile.getAbsolutePath(), header)) {
			csvHandler.addOption("sep", CSVHandler.DEFAULT_DELIMITER);
			for (int i=0; i<points3D.size(); i++) {
				Point3D v = points3D.get(i)
				csvHandler.printRow(v.getX(), v.getY(), v.getZ());
			}
		}
 * </code></pre>
 * 
 * 
 * @author massi
 * @version 1.0
 *
 */
public class CSVHandler implements Closeable{

	public static final String DEFAULT_DELIMITER = ";"; 
	
	private File file;
	private PrintWriter pw;
	private String delimiter;
	private Map<String,String> options;
	private String[] header;
	
	private boolean headerPrinted;
	private boolean optionsPrinted;
	
	public CSVHandler(String path, String delimiter, String encoding, String... header) throws IOException {
		this.file = new File(path);
		this.pw = new PrintWriter(this.file, encoding);
		this.options = new HashMap<>();
		this.header = header;
		this.setDelimiter(delimiter);
		
		this.headerPrinted = false;
		this.optionsPrinted = false;
	}
	
	public CSVHandler(String path, String... header) throws IOException {
		this(path, DEFAULT_DELIMITER, "UTF-8", header);
	}
	
	/**
	 * Adds an option in the CSV
	 * 
	 * Available options are:
	 * <ul>
	 * 	<li>sep: the column sepearator to use in the csv</li>
	 * </ul>
	 * 
	 * @param key the option name
	 * @param value the option value
	 */
	public void addOption(String key, String value) {
		this.options.put(key, value);
	}
	
	public void removeOption(String key) {
		this.options.remove(key);
	}
	
	/**
	 * @return the delimiter
	 */
	public final String getDelimiter() {
		return delimiter;
	}

	/**
	 * @param delimiter the delimiter to set
	 */
	public final void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	private void tryPrintingHeader(){
		if (!this.headerPrinted) {
			this.pw.println(String.join(this.delimiter, this.header));
			this.headerPrinted = true;
		}
	}

	private void tryPrintingOptions() {
		if (!this.optionsPrinted) {
			for (Entry<String, String> entry : this.options.entrySet()) {
				pw.println(String.format("%s=%s", entry.getKey(), entry.getValue()));
			}
			this.optionsPrinted = true;
		}
	}
	
	/**
	 * Print a row inside the CSV
	 * 
	 * @param values the values of the row you want to add
	 * @throws IOException if the <tt>values</tt> length mismatches with the length of the header you have given
	 */
	public void printRow(String... values) throws IOException{
		if (values.length != this.header.length) {
			throw new IOException("values length mismatches with header length");
		}
		this.println(String.join(this.delimiter, values));
	}

	private void println(String str){
		this.tryPrintingOptions();
		this.tryPrintingHeader();
		this.pw.println(str);
	}

	@Override
	public void close() throws IOException {
		this.tryPrintingOptions();
		this.tryPrintingHeader();
		this.pw.flush();
		this.pw.close();
	}
	
	
}
