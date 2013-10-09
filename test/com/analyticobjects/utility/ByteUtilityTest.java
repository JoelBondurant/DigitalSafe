package com.analyticobjects.utility;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author joel
 */
public class ByteUtilityTest {
	
	public ByteUtilityTest() {
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
	 * Test of toHexString method, of class ByteUtility.
	 */
	@Test
	public void testToHexString() {
		System.out.println("toHexString");
	}

	/**
	 * Test of toHexString0x method, of class ByteUtility.
	 */
	@Test
	public void testToHexString0x() {
		System.out.println("toHexString0x");
	}

	/**
	 * Test of concatenate method, of class ByteUtility.
	 */
	@Test
	public void testConcatenate() {
		System.out.println("concatenate");
	}

	/**
	 * Test of readFully method, of class ByteUtility.
	 */
	@Test
	public void testReadFully_Path() throws Exception {
		System.out.println("readFully");
	}

	/**
	 * Test of readFully method, of class ByteUtility.
	 */
	@Test
	public void testReadFully_InputStream() throws Exception {
		System.out.println("readFully");
	}

	/**
	 * Test of writeFully method, of class ByteUtility.
	 */
	@Test
	public void testWriteFully() throws Exception {
		System.out.println("writeFully");
		Path filePath = Paths.get("./test/com/analyticobjects/utility/ByteUtility.testWriteFully.txt");
		byte[] bytes = "ByteUtility.testWriteFully".getBytes();
		ByteUtility.writeFully(filePath, bytes);
	}
	
}
