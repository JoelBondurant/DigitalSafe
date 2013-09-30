package com.analyticobjects.digitalsafe;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for Crypto.
 * @author Joel Bondurant
 * @version 2013.09
 */
public class CryptoTest {
    
    public CryptoTest() {
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
     * Test of mix method, of class Crypto.
     */
    @Test
    public void testMix() {
        System.out.println("mix");
        String[] mixMeArray = {"This class implements an output stream in which the data is written into a byte array.", 
            "somethingshort",
            "something14564894165489with7778334lots089238590numbers",
            "js890ujuiOJ09j089sjio9)(jidf90i9H(U#*(IYDFHIUHIOSDFiomjdsjfkkj89s89f89hj"};
        for (String mixMe : mixMeArray) {
            print(mixMe);
            print(Crypto.mix(mixMe));
            String mixed = Crypto.mix(mixMe);
            assertEquals(mixMe.length(), mixed.length());
            assertFalse(mixMe.equals(mixed));
        }
        
    }

    private void print(String str) {
        System.out.println(str);
    }
    
}
