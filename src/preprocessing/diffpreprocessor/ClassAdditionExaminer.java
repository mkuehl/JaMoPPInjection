package preprocessing.diffpreprocessor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassAdditionExaminer {

	/**
	 * Checks if all lines are added. If so, a new class is added.
	 * @param diff
	 * @return
	 */
	protected static boolean isWholeClassAdded(String diff) {
		// leading + followed by optional modifier, class and the class name.
		String regex = "(\\+(\\s)*(public|protected|private)?\\sclass\\s(\\w|\\d|_)+)";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(diff);
		String[] lines = diff.split("\\r?\\n");
		for (String line : lines) {
			if (!line.startsWith("+")) {
				return false;
			}
		}
		return m.find();
	}
	
	protected static boolean isWholeClassRemoved(String diff) {
		// leading + followed by optional modifier, class and the class name.
		String regex = "(-(\\s)*(public|protected|private)?\\sclass\\s(\\w|\\d|_)+)";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(diff);
		String[] lines = diff.split("\\r?\\n");
		for (String line : lines) {
			if (!line.startsWith("-")) {
				return false;
			}
		}
		return m.find();
	}
}
