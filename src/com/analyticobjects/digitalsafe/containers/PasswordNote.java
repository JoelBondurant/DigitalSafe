package com.analyticobjects.digitalsafe.containers;

import com.analyticobjects.digitalsafe.TimeUtility;
import java.util.Date;

/**
 * A note type to store passwords.
 * @author Joel Bondurant
 * @since 2013.09
 */
public class PasswordNote extends Note {
    
    private static final int DAYS_TO_EXPIRATION = 180;
    private String userName;
    private String password;
    private String url;
    private Date expirationTime;

    public PasswordNote(String title, String password) {
        super(title, "");
        this.userName = "";
        this.password = password;
        this.url = "";
        this.expirationTime = TimeUtility.addDays(new Date(), DAYS_TO_EXPIRATION);
    }
    
    public String getUserName() {
        return this.userName;
    }
    
    public void setUserName(String userName) {
        this.updateTime();
        this.userName = userName;
    }
    
    public String getPassword() {
        return this.password;
    }
    
    public void setPassword(String password) {
        this.updateTime();
        this.password = password;
    }
    
    public String getUrl() {
        return this.url;
    }
    
    public void setUrl(String url) {
        this.updateTime();
        this.url = url;
    }
    
    public Date getExpirationTime() {
        return this.expirationTime;
    }
    
    public void setExpirationTime(Date expirationTime) {
        this.updateTime();
        this.expirationTime = (Date) expirationTime.clone();
    }
}
