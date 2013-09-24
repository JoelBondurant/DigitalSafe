package com.analyticobjects.digitalsafe.exceptions;

/**
 * PasswordExpiredException to indicate the password memory has expired.
 * 
 * @author Joel Bondurant
 * @since 2013.09
 */
public class PasswordExpiredException extends Exception {
    public PasswordExpiredException() {
        super("Password memory expired.");
    }
}
