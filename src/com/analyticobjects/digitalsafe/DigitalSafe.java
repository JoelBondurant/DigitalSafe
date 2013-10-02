package com.analyticobjects.digitalsafe;

import com.analyticobjects.digitalsafe.exceptions.InvalidPasswordException;
import com.analyticobjects.digitalsafe.exceptions.PasswordExpiredException;
import com.analyticobjects.digitalsafe.containers.NoteBook;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
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
    private byte[] password;
    private int secondsToCachePassword;
    
    static final String RESET = "RESET";
    private final String PASSWORD_SALT = "abcDEF1234!@#$";
    private final int PASSWORD_ITERATIONS = 1001001;
    private static final Level LOGGING_LEVEL = Level.ALL;
    
    
    private DigitalSafe() {
        this.setLoggingLevelGlobally(LOGGING_LEVEL);
        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.password = null;
        this.secondsToCachePassword = 60*5;
        SecureDatabase.ensureDB();
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
    
    byte[] getPassword() throws PasswordExpiredException {
        if (this.password == null) {
            throw new PasswordExpiredException();
        }
        return this.password;
    }
    
    public boolean isUnlocked() {
        return (this.password != null);
    }
    
    public void lock() {
        this.blankPassword();
    }
    
    private void validatePassword(String password) throws InvalidPasswordException {
        if (password == null || password.length() < 6) {
            throw new InvalidPasswordException();
        }
    }
    
    public void setPassword(String password) throws InvalidPasswordException {
        validatePassword(password);
        this.executor.schedule(new BlankPasswordTask(), secondsToCachePassword, TimeUnit.SECONDS);
        try {
            this.password = Crypto.oneWayHash(password, PASSWORD_SALT, PASSWORD_ITERATIONS);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
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
        if (this.password != null) {
            Arrays.fill(this.password, (byte) 0b00000000);
        }
        this.password = null;
        System.gc();
    }
    
    private class BlankPasswordTask implements Runnable {
        @Override
        public void run() {
            blankPassword();
        }
    }

    public void reset() {
        SecureDatabase.reset();
    }
    
    public static NoteBook getNoteBook() throws PasswordExpiredException {
        return SecureDatabase.getNoteBook();
    }
    
    public static void commitNoteBook(NoteBook noteBook) throws PasswordExpiredException {
        SecureDatabase.commitNoteBook(noteBook);
    }
    
    public static byte[] loadFile(String fileHash) throws PasswordExpiredException, IOException {
        return SecureDatabase.loadFile(fileHash);
    }

    /**
     * When this class is garbage collected on system exit, we clear the password first for good measure.
     * @throws Throwable 
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            super.finalize();
            this.blankPassword();
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).severe(ex.getMessage());
        }
    }
    
}
