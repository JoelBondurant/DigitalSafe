package com.analyticobjects.digitalsafe;

import com.analyticobjects.digitalsafe.exceptions.InvalidPasswordException;
import com.analyticobjects.digitalsafe.exceptions.PasswordExpiredException;
import com.analyticobjects.digitalsafe.containers.NoteBook;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
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
    
    private static final String FILE_NAME = "digitalSafe.safe";
    private static final String SWAP_NAME = "digitalSafe.swap";
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
                Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
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
    
    static NoteBook getNoteBook() throws PasswordExpiredException {
        NoteBook notebook = null;
        try {
            byte[] digiSafe = decrypt(Files.readAllBytes(filePath(FILE_NAME)));
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(digiSafe));
            notebook = (NoteBook) ois.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(SecureDatabase.class.getName()).log(Level.FINEST, ex.getLocalizedMessage(), ex);
        }
        return notebook;
    }
    
    static void commitNoteBook(NoteBook noteBook) throws PasswordExpiredException {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(noteBook);
            oos.flush();
            byte[] textSafe = encrypt(bos.toByteArray());
            Files.write(filePath(FILE_NAME), textSafe);
            Files.write(filePath(SWAP_NAME), textSafe);
        } catch (IOException ex) {
            Logger.getLogger(SecureDatabase.class.getName()).log(Level.FINEST, ex.getLocalizedMessage(), ex);
        }
    }
    
    private static Path filePath(String fileName) {
        return FileSystems.getDefault().getPath(".", fileName);
    }
    
    private static synchronized SecretKey keyGenAES() throws PasswordExpiredException {
        SecretKey key = null;
        try {
            byte[] salt = "salty mcbutter salts what what".getBytes();
            int iterations = 97447;
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEYGEN);
            PBEKeySpec pbeKeySpec = new PBEKeySpec(DigitalSafe.getInstance().getPassword().toCharArray(), salt, iterations, AES_KEY_LENGTH);
            key = keyFactory.generateSecret(pbeKeySpec);
            key = new SecretKeySpec(key.getEncoded(), "AES");
            Thread.sleep(100); // Yes, I'm calling sleep in a synchronized block on purpose.
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InterruptedException ex) {
            Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
        return key;
    }
    
    private static synchronized SecretKey keyGenBlowfish() throws PasswordExpiredException {
        SecretKey key = null;
        try {
            byte[] salt = "fish blows a what what".getBytes();
            int iterations = 90443;
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEYGEN);
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(DigitalSafe.getInstance().getPassword().getBytes());
            key = keyFactory.generateSecret(new PBEKeySpec(digest.toString().toCharArray(), salt, iterations, BLOWFISH_KEY_LENGTH));
            key = new SecretKeySpec(key.getEncoded(), "Blowfish");
            Thread.sleep(100); // Yes, I'm calling sleep in a synchronized block on purpose.
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InterruptedException ex) {
            Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
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
    
    private static byte[] decrypt(byte[] encryptedData) throws PasswordExpiredException {
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
        } catch (IllegalBlockSizeException | NoSuchPaddingException | BadPaddingException | InvalidKeyException ex) {
            Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException ex) {
            Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
        return unEncryptedData2;
    }
    
    private static byte[] encrypt(byte[] unencryptedData) throws PasswordExpiredException {
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
            Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        } catch (IllegalBlockSizeException | InvalidAlgorithmParameterException ex) {
            Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
        return encryptedData2;
    }
    
    static void validatePassword() throws InvalidPasswordException {
        try {
            if (Files.size(filePath(FILE_NAME)) < 1L) {
                return; // allow any password for empty database.
            }
        } catch (IOException ex) {
            Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
        try {
            getNoteBook();
        } catch (PasswordExpiredException ex) {
            Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }
 
}
