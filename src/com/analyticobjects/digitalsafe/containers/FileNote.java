package com.analyticobjects.digitalsafe.containers;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

/**
 * A note type to store arbitrary files.
 * @author Joel Bondurant
 * @since 2013.09
 */
public class FileNote extends Note {
    
    private final String fileHash;
    private Path sourceFilePath;
    private String fileName;
    private long sizeInBytes;

    public FileNote(File sourceFile, String message) {
        super("", message);
        this.sourceFilePath = sourceFile.toPath();
        this.fileName = sourceFilePath.getFileName().toString();
        this.sizeInBytes = sourceFile.length();
        this.fileHash = UUID.randomUUID().toString();
    }
    
    public String getFileName() {
        return this.fileName;
    }
    
    public Path getSourceFilePath() {
        return this.sourceFilePath;
    }
    
    public long getSizeInBytes() {
        return this.sizeInBytes;
    }
    
    public String getFileHash() {
        return this.fileHash;
    }
    
    public boolean isSourceAttached() {
        return !(this.sourceFilePath == null);
    }
    
    public void detachSource() {
        this.sourceFilePath = null;
    }
}
