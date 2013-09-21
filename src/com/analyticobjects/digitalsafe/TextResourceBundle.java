package com.analyticobjects.digitalsafe;

import java.util.ListResourceBundle;

/**
 * Text resource bundle.
 * @author Joel Bondurant
 * @since 2013.08
 */
public class TextResourceBundle extends ListResourceBundle {
    
    static final String VERSION = "VERSION";
    static final String APP_NAME = "APP_NAME";
    static final String HELP_PROMPT = "HELP_PROMPT";
    static final String UNKNOWN = "UNKNOWN";
    static final String PASSWORD_PROMPT = "PASSWORD_PROMPT";
    static final String EXIT = "EXIT";


    @Override
    protected Object[][] getContents() {
        return contents;
    }
    
    static final Object[][] contents = {
        {VERSION, "0.0.2"},
        {APP_NAME, "DigitalSafe"},
        {HELP_PROMPT, "type ? for help."},
        {UNKNOWN, "What the what?"},
        {PASSWORD_PROMPT, "Please enter password: "},
        {EXIT, "Exiting DigitalSafe."}
    };
    
}
