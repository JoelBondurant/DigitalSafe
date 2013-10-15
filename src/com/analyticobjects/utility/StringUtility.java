package com.analyticobjects.utility;

/**
 * A utility class for strings.
 *
 * @author Joel Bondurant
 * @since 2013.10
 */
public class StringUtility {
	
	public static String join(String joinString, String... strings) {
		StringBuilder sb = new StringBuilder();
		for (String string : strings) {
			sb.append(string).append(joinString);
		}
		return sb.substring(0, sb.length() - 1 - joinString.length());
	}
	
}
