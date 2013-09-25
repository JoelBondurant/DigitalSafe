package com.analyticobjects.digitalsafe;



import com.analyticobjects.digitalsafe.exceptions.InvalidPasswordException;
import com.analyticobjects.digitalsafe.exceptions.PasswordExpiredException;
import com.analyticobjects.digitalsafe.containers.NoteBook;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


/**
 * Main console app class.
 * 
 * @author Joel Bondurant
 *  @since 2013.08
 */
public class DigitalSafe {
    
    private static DigitalSafe singletonInstance;
    static final String BLANK = "";
    private final ScheduledExecutorService executor;
    private String password;
    private int secondsToCachePassword;
    
    static final String RESET = "RESET";
    private final String PASSWORD_SALT = "abcDEF1234!@#$";
    private static final Level LOGGING_LEVEL = Level.INFO;
    
    
    private DigitalSafe() {
        this.setLoggingLevelGlobally(LOGGING_LEVEL);
        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.password = BLANK;
        this.secondsToCachePassword = 120;
        SecureDatabase.prepFiles();
    }
    
    public static final synchronized DigitalSafe getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new DigitalSafe();
        }
        return singletonInstance;
    }
    
    private void setLoggingLevelGlobally(Level loggingLevel) {
        LogManager logManager = LogManager.getLogManager();
        Enumeration<String> loggerNames = logManager.getLoggerNames();
        while (loggerNames.hasMoreElements()) {
            logManager.getLogger(loggerNames.nextElement()).setLevel(loggingLevel);
        }
    }
    
    String getPassword() throws PasswordExpiredException {
        if (password.isEmpty()) {
            throw new PasswordExpiredException();
        }
        return this.password;
    }
    
    public boolean isUnlocked() {
        return !this.password.isEmpty();
    }
    
    private void validatePassword(String password) throws InvalidPasswordException {
        if (password == null || password.length() < 6) {
            throw new InvalidPasswordException();
        }
    }
    
    public void setPassword(String password) throws InvalidPasswordException {
        validatePassword(password);
        StringBuilder sb = new StringBuilder(password);
        sb.append(PASSWORD_SALT);
        sb.append(sb.toString().toLowerCase());
        sb.append(sb.toString().toUpperCase());
        sb.append(sb.reverse().toString());
        this.executor.schedule(new BlankPasswordTask(), secondsToCachePassword, TimeUnit.SECONDS);
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] hashword = sha256.digest(sb.toString().getBytes());
            for (int i = 0; i <= 100000; i++) {
                hashword = sha256.digest(hashword);
                hashword = sha1.digest(hashword);
                hashword = md5.digest(hashword);
                hashword = sha1.digest(hashword);
                hashword = sha256.digest(hashword);
            }
            this.password = ByteUtility.toHexString(hashword);
        } catch (NoSuchAlgorithmException ex) {
            blankPassword();
            Logger.getLogger(NoteBook.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        try {
            SecureDatabase.validatePassword();
        } catch (InvalidPasswordException ex) {
            blankPassword();
            throw ex;
        }
    }
    
    void setSecondsToCachePassword(int secondsToCachePassword) {
        this.secondsToCachePassword = secondsToCachePassword;
    }
    
    void blankPassword() {
        this.password = BLANK;
        System.gc();
    }
    
    private class BlankPasswordTask implements Runnable {
        @Override
        public void run() {
            blankPassword();
        }
    }

    void reset() {
        SecureDatabase.reset();
    }
    
    NoteBook getNoteBook() throws PasswordExpiredException {
        return SecureDatabase.getNoteBook();
    }

    
}
