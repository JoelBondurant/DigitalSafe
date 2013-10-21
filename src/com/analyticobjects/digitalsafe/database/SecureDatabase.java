package com.analyticobjects.digitalsafe.database;

import com.analyticobjects.utility.ByteUtility;
import com.analyticobjects.digitalsafe.exceptions.PassphraseExpiredException;
import com.analyticobjects.digitalsafe.crypto.Passphrase;
import com.analyticobjects.digitalsafe.crypto.TripleAES;
import com.analyticobjects.digitalsafe.exceptions.InvalidPassphraseException;
import com.analyticobjects.utility.SerializationUtility;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Manage secure data persistence. The goal of this class is to securely store user data as concisely as possible to
 * make code reviews as easy as possible. The goal of most databases is to make data highly available for marketing from
 * a localized server cluster. I call this a seize everything architecture, as hackers and totalitarian governments
 * simply need to hit up one place to seize all data. The approach taken here is based on noting current palm-top
 * computers (aka phones) have multiGB multiGhz quad-core power and local storage is the best way to store data. Person
 * specific data should follow data gravity and stick with the person it is related to.
 *
 * TODO: Automate distributed p2p or f2f backup and synchronization with untrustworthy/cloud storage providers.
 *
 * @author Joel Bondurant
 * @since 2013.08
 */
public final class SecureDatabase {
	
	private final Passphrase passphrase;
	private final Path dbPath;
	private static final String MASTER_INDEX = "MASTER_INDEX";

	/**
	 * Constructs a new connection to a secure database at the path supplied.
	 * 
	 * @param dbPath A path for the database file.
	 */
	public SecureDatabase(Path dbPath) {
		this.dbPath = Paths.get(dbPath.toUri());
		this.passphrase = new Passphrase();
		this.ensureFile();
	}

	/**
	 * Ensure the db file exists.
	 */
	private void ensureFile() {
		File dbFile = this.dbPath.toFile();
		if (!dbFile.exists()) {
			try {
				dbFile.createNewFile();
			} catch (IOException ex) {
				Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
			}
		}
	}

	/**
	 * Resets the database.
	 */
	public void clear() {
		File dbFile = this.dbFile();
		if (dbFile.exists()) {
			dbFile.delete();
		}
		ensureFile();
	}
	
	public void lock() {
		this.passphrase.clear();
	}
	
	public boolean isLocked() {
		return this.passphrase.isClear();
	}
	
	public void setPassphrase(String passphrase) throws InvalidPassphraseException {
		this.passphrase.setPassphrase(passphrase);
		if (dbFile().length() == 0) {
			return; // accept any passphrase for an empty database.
		}
		try {
			this.getMasterIndex();
		} catch (PassphraseExpiredException ex) {
			lock();
			Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
		}
	}

	/**
	 * A File object representation of the database.
	 *
	 * @return A File object representation of the database.
	 */
	private File dbFile() {
		ensureFile();
		return this.dbPath.toFile();
	}

