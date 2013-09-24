package com.analyticobjects.digitalsafe.containers;

import java.util.Date;

/**
 * A note type to store passwords.
 * @author Joel Bondurant
 * @since 2013.09
 */
public class PasswordNote extends Note {
    
    private String userName;
    private String password;
    private String url;
    private Date expirationTime;

    public PasswordNote(String title, String message) {
        super(title, message);
    }
    
}
