package com.analyticobjects.digitalsafe.res;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * A test case holder for ResourceLoader.
 *
 * @author Joel Bondurant
 * @since 2013.10
 */
public class ResourceLoaderTest {
	
	public ResourceLoaderTest() {
	}
	
	@BeforeClass
	public static void setUpClass() {
	}
	
	@AfterClass
	public static void tearDownClass() {
	}
	
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
	}

	/**
	 * Test of getResourceBundle method, of class ResourceLoader.
	 */
	@Test
	public void testGetResourceBundle() {
		System.out.println("testGetResourceBundle");
		String resourceName = "strings";
		String propertyName = "APP_NAME";
		String expected = "DigitalSafe";
		ResourceBundle rb = ResourceLoader.getResourceBundle(resourceName);
		String actual = rb.getString(propertyName);
		System.out.printf("Expected / Actual = %s / %s \n", expected, actual);
		assertEquals(expected, actual);
	}
	
}
