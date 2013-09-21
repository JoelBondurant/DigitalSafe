package com.analyticobjects.digitalsafe;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class wrapper for the note collection.
 * seconds.
 * @author Joel Bondurant
 * @since 2013.08
 */
class NoteBook {
    
    private static NoteBook singleton;
    static final String BLANK = "";
    private final ScheduledExecutorService executor;
    private String password;
    private int secondsToCachePassword;
    private int secondsToClearClipboard;
    static final String RESET = "RESET";
    private final String PASSWORD_SALT = "abcDEF1234!@#$";
    
    
    private NoteBook() {
        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.password = BLANK;
        this.secondsToCachePassword = 120;
        this.secondsToClearClipboard = 120;
    }
    
    static synchronized NoteBook getInstance() {
        if (singleton == null) {
            singleton = new NoteBook();
        }
        return singleton;
    }
    
    String getPassword() throws PasswordExpiredException {
        if (password.isEmpty()) {
            throw new PasswordExpiredException();
        }
        return this.password;
    }
    
    void setPassword(String password) throws InvalidPasswordException {
        StringBuilder sb = new StringBuilder(password);
        sb.append(PASSWORD_SALT);
        sb.append(sb.toString().toLowerCase());
        sb.append(sb.toString().toUpperCase());
        sb.append(sb.reverse().toString());
        password = sb.toString();
        
        this.executor.schedule(new BlankPasswordTask(), secondsToCachePassword, TimeUnit.SECONDS);
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] hashword = sha256.digest(password.getBytes());
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
            SecureDatabase.getMessage("");
        } catch (Exception ex) {
            blankPassword();
            throw new InvalidPasswordException();
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
    
    private class ClearClipBoardTask implements Runnable {
        @Override
        public void run() {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(""), null);
        }
    }
    
    /**
     * NoteBook as a light static wrap of SecureDatabase.
     */
    String getMessage(String name) throws PasswordExpiredException {
        String message = SecureDatabase.getMessage(name);
        toClipboard(message);
        return obscureText(message);
    }
    
    String getMessageFuzzy(String namePart) throws PasswordExpiredException {
        String message = SecureDatabase.getMessageFuzzy(namePart);
        toClipboard(message);
        return obscureText(message);
    }
    
    private String obscureText(String text) {
        return text.replaceAll(".", "*");
    }

    List<String> find(String namePart) throws PasswordExpiredException {
        return SecureDatabase.find(namePart);
    }
    
    void putNote(Note newNote) throws PasswordExpiredException {
        SecureDatabase.putNote(newNote);
    }
    
    void reset() throws PasswordExpiredException {
        SecureDatabase.reset();
    }
    
    private void toClipboard(String text) {
        this.executor.schedule(new ClearClipBoardTask(), secondsToClearClipboard, TimeUnit.SECONDS);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
    }
    
    /*
     * Exceptions.
     */
    class PasswordExpiredException extends Exception {
        PasswordExpiredException() {
            super("Password memory expired.");
        }
    }

    class InvalidPasswordException extends Exception {
        InvalidPasswordException() {
            super("Password was invalid.");
        }
    }
    
    
    @Override
    protected void finalize() throws Throwable {
        try {
            (new ClearClipBoardTask()).run();
        } catch (Exception ex) {
            super.finalize();
        }
    }
    
}
