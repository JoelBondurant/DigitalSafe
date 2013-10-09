package com.analyticobjects.digitalsafe.containers;

import com.analyticobjects.digitalsafe.database.FileTable;
import com.analyticobjects.digitalsafe.database.FileTableEntry;
import com.analyticobjects.digitalsafe.database.IndexedMapTable;
import com.analyticobjects.digitalsafe.database.MapTable;
import com.analyticobjects.digitalsafe.database.MasterIndex;
import com.analyticobjects.digitalsafe.database.SecureDatabase;
import com.analyticobjects.digitalsafe.exceptions.InvalidPassphraseException;
import com.analyticobjects.digitalsafe.exceptions.PassphraseExpiredException;
import com.analyticobjects.utility.ByteUtility;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A controller to SecureDatabase for higher level access.
 *
 * @author Joel Bondurant
 * @since 2013.10
 */
public class DigitalSafe {
	
	private SecureDatabase secureDatabase;
	private static final String DEFAULT_FILE_NAME = "digitalSafe.safe";
	private static final String NOTES = "NOTES";
	private static final String PASSWORDS = "PASSWORDS";
	private static final String FILES = "FILES";

	public DigitalSafe() {
		this(Paths.get(DEFAULT_FILE_NAME));
	}
	
	public DigitalSafe(Path dbPath) {
		this.secureDatabase = new SecureDatabase(dbPath);
	}
	
	private void init() throws PassphraseExpiredException {
		if (this.secureDatabase.isEmpty()) {
			MasterIndex masterIndex = this.secureDatabase.getMasterIndex();
			masterIndex.putIndexedMapTable(new IndexedMapTable<>(new MapTable<Note>(NOTES)));
			masterIndex.putIndexedMapTable(new IndexedMapTable<>(new MapTable<PasswordNote>(PASSWORDS)));
			masterIndex.putFileTable(new FileTable(FILES));
			this.secureDatabase.commitMasterIndex(masterIndex);
		}
	}

	public boolean isLocked() {
		return this.secureDatabase.isLocked();
	}

	public void setPassphrase(String string) throws InvalidPassphraseException {
		this.secureDatabase.setPassphrase(string);
		try {
			init();
		} catch (PassphraseExpiredException ex) {
			Logger.getLogger(DigitalSafe.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
			throw new InvalidPassphraseException();
		}
	}

	public void lock() {
		this.secureDatabase.lock();
	}

	public void clear() {
		this.secureDatabase.clear();
	}

	public void putNote(Note noteToSave) throws PassphraseExpiredException {
		MasterIndex masterIndex = this.secureDatabase.getMasterIndex();
		IndexedMapTable noteTable = masterIndex.getIndexedMapTable(NOTES);
		noteTable.putEntry(noteToSave);
		this.secureDatabase.commitMasterIndex(masterIndex);
	}

	public Note getNote(String title) throws PassphraseExpiredException {
		MasterIndex masterIndex = this.secureDatabase.getMasterIndex();
		IndexedMapTable<Note> noteTable = masterIndex.getIndexedMapTable(NOTES);
		return noteTable.getEntry(title);
	}

	public void putPasswordNote(PasswordNote noteToSave) throws PassphraseExpiredException {
		MasterIndex masterIndex = this.secureDatabase.getMasterIndex();
		IndexedMapTable passwordNoteTable = masterIndex.getIndexedMapTable(PASSWORDS);
		passwordNoteTable.putEntry(noteToSave);
		this.secureDatabase.commitMasterIndex(masterIndex);
	}

	public PasswordNote getPasswordNote(String title) throws PassphraseExpiredException {
		MasterIndex masterIndex = this.secureDatabase.getMasterIndex();
		IndexedMapTable<PasswordNote> passwordNoteTable = masterIndex.getIndexedMapTable(PASSWORDS);
		return passwordNoteTable.getEntry(title);
	}

	public void putFile(File selectedFile) throws PassphraseExpiredException {
		MasterIndex masterIndex = this.secureDatabase.getMasterIndex();
		FileTable fileTable = masterIndex.getFileTable(FILES);
		fileTable.putEntry(new FileTableEntry(selectedFile.toPath(), ""));
		this.secureDatabase.commitMasterIndex(masterIndex);
	}

	public void getFile(String fileName) throws PassphraseExpiredException, IOException {
		MasterIndex masterIndex = this.secureDatabase.getMasterIndex();
		FileTable fileTable = masterIndex.getFileTable(FILES);
		FileTableEntry fileEntry = fileTable.getEntry(fileName);
		byte[] fileBytes = this.secureDatabase.loadFile(fileEntry);
		if (fileBytes != null) {
			ByteUtility.writeFully(Paths.get(fileName), fileBytes);
		}
	}

	public List<String[]> listFiles() throws PassphraseExpiredException {
		MasterIndex masterIndex = this.secureDatabase.getMasterIndex();
		IndexedMapTable<FileTableEntry> fileTable = masterIndex.getFileTable(FILES);
		List<FileTableEntry> fileEntries = fileTable.getAll();
		List<String[]> fileListing = new ArrayList<>(fileEntries.size());
		for (FileTableEntry fileTableEntry : fileEntries) {
			StringBuilder sb = new StringBuilder(fileTableEntry.getFileName());
			sb.append(", ");
			sb.append(fileTableEntry.getFileNameHash());
			sb.append(", ");
			sb.append(fileTableEntry.getSizeInBytes());
			sb.append("bytes.");
			String[] listing = {(sb.toString())};
			fileListing.add(listing);
		}
		return fileListing;
	}
	
}
