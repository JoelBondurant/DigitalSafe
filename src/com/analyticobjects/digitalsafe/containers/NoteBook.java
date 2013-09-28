package com.analyticobjects.digitalsafe.containers;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;



/**
 * Main serializable container for notes, passwords, and picture/file indexes.
 * seconds.
 * My thought is to have a data blob for everything but file contents
 * (indexes to file contents will be in this blob).
 * 
 * @author Joel Bondurant
 * @since 2013.08
 */
public class NoteBook implements Serializable {
    
    private final List<Note> notes;
    private final List<PasswordNote> passwordNotes;
    private final List<PictureNote> pictureNotes;
    private final List<FileNote> fileNotes;
    
    
    public NoteBook() {
        this.notes = new LinkedList<>();
        this.passwordNotes = new LinkedList<>();
        this.pictureNotes = new LinkedList<>();
        this.fileNotes = new LinkedList<>();
    }
    
    private int generateId(List<? extends Note> noteList) {
        int minimumAvailableId = 0;
        if (noteList.isEmpty()) {
            return minimumAvailableId;
        }
        Set<Integer> ids = new HashSet<>();
        for (Note aNote : noteList) {
            ids.add(aNote.getId());
        }
        int maxId = 0;
        for (Integer id : ids) {
            if (id > maxId) {
                maxId = id;
            }
        }
        for (int id = 0; id <= (maxId + 1); id++) {
            if (!ids.contains(id)) {
                minimumAvailableId = id;
                break;
            }
        }
        return minimumAvailableId;
    }
    
    public void putNote(Note aNote) {
        if (aNote.needsId()) {
            int newId = generateId(this.notes);
            aNote.setId(newId);
        }
        if (this.notes.contains(aNote)) {
            this.notes.remove(aNote);
        }
        this.notes.add(aNote);
    }
    
    public void putPasswordNote(PasswordNote aPasswordNote) {
         if (aPasswordNote.needsId()) {
            int newId = generateId(this.passwordNotes);
            aPasswordNote.setId(newId);
        }
        if (this.passwordNotes.contains(aPasswordNote)) {
            this.passwordNotes.remove(aPasswordNote);
        }
        this.passwordNotes.add(aPasswordNote);
    }
    
    public void putFileNote(FileNote aFileNote) {
        if (aFileNote.needsId()) {
            int newId = generateId(this.fileNotes);
            aFileNote.setId(newId);
        }
        if (this.fileNotes.contains(aFileNote)) {
            this.fileNotes.remove(aFileNote);
        }
        this.fileNotes.add(aFileNote);
    }

    public Note getNoteByTitle(String title) {
        for (Note note : this.notes) {
            if (note.getTitle().toLowerCase().equals(title.toLowerCase())) {
                return note;
            }
        }
        return null;
    }
    
    public PasswordNote getPasswordNoteByTitle(String title) {
        for (PasswordNote passwordNote : this.passwordNotes) {
            if (passwordNote.getTitle().toLowerCase().equals(title.toLowerCase())) {
                return passwordNote;
            }
        }
        return null;
    }
    
    public FileNote getFileNoteByFileName(String fileName) {
        for (FileNote fileNote : this.fileNotes) {
            if (fileNote.getFileName().toLowerCase().equals(fileName)) {
                return fileNote;
            }
        }
        return null;
    }

    public List<FileNote> getModifiedFileNotes() {
        List<FileNote> modifiedFileNotes = new LinkedList<>();
        for (FileNote fileNote : this.fileNotes) {
            if (fileNote.isSourceAttached()) {
                modifiedFileNotes.add(fileNote);
            }
        }
        return modifiedFileNotes;
    }
}
