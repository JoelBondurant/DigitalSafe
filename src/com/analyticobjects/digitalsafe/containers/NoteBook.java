package com.analyticobjects.digitalsafe.containers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;



/**
 * Main serializable container.
 * seconds.
 * @author Joel Bondurant
 * @since 2013.08
 */
public class NoteBook implements Serializable {
    
    private final List<Note> notes;
    private final List<PasswordNote> passwordNotes;
    private final List<PictureNote> pictureNotes;
    private final List<FileNote> fileNotes;
    
    
    public NoteBook() {
        this.notes = new ArrayList<>();
        this.passwordNotes = new ArrayList<>();
        this.pictureNotes = new ArrayList<>();
        this.fileNotes = new ArrayList<>();
    }
    
    public void addNote(Note aNote) {
        this.notes.add(aNote);
    }
    
    public void addPasswordNote(PasswordNote aPasswordNote) {
        this.passwordNotes.add(aPasswordNote);
    }

    public Note getByTitle(String title) {
        for (Note note : notes) {
            if (note.getTitle().toLowerCase().equals(title.toLowerCase())) {
                return note;
            }
        }
        return null;
    }

}
