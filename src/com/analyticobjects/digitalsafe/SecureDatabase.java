package com.analyticobjects.digitalsafe;

import com.analyticobjects.digitalsafe.exceptions.InvalidPasswordException;
import com.analyticobjects.digitalsafe.exceptions.PasswordExpiredException;
import com.analyticobjects.digitalsafe.containers.NoteBook;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
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
    
    private static final String DB_NAME = "digitalSafe.safe";
    private static final String KEYGEN = "PBKDF2WithHmacSHA1";
    // AES requires PKCS7Padding or better, not PKCS5Padding, so better it is.
    private static final String AES = "AES/CBC/NoPadding";
    private static final int AES_BLOCK_SIZE = 16; // 16 byte (128 bit) blocks.
    private static final int AES_KEY_LENGTH = 128;
    private static final String NOTEBOOK = "noteBook";

    
    private SecureDatabase(){}; // no, just no.
    
    private static void ensureFile(String name) {
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
    
    static void ensureDB() {
        ensureFile(DB_NAME);
    }
    
    static void reset() {
        resetDB();
    }
    
    private static void resetDB() {
        Path dbPath = filePath(DB_NAME);
        File dbFile = dbPath.toFile();
        if (dbFile.exists()) {
            dbFile.delete();
        }
        ensureDB();
    }
    
    private static File dbFile() {
        ensureDB();
        return filePath(DB_NAME).toFile();
    }
       
    private static ZipOutputStream outZip() throws FileNotFoundException {
        return new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(dbFile())));
    }
    
    private static ZipFile zipFile() throws IOException {
        return new ZipFile(dbFile());
    }
    
    static NoteBook getNoteBook() throws PasswordExpiredException {
        NoteBook notebook = null;
        ensureDB();
        try {
            if (Files.size(dbFile().toPath()) < 1L) {
                return new NoteBook(); // only make a new NoteBook for empty db.
            }
        } catch (IOException ex) {
            Logger.getLogger(SecureDatabase.class.getName()).log(Level.FINEST, ex.getLocalizedMessage(), ex);
        }
        try (
                ZipFile zipFile = zipFile();
                InputStream noteBookInStream = zipFile.getInputStream(zipFile.getEntry(NOTEBOOK));
            ) {
            byte[] encryptedNoteBook = ByteUtility.readFully(noteBookInStream);
            byte[] decryptedNoteBook = decrypt(encryptedNoteBook);
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(decryptedNoteBook))) {
                notebook = (NoteBook) ois.readObject();
            }
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
        return notebook;
    }
    
    static void commitNoteBook(NoteBook noteBook) throws PasswordExpiredException {
        try (
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                ZipOutputStream zipOut =  outZip();
            ) {
            oos.writeObject(noteBook);
            oos.flush();
            byte[] encryptedNoteBook = encrypt(bos.toByteArray());
            zipOut.putNextEntry(new ZipEntry(NOTEBOOK));
            zipOut.write(encryptedNoteBook);
            zipOut.flush();
            zipOut.closeEntry();
            zipOut.close();
        } catch (IOException ex) {
            Logger.getLogger(SecureDatabase.class.getName()).log(Level.FINEST, ex.getLocalizedMessage(), ex);
        }
    }
    
    private static Path filePath(String fileName) {
        return FileSystems.getDefault().getPath(".", fileName);
    }
    
    private static synchronized SecretKey keyGenAES(byte[] salt, int iterations) throws PasswordExpiredException {
        SecretKey key = null;
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEYGEN);
            PBEKeySpec pbeKeySpec = new PBEKeySpec(DigitalSafe.getInstance().getPassword().toCharArray(), salt, iterations, AES_KEY_LENGTH);
            key = keyFactory.generateSecret(pbeKeySpec);
            key = new SecretKeySpec(key.getEncoded(), "AES");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
        return key;
    }
    
    private static IvParameterSpec ivParameterSpec16(int level) {
        byte[] iv = { 1, 0, 30, 1, 0, 0, 90, 1, 0, 0, 10, 0, 20, 0, 1, 70 };
        iv[level] = 7;
        iv[level + 1] = 13;
        iv[level + 2] = 7;
        return new IvParameterSpec(iv);
    }
    
    private static List<Cipher> cipherList(int mode) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException, PasswordExpiredException {
        Cipher aes1 = Cipher.getInstance(AES);
        Cipher aes2 = Cipher.getInstance(AES);
        Cipher aes3 = Cipher.getInstance(AES);
        aes1.init(mode, keyGenAES("saltyN3SS&Whate".getBytes(), 189213), ivParameterSpec16(1));
        aes2.init(mode, keyGenAES("saltyN74G@337q8".getBytes(), 239404), ivParameterSpec16(2));
        aes3.init(mode, keyGenAES("saltyN99!14Ra12".getBytes(), 197781), ivParameterSpec16(3));
        return Arrays.asList(aes1, aes2, aes3);
    }
    
    private static byte[] decrypt(byte[] encryptedData) throws PasswordExpiredException {
        byte[] byteHolder1;
        byte[] byteHolder2 = null;
        try {
            List<Cipher> ciphers = cipherList(Cipher.DECRYPT_MODE);
            byteHolder1 = ciphers.get(2).doFinal(encryptedData);
            byteHolder2 = ciphers.get(1).doFinal(byteHolder1);
            byteHolder1 = ciphers.get(0).doFinal(byteHolder2);
            byteHolder2 = unPad4AES(byteHolder1);
        } catch (IllegalBlockSizeException | NoSuchPaddingException | BadPaddingException | InvalidKeyException ex) {
            Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException ex) {
            Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
        return byteHolder2;
    }
    
    private static byte[] encrypt(byte[] unencryptedData) throws PasswordExpiredException {
        byte[] byteHolder1;
        byte[] byteHolder2 = null;
        try {
            List<Cipher> ciphers = cipherList(Cipher.ENCRYPT_MODE);
            byteHolder1 = pad4AES(unencryptedData);
            byteHolder2 = ciphers.get(0).doFinal(byteHolder1);
            byteHolder1 = ciphers.get(1).doFinal(byteHolder2);
            byteHolder2 = ciphers.get(2).doFinal(byteHolder1);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException ex) {
            Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        } catch (IllegalBlockSizeException | InvalidAlgorithmParameterException ex) {
            Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
        return byteHolder2;
    }
    
    private static byte[] unPad4AES(byte[] paddedEncryptedData) throws BadPaddingException {
        if (paddedEncryptedData.length == 0) {
            throw new BadPaddingException();
        }
        if ((paddedEncryptedData.length % AES_BLOCK_SIZE) != 0) {
            throw new BadPaddingException();
        }
        byte lastByte = paddedEncryptedData[paddedEncryptedData.length - 1];
        if ((lastByte < 0) || (lastByte >= AES_BLOCK_SIZE)) {
            throw new BadPaddingException();
        }
        for (int i = 1; i <= (lastByte + AES_BLOCK_SIZE); i++) {
             byte aByte = paddedEncryptedData[paddedEncryptedData.length - i];
             if (aByte != lastByte) {
                 throw new BadPaddingException(); 
             }
        }
        int encryptedSize = paddedEncryptedData.length - lastByte - AES_BLOCK_SIZE;
        byte[] encryptedData = new byte[encryptedSize];
        System.arraycopy(paddedEncryptedData, 0, encryptedData, 0, encryptedSize);
        return encryptedData;
    }

    /**
     * Pad unencrypted data for AES / Blowfish blocksize.
     * @param unencryptedData Raw unencrypted binary to encrypt, of no particular size.
     * @return Unencrypted binary padded for AES block size. (and Blowfish)
     */
    private static byte[] pad4AES(byte[] unencryptedData) {
        int bytesToPad = AES_BLOCK_SIZE - (unencryptedData.length % AES_BLOCK_SIZE);
        byte bytesToPadValue = Integer.valueOf(bytesToPad).byteValue();
        int totalBytes = unencryptedData.length + bytesToPad + AES_BLOCK_SIZE;
        byte[] paddedUnencryptedData = new byte[totalBytes];
        System.arraycopy(unencryptedData, 0, paddedUnencryptedData, 0, unencryptedData.length);
        Arrays.fill(paddedUnencryptedData, unencryptedData.length, totalBytes, bytesToPadValue);
        return paddedUnencryptedData;
    }
    
    static void validatePassword() throws InvalidPasswordException {
        try {
            if (Files.size(dbFile().toPath()) < 1L) {
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
