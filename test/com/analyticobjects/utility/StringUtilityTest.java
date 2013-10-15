package com.analyticobjects.utility;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A test case holder for StringUtility.
 *
 * @author Joel Bondurant
 * @since 2011.0317
 */
public class StringUtilityTest {
	
	public StringUtilityTest() {
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
	 * Test of join method, of class StringUtility.
	 */
	@Test
	public void testJoin() {
		String actual = StringUtility.join(", ", "testing", "my", "string", "joiner");
		String expected = "testing, my, string, joiner";
		Assert.assertEquals(expected, actual);
	}

	
}
