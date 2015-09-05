package preprocessing.diffpreprocessor;

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
			newParts = newParts.replace(p, "");
		}
		return newParts;
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
		interfaces = "interfaces " + interfaces.substring(1);
		return interfaces;
	}
}
