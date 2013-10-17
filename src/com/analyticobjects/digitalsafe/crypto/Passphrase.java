package com.analyticobjects.digitalsafe.crypto;

import com.analyticobjects.digitalsafe.exceptions.InvalidPassphraseException;
import com.analyticobjects.digitalsafe.database.MasterIndex;
import com.analyticobjects.digitalsafe.exceptions.PassphraseExpiredException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class to store a passphrases. The supplied passphrase string is not stored, 
 * rather a highly hashed version of the passphrase is stored. The value is 
 * cleared from memory at a scheduled time after being set and object
 * finalization. Default passphrase cache time is 10 minutes.
 *
 * @author Joel Bondurant
 * @since 2013.08
 */
public final class Passphrase {
	
	private byte[] passphraseHash;
	private int secondsToCachePassphrase;
	private final ScheduledExecutorService executor;
	private static final String STATIC_SALT = "abcDEF1234!@#$";
	private static final int PASSWORD_ITERATIONS = 20011;
	public static final int MINIMUM_PASSPHRASE_LENGTH = 8;
	public static final int DEFAULT_CACHE_TIME_IN_SECONDS = 10 * 60;

	/**
	 * Constructs an empty passphrase.
	 */
	public Passphrase() {
		this.executor = Executors.newSingleThreadScheduledExecutor();
		this.secondsToCachePassphrase = DEFAULT_CACHE_TIME_IN_SECONDS;
		this.passphraseHash = null;
	}
	
	/**
	 * Constructs a passphrase from the supplied string.
	 * 
	 * @param passphrase A user suppled passphrase.
	 * @throws InvalidPassphraseException 
	 */
	public Passphrase(String passphrase) throws InvalidPassphraseException {
		this();
		this.setPassphrase(passphrase);
	}
	
	/**
	 * Constructs a passphrase from the supplied string with non-default 
	 * caching time specified.
	 * 
	 * @param passphrase A user suppled passphrase.
	 * @param secondsToCachePassphrase Number of seconds to store the passphrase.
	 * @throws InvalidPassphraseException 
	 */
	public Passphrase(String passphrase, int secondsToCachePassphrase) throws InvalidPassphraseException {
		this(passphrase);
		this.setSecondsToCachePassphrase(secondsToCachePassphrase);
	}


	/**
	 * Validation code for the passphrase. Crude v0.0.1 implementation 
	 * 
	 * @param passphrase 
	 * @throws InvalidPassphraseException 
	 */
	public void validatePassphrase(String passphrase) throws InvalidPassphraseException {
		if (passphrase == null || passphrase.length() < MINIMUM_PASSPHRASE_LENGTH) {
			throw new InvalidPassphraseException();
		}
	}

	/**
	 * Passphrase setter.
	 * 
	 * @param passphrase The passphrase.
	 * @throws InvalidPassphraseException 
	 */
	public final void setPassphrase(String passphrase) throws InvalidPassphraseException {
		validatePassphrase(passphrase);
		this.executor.schedule(new ClearPassphraseTask(), secondsToCachePassphrase, TimeUnit.SECONDS);
		try {
			this.passphraseHash = HashUtility.hash512(passphrase, STATIC_SALT, PASSWORD_ITERATIONS);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
			clear();
			Logger.getLogger(MasterIndex.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
		}
	}
	
	/**
	 * Passphrase (hash) getter.
	 * 
	 * @return The highly hashed version of the passphrase.
	 * @throws PassphraseExpiredException 
	 */
	public byte[] getPassphraseHash() throws PassphraseExpiredException {
		if (this.passphraseHash == null) {
			throw new PassphraseExpiredException();
		}
		return this.passphraseHash;
	}

	/**
	 * Adjust how long the passphrase will be held in memory. Default is 5 minutes.
	 *
	 * @param secondsToCachePassphrase The number of seconds to cache the passphrase.
	 */
	public final void setSecondsToCachePassphrase(int secondsToCachePassphrase) {
		this.secondsToCachePassphrase = secondsToCachePassphrase;
	}

	/**
	 * Clear the passphrase from memory.
	 */
	public void clear() {
		if (this.passphraseHash != null) {
			Arrays.fill(this.passphraseHash, (byte) 0b00000000);
		}
		this.passphraseHash = null;
		System.gc();
	}
	
	/**
	 * @return Quick check if passphrase is cleared.
	 */
	public boolean isClear() {
		return this.passphraseHash == null;
	}

	/**
	 * Boo no lambdas for another year because nobody can get security right to save their life.
	 */
	private class ClearPassphraseTask implements Runnable {

		@Override
		public void run() {
			clear();
		}
	}

	/**
	 * When this class is garbage collected, clear the password first for good measure.
	 *
	 * @throws Throwable
	 */
	@Override
	protected void finalize() throws Throwable {
		try {
			super.finalize();
			clear();
		} catch (Exception ex) {
			Logger.getLogger(this.getClass().getName()).severe(ex.getMessage());
		}
	}

}
