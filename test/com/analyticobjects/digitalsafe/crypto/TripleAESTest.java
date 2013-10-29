package com.analyticobjects.digitalsafe.crypto;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for TripleAES.
 * 
 * @author Joel Bondurant
 * @since 2013.10
 */
public class TripleAESTest {
	
	public TripleAESTest() {
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

	@Test
	public void testEncryptonBitDistribution() throws Exception {
		// TODO: Take a byte block and do a ton of encryptions with single bit
		// deviations and average the number of bits changed in the results.
		// Results should be within a tight bound of half the bits flipped.
	}

	@Test
	public void testEncrypt() throws Exception {
		System.out.println("testEncrypt::");
		List<String> testMessages = new LinkedList<>();
		testMessages.add("");
		testMessages.add("Short test message.");
		String aTestMessage = "This is a test message from the emergency broadcasting system.\n";
		aTestMessage += "This is only a test. If this were not a test, emergency information would\n";
		aTestMessage += "follow. This concludes the test of the emergency broadcasting system.";
		testMessages.add(aTestMessage);
		testMessages.add(UUID.randomUUID().toString());
		Passphrase passphrase = new Passphrase("TEST_PASSPHRASE");
		for (String testMessage : testMessages) {
			System.out.println("\tEncrypt+Decrypt>" + testMessage);
			byte[] inputBytes = testMessage.getBytes();
			byte[] result = TripleAES.decrypt(passphrase, TripleAES.encrypt(passphrase, inputBytes));
			assertArrayEquals(inputBytes, result);
		}
	}
	
	
}