	/**
	 * Gets a ZipOutputStream object representing the outer database wrapper.
	 *
	 * @return A zip file output stream for the Passphrase database.
	 * @throws FileNotFoundException
	 */
	private ZipOutputStream outZip() throws FileNotFoundException {
		return new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(dbFile())));
	}

	/**
	 * Gets a ZipFile object representing the outer Passphrase database wrapper.
	 *
	 * @return A zip file archive object for the Passphrase database.
	 * @throws IOException
	 */
	private ZipFile zipDbFile() throws IOException {
		return new ZipFile(dbFile());
	}
	
	/**
	 * @return True if empty, false ow.
	 */
	public boolean isEmpty() {
		return (dbFile().length() < 1L);
	}

	/**
	 * Get the master index from encrypted persistent storage.
	 *
	 * @return A volatile memory version of the master index.
	 * @throws PassphraseExpiredException
	 */
	public MasterIndex getMasterIndex() throws PassphraseExpiredException {
		ensureFile();
		if (isEmpty()) {
			return new MasterIndex(); // only make a new MasterIndex for empty db.
		}
		try (
			ZipFile zipFile = zipDbFile();
			InputStream masterIndexInStream = zipFile.getInputStream(zipFile.getEntry(MASTER_INDEX));) {
			byte[] encryptedMasterIndex = ByteUtility.readFully(masterIndexInStream);
			byte[] decryptedMasterIndex = TripleAES.decrypt(this.passphrase, encryptedMasterIndex);
			return SerializationUtility.<MasterIndex>inflate(decryptedMasterIndex);
		} catch (IOException | ClassNotFoundException ex) {
			Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
		}
		return null;
	}

	/**
	 * Persist the master index to non-volatile storage.
	 *
	 * @param masterIndex The volatile memory master index.
	 * @throws PassphraseExpiredException
	 */
	public void commitMasterIndex(MasterIndex masterIndex) throws PassphraseExpiredException {
		
		try (ZipOutputStream zipOut = outZip();) {
			// should only be one unmodified file per call, but may need multithreading in future.
			for (FileTable fileTable : masterIndex.getFileTables()) {
				Logger.getLogger(SecureDatabase.class.getName()).log(Level.FINE, "Committing File Table: {0}", fileTable.getName());
				for (FileTableEntry fileTableEntry : fileTable.getAll()) {
					if (!fileTableEntry.isSourceAttached()) {
						continue;
					}
					Logger.getLogger(SecureDatabase.class.getName()).log(Level.FINE, "Committing File: {0}", fileTableEntry.getFileName());
					zipOut.putNextEntry(new ZipEntry(fileTableEntry.getFileNameHash()));
					Path sourceFilePath = fileTableEntry.getSourceFilePath();
					byte[] fileBytes = ByteUtility.readFully(sourceFilePath);
					zipOut.write(TripleAES.encrypt(this.passphrase, fileBytes));
					zipOut.flush();
					zipOut.closeEntry();
					fileTableEntry.detachSource();
				}
			}
			masterIndex.incrementCommitCount();
			byte[] encryptedMasterIndex = TripleAES.encrypt(this.passphrase, SerializationUtility.<MasterIndex>deflate(masterIndex));
			zipOut.putNextEntry(new ZipEntry(MASTER_INDEX));
			zipOut.write(encryptedMasterIndex);
			zipOut.flush();
			zipOut.closeEntry();
			zipOut.close();
		} catch (IOException ex) {
			Logger.getLogger(SecureDatabase.class.getName()).log(Level.FINEST, ex.getLocalizedMessage(), ex);
		}
	}

	/**
	 * Load an encrypted file into volatile memory from the database.
	 *
	 * @param fileTableEntry A file table entry record to export.
	 * @return The raw unencrypted file bytes.
	 * @throws com.analyticobjects.digitalsafe.exceptions.PassphraseExpiredException
	 * @throws java.io.IOException
	 */
	public byte[] loadFile(FileTableEntry fileTableEntry) throws PassphraseExpiredException, IOException {
		if (fileTableEntry == null) {
			return null;
		}
		byte[] fileBytes = null;
		ensureFile();
		try {
			if (Files.size(dbFile().toPath()) < 1L) {
				return fileBytes;
			}
		} catch (IOException ex) {
			Logger.getLogger(SecureDatabase.class.getName()).log(Level.FINEST, ex.getLocalizedMessage(), ex);
			throw ex;
		}
		byte[] encryptedFile;
		try (
			ZipFile zipFile = zipDbFile();
			InputStream fileInStream = zipFile.getInputStream(zipFile.getEntry(fileTableEntry.getFileNameHash()));) {
			encryptedFile = ByteUtility.readFully(fileInStream);
		}
		fileBytes = TripleAES.decrypt(this.passphrase, encryptedFile);
		return fileBytes;
	}

}
