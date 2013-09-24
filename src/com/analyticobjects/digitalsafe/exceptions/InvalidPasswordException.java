package com.analyticobjects.digitalsafe.exceptions;

/**
 * InvalidPasswordException to indicate the password specified is invalid.
 * 
 * @author Joel Bondurant
 * @since 2013.09
 */
public class InvalidPasswordException extends Exception {
    public InvalidPasswordException() {
        super("Password was invalid.");
    }
}