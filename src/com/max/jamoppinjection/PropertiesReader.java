package com.max.jamoppinjection;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesReader {
	private String propertiesFileName;
	
	public PropertiesReader(String propFileName) {
		propertiesFileName = propFileName;
	}

	/**
	 * Returns the value of the given key from the specified properties file. If the key is not
	 * found, the empty string is returned.
	 * @param key
	 * @return
	 */
	public String getPropValue(String key) throws IOException {
		InputStream in = null;
		String result = "";
		
		try {
			Properties prop = new Properties();
			in = getClass().getClassLoader().getResourceAsStream(propertiesFileName);
			
			if (in != null) {
				prop.load(in);
			} else {
				result = "";
			}
			
			result = prop.getProperty(key);
		} catch (Exception e) {
			in.close();
		} 

		return result;
	}
}
