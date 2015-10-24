package preprocessing.diffpreprocessor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Cleaner {

	/**
	 * Cleans the input from all not necessary meta information. Thus, just information about 
	 * starting line and number of (affected) lines is retained 
	 */
	public static String cleanInput(String input) {
		if (input != null && !input.isEmpty()) {
			// first line says, that lines start with one expression within the parenthesis followed by an optional : and a mandatory whitespace
			String regex = "((From|Date):\\s" //removed Subject from inside parentheses
					// matches the following example: 8071651f2235b87551e757ba8f53d74509be5d3f Mon Sep 17 00:00:00 2001 (hash, day, month, date, time, year)
					+ "((" + /*[0-9a-z]{40}\\s*/"(Mon|Tue|Wed|Thu|Fri|Sat|Sun){1}\\s(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec){1} [1-9]{2}\\s([0-9]{2}:?){3}\\s[0-9]{4})|"
					// user name and email of committer
					+ "([\\w|\\.|_]+\\s<[\\w|\\.|_]+@[\\w|\\d|\\.|_]+[^\\.]+>)|"
					// Date
					+ "((Mon|Tue|Wed|Thu|Fri|Sat|Sun){1},\\s[\\d]{1,2}\\s(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec){1}\\s[0-9]{4}\\s([0-9]{2}:?){3}\\s\\+[0-9]{4})|"
					// commit message. DOES NOT WORK YET!!!!!!!!!!!!!!!! (and probably needn't to) 
//					+ "(([\\w]+)$)))|" 
					// only because previous line is commented out
					+ "))|"
					// diff --git a/class.extension b/class.extension
					+ "((diff\\s--git\\sa\\/[^.]+\\.[\\w]{1,4}\\sb\\/[^.]+\\.[\\w]{1,4})|"
					// index 7hash..7hash (optional:) 6digits
					+ "(index\\s[0-9a-f]{7}[\\.]{2}[0-9a-f]{7})(\\s[\\d]{6})?|"
					//---/+++ followed by [a|b]/class.ext or /dev/null
					+ "(([\\-]{3})|([\\+]{3}))\\s(([a|b]\\/[^.]+\\.[\\w]{1,4})|(\\/(dev)\\/(null)))|"
					// keyword file mode 6digits
					+ "((new|modified|deleted)\\s(file)\\s(mode)\\s[\\d]{6}))";
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(input);
			input = m.replaceAll("");
		}
		return input;
	}
	
	public static String cleanDiffFromComments(String diff) {
//		String[] lines = diff.split("\\n");
//		StringBuilder result = new StringBuilder();
//		boolean multiLine = false;
//		for (String line : lines) {
//			if (line.contains("/*")) {
//				multiLine = true;
//				line = "";
//			}
//			if (multiLine) {
//				if (line.contains("*/")) {
//					multiLine = false;
//				}
//				line = "";
//			}
//			if (line.contains("//")) {
//				line = "";
//			}
//			result.append(line + "\n");
//		}
//		return result.toString();
		// remove comments
		while (diff.contains("/*")) {
			String temp = "",
				   temp2 = "";
			temp = diff.substring(0, diff.indexOf("/*")-1);
			temp2 = diff.substring(diff.indexOf("*/")+2);
			diff = temp + temp2;
		}
		return diff;
	}
	
}
