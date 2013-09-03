package com.analyticobjects.digitalsafe;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * A class to hide all the note database decrypted data.
 * 
 * @author Joel Bondurant
 * @since 2013.08
 */
final class SecureDatabase {
    
    private static final String FILE_NAME = "textSafe.safe";
    private static final String SWAP_NAME = "textSafe.swap";
    private static final String KEYGEN = "PBKDF2WithHmacSHA1";
    private static final String AES = "AES/CBC/PKCS5Padding";
    private static final int AES_KEY_LENGTH = 128;
    private static final String BLOWFISH = "Blowfish/CBC/PKCS5Padding";
    private static final int BLOWFISH_KEY_LENGTH = 128;
    
    private SecureDatabase(){}; // no, just no.
    
    private static void prepDB(String name) {
        Path dbPath = filePath(name);
        File dbFile = dbPath.toFile();
        if (!dbFile.exists()) {
            try {
                dbFile.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }
    
    static void prepFiles() {
        prepDB(FILE_NAME);
        prepDB(SWAP_NAME);
    }
    
    static void reset() {
        resetDB(FILE_NAME);
        resetDB(SWAP_NAME);
    }
    
    private static void resetDB(String name) {
        Path dbPath = filePath(name);
        File dbFile = dbPath.toFile();
        if (dbFile.exists()) {
            dbFile.delete();
        }
        prepDB(name);
    }
    
    private static Map<String, Note> getNoteMap() throws NoteBook.PasswordExpiredException {
        Map<String, Note> noteMap = new HashMap<>();
        List<Note> notes;
        try {
            byte[] textSafe = decrypt(Files.readAllBytes(filePath(FILE_NAME)));
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(textSafe));
            notes = (ArrayList<Note>) ois.readObject();
            for (Note note : notes) {
                noteMap.put(note.getName(), note);
            }
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return noteMap;
    }
    
    private static void commitNoteMap(Map<String, Note> noteMap) throws NoteBook.PasswordExpiredException {
        List<Note> notes = new ArrayList<>(noteMap.values());
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(notes);
            oos.flush();
            byte[] textSafe = encrypt(bos.toByteArray());
            Files.write(filePath(FILE_NAME), textSafe);
            Files.write(filePath(SWAP_NAME), textSafe);
        } catch (IOException ex) {
            Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    
    private static Path filePath(String fileName) {
        return FileSystems.getDefault().getPath(".", fileName);
    }
    
    private static synchronized SecretKey keyGenAES() throws NoteBook.PasswordExpiredException {
        SecretKey key = null;
        try {
            byte[] salt = "salty mcbutter salts what what".getBytes();
            int iterations = 97447;
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEYGEN);
            PBEKeySpec pbeKeySpec = new PBEKeySpec(NoteBook.getInstance().getPassword().toCharArray(), salt, iterations, AES_KEY_LENGTH);
            key = keyFactory.generateSecret(pbeKeySpec);
            key = new SecretKeySpec(key.getEncoded(), "AES");
            Thread.sleep(100); // Yes, I'm calling sleep in a synchronized block on purpose.
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InterruptedException ex) {
            Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return key;
    }
    
    private static synchronized SecretKey keyGenBlowfish() throws NoteBook.PasswordExpiredException {
        SecretKey key = null;
        try {
            byte[] salt = "fish blows a what what".getBytes();
            int iterations = 90443;
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEYGEN);
            key = keyFactory.generateSecret(new PBEKeySpec(NoteBook.getInstance().getPassword().toCharArray(), salt, iterations, BLOWFISH_KEY_LENGTH));
            key = new SecretKeySpec(key.getEncoded(), "Blowfish");
            Thread.sleep(100); // Yes, I'm calling sleep in a synchronized block on purpose.
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InterruptedException ex) {
            Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return key;
    }
    
    private static IvParameterSpec ivParameterSpec8() {
        byte[] iv = { 1, 0, 1, 0, 1, 0, 0, 1};
        return new IvParameterSpec(iv);
    }
    
    private static IvParameterSpec ivParameterSpec16() {
        byte[] iv = { 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0 };
        return new IvParameterSpec(iv);
    }
    
    private static byte[] decrypt(byte[] encryptedData) throws NoteBook.PasswordExpiredException {
        byte[] unEncryptedData1;
        byte[] unEncryptedData2 = null;
        try {
            Cipher aes = Cipher.getInstance(AES);
            SecretKey keyAES = keyGenAES();
            aes.init(Cipher.DECRYPT_MODE, keyAES, ivParameterSpec16());
            unEncryptedData1 = aes.doFinal(encryptedData);
            Cipher blowfish = Cipher.getInstance(BLOWFISH);
            SecretKey keyBlowfish = keyGenBlowfish();
            blowfish.init(Cipher.DECRYPT_MODE, keyBlowfish, ivParameterSpec8());
            unEncryptedData2 = blowfish.doFinal(unEncryptedData1);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | InvalidKeyException ex) {
            Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (InvalidAlgorithmParameterException | IllegalBlockSizeException ex) {
            Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return unEncryptedData2;
    }
    
    private static byte[] encrypt(byte[] unencryptedData) throws NoteBook.PasswordExpiredException {
        byte[] encryptedData1;
        byte[] encryptedData2 = null;
        try {
            Cipher blowfish = Cipher.getInstance(BLOWFISH);
            blowfish.init(Cipher.ENCRYPT_MODE, keyGenBlowfish(), ivParameterSpec8());
            encryptedData1 = blowfish.doFinal(unencryptedData);
            Cipher aes = Cipher.getInstance(AES);
            aes.init(Cipher.ENCRYPT_MODE, keyGenAES(), ivParameterSpec16());
            encryptedData2 = aes.doFinal(encryptedData1);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException ex) {
            Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (IllegalBlockSizeException | InvalidAlgorithmParameterException ex) {
            Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return encryptedData2;
    }
    
    static String getMessage(String name) throws NoteBook.PasswordExpiredException {
        Map<String, Note> notes = getNoteMap();
        if (notes.containsKey(name)) {
            return notes.get(name).getMessage();
        }
        return NoteBook.BLANK;
    }
    
    static String getMessageFuzzy(String namePart) throws NoteBook.PasswordExpiredException {
        for (Note note : getNoteMap().values()) {
            if (note.fullText().contains(namePart)) {
                return note.getMessage();
            }
        }
        return NoteBook.BLANK;
    }
    
    static List<String> find(String namePart) throws NoteBook.PasswordExpiredException {
        List<String> names = new ArrayList<>();
        for (Note note : getNoteMap().values()) {
            if (note.fullText().matches(namePart)) {
                names.add(note.getName());
            }
        }
        return names;
    }
    
    static void putNote(Note newNote) throws NoteBook.PasswordExpiredException {
        Map<String, Note> noteMap = getNoteMap();
        noteMap.put(newNote.getName(), newNote);
        commitNoteMap(noteMap);
    }
    
}
