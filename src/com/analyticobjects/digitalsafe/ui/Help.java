package com.analyticobjects.digitalsafe.ui;

/**
 * Help system.
 * @author Joel Bondurant
 * @since 2013.08
 */
public class Help {
    
    static final String HELP = "?";
    static final String COMMAND_LIST;
    
    static {
        StringBuilder commandList = new StringBuilder();
        commandList.append("GET noteName - Fetches a note by rough name match (to clipboard).\n");
        commandList.append("\te.g. get gmail.password\n");
        commandList.append("PUT noteName noteMessage - Puts a new note in the database by exact name match.\n");
        commandList.append("\te.g. put gmail.password S0m3thing!C@nn0tReca11\n");
        commandList.append("FIND regex - Searches for notes (names and contents) matching the supplied regex.\n");
        commandList.append("\te.g. find .+\n");
        commandList.append("RESET - Resets the database. All stored notes will be lost.\n");
        commandList.append("Q, QUIT, EXIT - Exits TextSafe.\n");
        COMMAND_LIST = commandList.toString();
    }
    
}
