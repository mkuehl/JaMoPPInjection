package preprocessing.diffpreprocessor;

/**
 * Class for checking if all lines are added or removed and for extracting new parts of a line,
 * which are not visible to the program since the whole line is marked as removed and added with 
 * the newly introduced changes.
 * @author Max
 *
 */
public class LineChecker {

	/**
	 * Removes all substrings contained in lineBefore and separated by the space character from actualLine.
	 * Returns just the parts that are not found in actualLine.
	 * Supposed to be used within DiffPreprocessor.separateChanges()
	 * @param actualLine - line to check/remove parts from.
	 * @param lineBefore - old parts of actual line.
	 * @return
	 */
	public String getNewPartsOfLine(String actualLine, String lineBefore) {
		String newParts = actualLine;
		String[] partsLineBefore = lineBefore.split("\\s");
		for (String p : partsLineBefore) {
			if (p.equals("extends") || p.equals("implements")) {
				continue;
			}
			newParts = newParts.replace(p, "");
		}
		newParts = newParts.trim();
		if (newParts.contains("extends implements")) {
			newParts = newParts.replace("extends", "");
		}
		if (newParts.endsWith("implements")) {
			newParts = newParts.replace("implements", "");
		}
		return newParts;
	}
	
	/**
	 * 	/**
	 * Removes all substrings contained in the shorter line and separated by the space character from the longer line.
	 * Returns just the parts that are not found in actualLine.
	 * Supposed to be used within DiffPreprocessor.separateChanges()
	 * @param actualLine - line to check/remove parts from.
	 * @param lineBefore - old parts of actual line.
	 * @return
	 */
	public String getChangedPartsOfLine(String actualLine, String lineBefore) {
		
		String changedParts,
			   shorterLine;
		if (actualLine.length()>lineBefore.length()) {
			changedParts = actualLine;
			shorterLine = lineBefore;
		} else {
			changedParts = lineBefore;
			shorterLine = actualLine;
		}
		String[] partsLineBefore = shorterLine.split("\\s");
		for (String p : partsLineBefore) {
			changedParts = changedParts.replace(p, "");
		}
		return changedParts;
	}
	
	public String getSuperclassFromLineArray(String[] addedParts) {
		String superclass = "";
		for (int i = 0; i < addedParts.length; i++) {
			if (addedParts[i].equals("")) {
				continue;
			}
			if (addedParts[i].equals("extends")) {
				// codewords are removed when creating Change object
				superclass = "superclass " + addedParts[++i];
			}
		}
		return superclass;
	}
	
	/**
	 * Looks sets the addedParts together to a comma-separated String which represents the added interfaces.
	 * @param addedParts
	 * @return
	 */
	public String getInterfacesFromLineArray(String[] addedParts) {
		String interfaces = "";
		for (int i = 0; i < addedParts.length; i++) {
			// if extends contained, there must also be a superclass. Skip both.
			if (addedParts[i].equals("extends")) {
				i++;
				continue;
			}
			// if not implements, then add to String.
			if (addedParts[i].equals("implements") || addedParts[i].equals("")) {
				continue;
			}

			interfaces += ", " + addedParts[i];
		}
		// codewords are removed when creating Change object
		interfaces = "interfaces " + interfaces.substring(1).trim();
		return interfaces;
	}
	
	/**
	 * Checks if a whole class is affected, i.e. all lines start with + or -. If so, true is returned.
	 * @param diffCode
	 * @return
	 */
	public boolean isWholeClass(String diffCode) {
		String[] lines = diffCode.split("\\r?\\n");
		boolean firstLineAffected = true;
		int count = lines.length,
			plus = 0,
			minus = 0;
		for (int i = 0; i < lines.length; i++) {
			if (i==0 && (lines[i].equals("\n") || lines[i].equals(""))) {
				firstLineAffected = false;
				continue;
			}
			if (lines[i].startsWith("+")) {
				plus++;
			} else if (lines[i].startsWith("-")) {
				minus++;
			} else if (lines[i].equals("\\ No newline at end of file")) {
			//everything that might come in subsequent lines won't affect the modification anymore.
				//subtract one for the actual line and one if the first line had a modification sign.
				count = i - (firstLineAffected ? 0 : 1);
				break;
			} else {
				return false;
			}
		}
		if (count == plus || count == minus) {
			return true;
		}
		return false;
	}
}
