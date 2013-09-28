package com.analyticobjects.digitalsafe.ui;

import com.analyticobjects.digitalsafe.DigitalSafe;
import com.analyticobjects.digitalsafe.containers.FileNote;
import com.analyticobjects.digitalsafe.containers.Note;
import com.analyticobjects.digitalsafe.containers.NoteBook;
import com.analyticobjects.digitalsafe.containers.PasswordNote;
import com.analyticobjects.digitalsafe.exceptions.PasswordExpiredException;
import com.analyticobjects.digitalsafe.ui.MainFrame.Context;
import java.io.File;
import javax.swing.JFileChooser;

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
            case Passwords: {
                passwordCommand(command);
                break;
            }
            case Files: {
                fileCommand(command);
                break;
            }
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
            DigitalSafe.commitNoteBook(noteBook);
            MainFrame.getInstance().setNotesPanel(null);
        } else if (command.startsWith("get ") || command.startsWith("load ") || command.startsWith("open ")) {
            String title = command.substring(4).trim();
            NoteBook noteBook = DigitalSafe.getNoteBook();
            Note aNote = noteBook.getNoteByTitle(title);
            if (aNote == null) {
                MainFrame.getInstance().setNotesPanel(null);
                return;
            }
            NotePanel notePanel = new NotePanel();
            notePanel.fromNote(aNote);
            MainFrame.getInstance().setNotesPanel(notePanel);
        }
    }
    
    private static void passwordCommand(String command) throws PasswordExpiredException {
        if (command.equals("new") || command.equals("add")) {
            PasswordNotePanel passwordNotePanel = new PasswordNotePanel();
            MainFrame.getInstance().setPasswordNotesPanel(passwordNotePanel);
        } else if (command.equals("save")) {
            PasswordNote noteToSave = MainFrame.getInstance().getPasswordNoteFromPasswordNotePanel();
            NoteBook noteBook = DigitalSafe.getNoteBook();
            noteBook.putPasswordNote(noteToSave);
            DigitalSafe.commitNoteBook(noteBook);
            MainFrame.getInstance().setPasswordNotesPanel(null);
        } else if (command.startsWith("get ") || command.startsWith("load ") || command.startsWith("open ")) {
            String title = command.substring(4).trim();
            NoteBook noteBook = DigitalSafe.getNoteBook();
            PasswordNote aNote = noteBook.getPasswordNoteByTitle(title);
            if (aNote == null) {
                MainFrame.getInstance().setNotesPanel(null);
                return;
            }
            PasswordNotePanel passwordNotePanel = new PasswordNotePanel();
            passwordNotePanel.fromPasswordNote(aNote);
            MainFrame.getInstance().setPasswordNotesPanel(passwordNotePanel);
        }
    }
    
    private static void fileCommand(String command) throws PasswordExpiredException {
        if (command.equals("new") || command.equals("add")) {
            JFileChooser jFileChooser = new JFileChooser();
            jFileChooser.addActionListener(null);
            MainFrame mainFrame = MainFrame.getInstance();
            int returnVal = jFileChooser.showOpenDialog(mainFrame);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File selectedFile = jFileChooser.getSelectedFile();
                FileNote newFileNote = new FileNote(selectedFile, "");
                NoteBook noteBook = DigitalSafe.getNoteBook();
                noteBook.putFileNote(newFileNote);
                DigitalSafe.commitNoteBook(noteBook);
            }
        } else if (command.startsWith("get ") || command.startsWith("load ") || command.startsWith("open ")) {
            System.out.println("nadda");
        }
    }
}
