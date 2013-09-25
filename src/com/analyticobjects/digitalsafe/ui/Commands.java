package com.analyticobjects.digitalsafe.ui;

import com.analyticobjects.digitalsafe.ui.MainFrame.Context;

/**
 * Commands.
 * @author Joel Bondurant
 * @since 2013.09
 */
public class Commands {

    static void execute(String command, Context context) {
        command = command.toLowerCase();
        switch (context) {
            case Notes: {
                noteCommand(command);
            }
            default:
        }
    }

    private static void noteCommand(String command) {
        if (command.equals("new") || command.equals("add")) {
            NotePanel notePanel = new NotePanel();
            MainFrame.getInstance().addNotesPanel(notePanel);
        }
    }
    
}
