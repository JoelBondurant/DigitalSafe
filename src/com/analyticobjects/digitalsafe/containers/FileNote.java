package com.analyticobjects.digitalsafe.containers;

import com.analyticobjects.digitalsafe.ByteUtility;
import com.analyticobjects.digitalsafe.DigitalSafe;
import com.analyticobjects.digitalsafe.exceptions.PasswordExpiredException;
import java.io.File;
import java.io.IOException;
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

    public void export() throws IOException, PasswordExpiredException {
        byte[] bytes = DigitalSafe.loadFile(this.fileHash);
        ByteUtility.writeFully((new File(this.fileName)).toPath(), bytes);
    }

    public String getListing() {
        String SPACER = ", ";
        StringBuilder sb = new StringBuilder();
        sb.append(this.fileName).append(SPACER);
        sb.append(this.fileHash).append(SPACER);
        sb.append(Long.toString(this.sizeInBytes));
        sb.append(" bytes.");
        return sb.toString();
    }
    
    @Override
    String toXML() {
        StringBuilder xmlSnip = new StringBuilder();
        xmlSnip.append("\t<FileNote>\n");
        xmlSnip.append("\t\t<FileHash>").append(this.fileHash).append("</FileHash>\n");
        xmlSnip.append("\t\t<FileName>").append(this.fileName).append("</FileName>\n");
        xmlSnip.append("\t\t<SizeInBytes>").append(Long.toString(this.sizeInBytes)).append("</SizeInBytes>\n");
        xmlSnip.append("\t\t<Tags>").append(this.getTagString()).append("</Tags>\n");
        xmlSnip.append("\t</FileNote>\n");
        return xmlSnip.toString();
    }
}
