package com.analyticobjects.digitalsafe.ui;

import com.analyticobjects.digitalsafe.DigitalSafe;
import com.analyticobjects.digitalsafe.containers.Note;
import com.analyticobjects.digitalsafe.containers.NoteBook;
import com.analyticobjects.digitalsafe.exceptions.PasswordExpiredException;
import com.analyticobjects.digitalsafe.ui.MainFrame.Context;

/**
 * Commands.
 * @author Joel Bondurant
 * @since 2013.09
 */
public class Commands {

    static void execute(String command, Context context) throws PasswordExpiredException {
        command = command.toLowerCase();
        // context independent commands.
        switch (command) {
            case "wipe":
            case "blank":
            case "clean":
            case "reset": {
                DigitalSafe.getInstance().reset();
                MainFrame.getInstance().lock();
                break;
            }
            case "q":
            case "quit":
            case "exit": {
                System.exit(0);
                break;
            }
            case "lock": {
                MainFrame.getInstance().lock();
                break;
            }
        }
        // context dependent commands.
        switch (context) {
            case Notes: {
                noteCommand(command);
                break;
            }
            default:
        }
    }

    private static void noteCommand(String command) throws PasswordExpiredException {
        if (command.equals("new") || command.equals("add")) {
            NotePanel notePanel = new NotePanel();
            MainFrame.getInstance().setNotesPanel(notePanel);
        } else if (command.equals("save")) {
            Note noteToSave = MainFrame.getInstance().getNoteFromNotePanel();
            NoteBook noteBook = DigitalSafe.getNoteBook();
            noteBook.putNote(noteToSave);
            DigitalSafe.getInstance().commitNoteBook(noteBook);
            MainFrame.getInstance().setNotesPanel(null);
        } else if (command.startsWith("get ") || command.startsWith("load ") || command.startsWith("open ")) {
            String title = command.substring(4).trim();
            NoteBook noteBook = DigitalSafe.getNoteBook();
            Note aNote = noteBook.getByTitle(title);
            if (aNote == null) {
                MainFrame.getInstance().setNotesPanel(null);
                return;
            }
            NotePanel notePanel = new NotePanel();
            notePanel.fromNote(aNote);
            MainFrame.getInstance().setNotesPanel(notePanel);
        }
    }
    
}
