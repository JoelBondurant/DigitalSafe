package com.analyticobjects.digitalsafe.ui;


import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * A secure clipboard.
 * @author Joel Bondurant
 * @since 2013.09
 */
public class SecureClipboard {
    
    private static SecureClipboard singletonInstance;
    private final ScheduledExecutorService executor;
    private int secondsToClearClipboard;
    private final Clipboard systemClipboard;
    
    private SecureClipboard() {
        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.secondsToClearClipboard = 120;
        this.systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    };
    
    public static synchronized SecureClipboard getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new SecureClipboard();
        }
        return singletonInstance;
    }
    
    private void directToClipboard(String text) {
        this.systemClipboard.setContents(new StringSelection(text), null);
    }
    
    public void toClipboard(String text) {
        this.executor.schedule(new ClearClipBoardTask(), secondsToClearClipboard, TimeUnit.SECONDS);
        directToClipboard(text);
    }
    
    private class ClearClipBoardTask implements Runnable {
        @Override
        public void run() {
            directToClipboard("");
        }
    }
    
    @Override
    protected void finalize() throws Throwable {
        try {
            super.finalize();
            (new ClearClipBoardTask()).run();
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).severe(ex.getMessage());
        }
    }
}
