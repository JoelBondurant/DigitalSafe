package com.analyticobjects.digitalsafe.net;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests of DigitalServer.
 *
 * @author Joel Bondurant
 * @since 2013.10
 */
public class DigitalServerTest {
	
	public DigitalServerTest() {
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
	 * Test of run method, of class DigitalServer.
	 */
	@Test
	public void testRun() {
		System.out.println("run");
		DigitalServer digiServer = DigitalServer.getInstance();
		try {
			digiServer.start();
		} catch (InterruptedException ex) {
			System.out.println(ex.getLocalizedMessage());
			fail();
		}
	}
	
}
