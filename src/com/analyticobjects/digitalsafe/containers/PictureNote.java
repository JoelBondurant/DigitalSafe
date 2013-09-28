package com.analyticobjects.digitalsafe.containers;

import java.io.File;

/**
 * A note type to store pictures.
 * @author Joel Bondurant
 * @since 2013.09
 */
public class PictureNote extends FileNote {

    public PictureNote(File sourceFile, String message) {
        super(sourceFile, message);
    }
    
}
