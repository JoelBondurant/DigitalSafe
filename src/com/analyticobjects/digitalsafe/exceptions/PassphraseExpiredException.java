package com.analyticobjects.digitalsafe.exceptions;

/**
 * PassphraseExpiredException to indicate the password memory has expired.
 *
 * @author Joel Bondurant
 * @since 2013.09
 */
public class PassphraseExpiredException extends Exception {

	public PassphraseExpiredException() {
		super("Password memory expired.");
	}
}
