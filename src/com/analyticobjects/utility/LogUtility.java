/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.analyticobjects.utility;

import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * A utility class for logging.
 *
 * @author Joel Bondurant
 * @since 2013.10
 */
public class LogUtility {
	
	public static void setLoggingLevelGlobally(Level loggingLevel) {
		LogManager logManager = LogManager.getLogManager();
		Enumeration<String> loggerNames = logManager.getLoggerNames();
		while (loggerNames.hasMoreElements()) {
			logManager.getLogger(loggerNames.nextElement()).setLevel(loggingLevel);
		}
	}
	
}
