package com.analyticobjects.utility;

/**
 * A utility class for strings.
 *
 * @author Joel Bondurant
 * @since 2013.10
 */
public class StringUtility {
	
	/**
	 * Join some strings together with a uniform joiner.
	 * @param joinString A string to put between each input string. e.g. ", "
	 * @param strings Some strings.
	 * @return The joined strings.
	 */
	public static String join(String joinString, String... strings) {
		StringBuilder sb = new StringBuilder();
		for (String string : strings) {
			sb.append(string).append(joinString);
		}
		return sb.substring(0, sb.length() - 1 - joinString.length());
	}
	
	
	public static String insertLineBreaks(String inputString, int fixedBreakInterval) {
		if (inputString == null || inputString.isEmpty()) {
			return "";
		}
		if (inputString.length() <= fixedBreakInterval) {
			return inputString;
		}
		StringBuilder output = new StringBuilder(inputString);
		int insertCount = 0;
		while (true) {
			insertCount++;
			int psn = (fixedBreakInterval * insertCount) + (insertCount - 1);
			if (psn >= output.length()) {
				return output.toString();
			}
			output.insert(psn, "\n");
		}
	}
	
}
