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
 * @author massi
 *
 */
public class CSVHandler implements Closeable{

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
		this(path, ",", "UTF-8", header);
	}
	
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
