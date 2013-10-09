package com.analyticobjects.digitalsafe.exceptions;

/**
 * InvalidPassphraseException to indicate the password specified is invalid.
 *
 * @author Joel Bondurant
 * @since 2013.09
 */
public class InvalidPassphraseException extends Exception {

	public InvalidPassphraseException() {
		super("Password was invalid.");
	}
}
