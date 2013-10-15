package com.analyticobjects.digitalsafe.res;

import com.analyticobjects.utility.StringUtility;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class to help load resources.
 *
 * @author Joel Bondurant
 * @since 2013.10
 */
public class ResourceLoader {

	public ResourceLoader() {
	}
	
	public static URL getResourceURL(String resourceName) {
		return ResourceLoader.class.getResource(resourceName);
	}
	
	public static InputStream getResourceAsInputStream(String resourceName) {
		return ResourceLoader.class.getResourceAsStream(resourceName);
	}
	
	public static ResourceBundle getResourceBundle(String resourceName) {
		return ResourceBundle.getBundle(resourceName);
	}
	
	public static Properties getProperties(String propertiesName) throws IOException {
		Properties properties = new Properties();
		InputStream resourceAsInputStream = getResourceAsInputStream(propertiesName + ".properties");
		try {
			properties.load(resourceAsInputStream);
		} catch (IOException ex) {
			Logger.getLogger(ResourceLoader.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
			throw ex;
		}
		return properties;
	}
	
	public static String getProperty(String propertiesName, String propertyName) throws IOException {
		return getProperty(propertiesName, propertyName, "");
	}
	
	public static String getProperty(String propertiesName, String propertyName, String propertyDefault) throws IOException {
		try {
			return getProperties(propertiesName).getProperty(propertyName, propertyDefault);
		} catch (IOException ex) {
			String msg = StringUtility.join(", ", "propertiesName", "propertyName", "propertyDefault");
			msg = msg + "\n" + StringUtility.join(", ", propertiesName, propertyName, propertyDefault);
			Logger.getLogger(ResourceLoader.class.getName()).log(Level.SEVERE, msg);
			Logger.getLogger(ResourceLoader.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
			throw ex;
		}
	}

	public static int getPropertyAsInt(String propertiesName, String propertyName) throws IOException {
		String strValue = getProperty(propertiesName, propertyName);
		if (strValue.isEmpty()) {
			return 0;
		}
		return Integer.parseInt(strValue);
	}
	
	public static InetAddress getPropertyAsInetAddress(String propertiesName, String propertyName) throws IOException {
		String strValue = getProperty(propertiesName, propertyName);
		if (strValue.isEmpty()) {
			return null;
		}
		return InetAddress.getByName(strValue);
	}

	
}
