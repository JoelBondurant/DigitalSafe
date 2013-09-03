package com.analyticobjects.digitalsafe;

import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


/**
 * Main console app class.
 * 
 * @author Joel Bondurant
 *  @since 2013.08
 */
public class TextSafe {
    
    private NoteBook noteBook;
    private ResourceBundle textBundle;
    private static final String COMMAND_PROMPT = "ts:> ";
    private static final String DEBUG = "DEBUG";
    
    
    private TextSafe() {
        textBundle = ResourceBundle.getBundle(TextResourceBundle.class.getName());
        noteBook = NoteBook.getInstance();
        SecureDatabase.prepFiles();
        setLoggingLevelGlobally(Level.OFF);
    }
    
    private void setLoggingLevelGlobally(Level loggingLevel) {
        LogManager logManager = LogManager.getLogManager();
        Enumeration<String> loggerNames = logManager.getLoggerNames();
        while (loggerNames.hasMoreElements()) {
            logManager.getLogger(loggerNames.nextElement()).setLevel(loggingLevel);
        }
    } 

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TextSafe textSafe = new TextSafe();
        //textSafe.printSecurityProviders();
        textSafe.repl();
    }
    

    
    private void repl() {
        print(textBundle.getString(TextResourceBundle.APP_NAME));
        tab();
        print("v");
        println(textBundle.getString(TextResourceBundle.VERSION));
        println(textBundle.getString(TextResourceBundle.HELP_PROMPT));
        passwordPrompt();
        while (true) {
            print(COMMAND_PROMPT);
            String cmd = readln();
            checkForQuitAndQuit(cmd);
            run(cmd);
        }
    }
    
    private void checkForQuitAndQuit(String cmd) {
        cmd = cmd.toUpperCase();
        if (cmd.equals("QUIT") || cmd.equals("EXIT") || cmd.equals("Q")) {
            println(textBundle.getString(TextResourceBundle.EXIT));
            System.exit(0);
        }
    }
    
    private void run(String cmd) {
        List<String> cmdSplit = Arrays.asList(cmd.split("\\s"));
        String mainCommand = cmdSplit.get(0).toUpperCase();
        try {
            switch (mainCommand) {
                case DEBUG: {
                    printSecurityProviders();
                    break;
                }
                case Help.HELP: {
                    Help.printHelp();
                    break; }
                case NoteBook.RESET: {
                    noteBook.reset();
                    break; }
                case Note.GET: {
                    if (cmdSplit.size() < 2) {
                        break;
                    }
                    String message = noteBook.getMessageFuzzy(cmdSplit.get(1));
                    if (!message.isEmpty()) {
                        println(message);
                    }
                    break; }
                case Note.PUT: {
                    if (cmdSplit.size() < 3) {
                        break;
                    }
                    String name = cmdSplit.get(1);
                    String message = cmd.replaceAll("^\\s*[Pp][Uu][Tt]\\s+" + name + "\\s+", "");
                    noteBook.putNote(new Note(name, message));
                    break; }
                case Note.FIND: {
                    if (cmdSplit.size() < 2) {
                        break;
                    }
                    String namePart = cmdSplit.get(1);
                    List<String> matches = noteBook.find(namePart);
                    for (String match : matches) {
                        println(match);
                    }
                    break; }
                default: {
                    println(textBundle.getString(TextResourceBundle.UNKNOWN));
                    break; }
            } 
        } catch (NoteBook.PasswordExpiredException ex) {
            println(ex.getMessage());
            passwordPrompt();
        }
    }
    
    static void print(Object obj) {
        System.out.print(obj);
        System.out.flush();
    }
    
    static void println(Object obj) {
        System.out.println(obj);
        System.out.flush();
    }
    
    static String readln() {
        System.out.flush();
        Scanner inputScanner = new Scanner(System.in);
        return inputScanner.nextLine();
    }
    
    static void tab() {
        print("    ");
    }
    
    private void passwordPrompt() {
        boolean keepPrompting = true;
        while (keepPrompting) {
            try {
                println(textBundle.getString(TextResourceBundle.PASSWORD_PROMPT));
                String password;
                if (System.console() != null) {
                    char[] passwordChars = System.console().readPassword();
                    password = new String(passwordChars);
                } else {
                    password = readln();
                }
                checkForQuitAndQuit(password);
                NoteBook.getInstance().setPassword(password);
                keepPrompting = false;
            } catch (NoteBook.InvalidPasswordException ex) {
                Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }
    
    private void printSecurityProviders() {
        for (Provider provider: Security.getProviders()) {
            println("\n\n" + provider.getName());
            for (String key: provider.stringPropertyNames()) {
                println("\t" + key + "\t" + provider.getProperty(key));
            }
        }
    }
    
    
}
