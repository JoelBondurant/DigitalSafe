package com.analyticobjects.digitalsafe.ui;

import com.analyticobjects.digitalsafe.containers.DigitalSafe;
import com.analyticobjects.digitalsafe.containers.Note;
import com.analyticobjects.digitalsafe.containers.PasswordNote;
import com.analyticobjects.digitalsafe.exceptions.PassphraseExpiredException;
import com.analyticobjects.digitalsafe.ui.MainFrame.Context;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

/**
 * Commands.
 *
 * @author Joel Bondurant
 * @since 2013.09
 */
public class Commands {

	static void execute(String command, Context context) throws PassphraseExpiredException {
		command = command.toLowerCase();
		MainFrame mainFrame = MainFrame.getInstance();
		DigitalSafe digitalSafe = mainFrame.getDigitalSafe();
		// context independent commands.
		switch (command) {
			case "wipe":
			case "blank":
			case "clean":
			case "clear":
			case "reset": {
				digitalSafe.clear();
				mainFrame.lock();
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
			case "export": {
				throw new UnsupportedOperationException();
			}
		}
		// context dependent commands.
		switch (context) {
			case Notes: {
				noteCommand(digitalSafe, command);
				break;
			}
			case Passwords: {
				passwordCommand(digitalSafe, command);
				break;
			}
			case Files: {
				fileCommand(digitalSafe, command);
				break;
			}
		}
	}

	private static void noteCommand(DigitalSafe digitalSafe, String command) throws PassphraseExpiredException {
		if (command.equals("new") || command.equals("add")) {
			NotePanel notePanel = new NotePanel();
			MainFrame.getInstance().setNotesPanel(notePanel);
		} else if (command.equals("save")) {
			Note noteToSave = MainFrame.getInstance().getNoteFromNotePanel();
			digitalSafe.putNote(noteToSave);
			MainFrame.getInstance().setNotesPanel(null);
		} else if (command.startsWith("get ") || command.startsWith("load ") || command.startsWith("open ")) {
			String title = command.substring(4).trim();
			Note aNote = digitalSafe.getNote(title);
			if (aNote == null) {
				MainFrame.getInstance().setNotesPanel(null);
				return;
			}
			NotePanel notePanel = new NotePanel();
			notePanel.fromNote(aNote);
			MainFrame.getInstance().setNotesPanel(notePanel);
		}
	}

	private static void passwordCommand(DigitalSafe digitalSafe, String command) throws PassphraseExpiredException {
		if (command.equals("new") || command.equals("add")) {
			PasswordNotePanel passwordNotePanel = new PasswordNotePanel();
			MainFrame.getInstance().setPasswordNotesPanel(passwordNotePanel);
		} else if (command.equals("save")) {
			PasswordNote noteToSave = MainFrame.getInstance().getPasswordNoteFromPasswordNotePanel();
			digitalSafe.putPasswordNote(noteToSave);
			MainFrame.getInstance().setPasswordNotesPanel(null);
		} else if (command.startsWith("get ") || command.startsWith("load ") || command.startsWith("open ")) {
			String title = command.substring(4).trim();
			PasswordNote aNote = digitalSafe.getPasswordNote(title);
			if (aNote == null) {
				MainFrame.getInstance().setPasswordNotesPanel(null);
				return;
			}
			PasswordNotePanel passwordNotePanel = new PasswordNotePanel();
			passwordNotePanel.fromPasswordNote(aNote);
			MainFrame.getInstance().setPasswordNotesPanel(passwordNotePanel);
		}
	}

	private static void fileCommand(DigitalSafe digitalSafe, String command) throws PassphraseExpiredException {
		if (command.equals("new") || command.equals("add")) {
			JFileChooser jFileChooser = new JFileChooser();
			jFileChooser.addActionListener(null);
			MainFrame mainFrame = MainFrame.getInstance();
			int returnVal = jFileChooser.showOpenDialog(mainFrame);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File selectedFile = jFileChooser.getSelectedFile();
				digitalSafe.putFile(selectedFile);
			}
		} else if (command.startsWith("get ") || command.startsWith("export ")) {
			String fileName = command.substring(4).trim();
			if (command.startsWith("export ")) {
				fileName = command.substring(6).trim();
			}
			try {
				digitalSafe.getFile(fileName);
			} catch (IOException ex) {
				Logger.getLogger(Commands.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
			}
		} else if (command.equals("list") || command.equals("all") || command.equals("show")) {
			List<String[]> fileListing = digitalSafe.listFiles();
			ListPanel listingPanel = new ListPanel();
			listingPanel.setListing(fileListing);
			MainFrame.getInstance().setFilesPanel(listingPanel);
		}
	}
}
