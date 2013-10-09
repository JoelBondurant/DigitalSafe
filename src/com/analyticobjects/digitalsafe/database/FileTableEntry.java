package com.analyticobjects.digitalsafe.database;

import java.nio.file.Path;
import java.util.UUID;

/**
 * A note type to store arbitrary files.
 *
 * @author Joel Bondurant
 * @since 2013.09
 */
public class FileTableEntry implements IndexedTableEntry {
	
	private static final long serialVersionUID = 1L;

	private long id;
	private final String fileNameHash;
	private Path sourceFilePath;
	private String fileName;
	private long sizeInBytes;
	private String message;

	public FileTableEntry(Path sourceFilePath, String message) {
		this.fileNameHash = UUID.randomUUID().toString();
		this.sourceFilePath = sourceFilePath;
		this.fileName = sourceFilePath.getFileName().toString();
		this.sizeInBytes = sourceFilePath.toFile().length();
		this.message = message;
	}

	public String getFileName() {
		return this.fileName;
	}
	
	public String getFileNameHash() {
		return this.fileNameHash;
	}

	public Path getSourceFilePath() {
		return this.sourceFilePath;
	}

	public long getSizeInBytes() {
		return this.sizeInBytes;
	}

	public boolean isSourceAttached() {
		return !(this.sourceFilePath == null);
	}

	public void detachSource() {
		this.sourceFilePath = null;
	}

	@Override
	public String getIndexId() {
		StringBuilder sb = new StringBuilder(this.fileName);
		//sb.append(" ");
		//sb.append(this.fileNameHash);
		//sb.append(" ");
		//sb.append(this.message);
		return sb.toString().toLowerCase();
	}

	@Override
	public Long getId() {
		return this.id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

}
