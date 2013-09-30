package com.analyticobjects.digitalsafe;

import com.analyticobjects.digitalsafe.containers.FileNote;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * A class to hide all the note database decrypted data.
 * 
 * @author Joel Bondurant
 * @since 2013.08
 */
final class SecureDatabase {
    
    private static final String DB_NAME = "digitalSafe.safe";
    private static final String NOTEBOOK = "NoteBook";

    
    private SecureDatabase(){}; // no, just no.
    
    /**
     * Ensure a file exists.
     * @param name A file name to ensure is present.
     */
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
    
    /**
     * Ensure database files are present.
     */
    static void ensureDB() {
        ensureFile(DB_NAME);
    }
    
    /**
     * Reset the DigitalSafe database.
     */
    static void reset() {
        resetDB();
    }
    
    /**
     * Private reset the DigitalSafe database.
     */
    private static void resetDB() {
        Path dbPath = filePath(DB_NAME);
        File dbFile = dbPath.toFile();
        if (dbFile.exists()) {
            dbFile.delete();
        }
        ensureDB();
    }
    
    /**
     * A File object representation of the DigitalSafe database.
     * @return A File object representation of the DigitalSafe database.
     */
    private static File dbFile() {
        ensureDB();
        return filePath(DB_NAME).toFile();
    }
    
    /**
     * Gets a ZipOutputStream object representing the outer DigitalSafe database wrapper.
     * @return A zip file output stream for the DigitalSafe database.
     * @throws FileNotFoundException 
     */
    private static ZipOutputStream outZip() throws FileNotFoundException {
        return new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(dbFile())));
    }
    
    /**
     * Gets a ZipFile object representing the outer DigitalSafe database wrapper.
     * @return A zip file archive object for the DigitalSafe database.
     * @throws IOException 
     */
    private static ZipFile zipFile() throws IOException {
        return new ZipFile(dbFile());
    }
    
    /**
     * Get an executor with a thread for each processor core.
     * @return An executor with a thread for each processor core.
     */
    static ExecutorService allProcessorCores() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }
    
    /**
     * Get the noteBook from encrypted persistent storage.
     * @return A volatile memory noteBook.
     * @throws PasswordExpiredException 
     */
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
            byte[] decryptedNoteBook = Crypto.decrypt(DigitalSafe.getInstance().getPassword(), encryptedNoteBook);
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(decryptedNoteBook))) {
                notebook = (NoteBook) ois.readObject();
            }
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
        return notebook;
    }
    
    /**
     * Persist the noteBook to non-volatile storage.
     * @param noteBook The volatile memory noteBook.
     * @throws PasswordExpiredException 
     */
    static void commitNoteBook(NoteBook noteBook) throws PasswordExpiredException {
        try (
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                ZipOutputStream zipOut =  outZip();
            ) {
            // should only be one unmodified file per call, but may need multithreading in future.
            for (FileNote modifiedFileNote : noteBook.getModifiedFileNotes()) {
                zipOut.putNextEntry(new ZipEntry(modifiedFileNote.getFileHash()));
                Path sourceFilePath = modifiedFileNote.getSourceFilePath();
                byte[] fileBytes = ByteUtility.readFully(sourceFilePath);
                zipOut.write(Crypto.encrypt(DigitalSafe.getInstance().getPassword(), fileBytes));
                zipOut.flush();
                zipOut.closeEntry();
                modifiedFileNote.detachSource();
            }
            oos.writeObject(noteBook);
            oos.flush();
            byte[] encryptedNoteBook = Crypto.encrypt(DigitalSafe.getInstance().getPassword(), bos.toByteArray());
            zipOut.putNextEntry(new ZipEntry(NOTEBOOK));
            zipOut.write(encryptedNoteBook);
            zipOut.flush();
            zipOut.closeEntry();
            zipOut.close();
        } catch (IOException ex) {
            Logger.getLogger(SecureDatabase.class.getName()).log(Level.FINEST, ex.getLocalizedMessage(), ex);
        }
    }
    
    /**
     * This is a very exciting method.
     * @param fileName Hey look at me, I'm a file's name.
     * @return Path of supplied file name.
     */
    private static Path filePath(String fileName) {
        return FileSystems.getDefault().getPath(".", fileName);
    }
    
    
    /**
     * Test if the password is valid by trying to decrypt the database.
     * @throws InvalidPasswordException If the decryption fails.
     */
    static void validatePassword() throws InvalidPasswordException {
        try {
            if (Files.size(dbFile().toPath()) < 1L) {
                return; // allow any password for empty database.
            }
            getNoteBook();
        } catch (IOException | PasswordExpiredException ex) {
            Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            throw new InvalidPasswordException();
        }
    }
    
    /**
     * Load an encrypted file into volatile memory from the database.
     * @param fileHash The file hash of the stored file note.
     * @return 
     */
    static byte[] loadFile(String fileHash) throws PasswordExpiredException, IOException {
        byte[] fileBytes = null;
        ensureDB();
        try {
            if (Files.size(dbFile().toPath()) < 1L) {
                return fileBytes;
            }
        } catch (IOException ex) {
            Logger.getLogger(SecureDatabase.class.getName()).log(Level.FINEST, ex.getLocalizedMessage(), ex);
            throw ex;
        }
        byte[] encryptedFile = null;
        try (
                ZipFile zipFile = zipFile();
                InputStream fileInStream = zipFile.getInputStream(zipFile.getEntry(fileHash));
            ) {
            encryptedFile = ByteUtility.readFully(fileInStream);
        }
        fileBytes = Crypto.decrypt(DigitalSafe.getInstance().getPassword(), encryptedFile);
        return fileBytes;
    }
 
}
