package preprocessing.diffs;

/**
 * This class is designed for represent all changes affecting the specified class.
 * @author Max
 *
 */
public class Change {

	// contains the class/file name
	private String className;
	// Marks the beginning line of the respective changes
	private int beginningLine;
	// Flags if the respective change is a modification, removal or an addition
	private byte addRem;
	// Contains the changes
	private String changes;
	
	public Change() {
		className = "";
		beginningLine = -1;
		addRem = -128;
		changes = null;
	}
	
	public Change(String p_className, byte p_addRem, int p_beginningLine, String p_changes) {
		className = p_className;
		addRem = p_addRem;
		beginningLine = p_beginningLine;
		changes = p_changes;
	}
	
	public void setClassFile(String p_className) {
		className = p_className;
	}
	
	public String getClassName() {
		return className;
	}
	
	/**
	 * Sets line number as int for beginning line. If beginning line is not 
	 * positive, nothing is done.
	 * @param beginningLine
	 */
	public void setBeginningLine(int p_beginningLine) {
		if (p_beginningLine > 0) {
			beginningLine = p_beginningLine;
		}
	}
	
	/**
	 * Returns beginning line, if not set or < 0, -1 is returned.
	 * @return
	 */
	public int getBeginningLine() {
		if (beginningLine > 0) {
			return beginningLine;
		}
		return -1;
	}
	
	/**
	 * Sets type of changes as byte. If addRem is -128, nothing is added. 
	 * Value -128 is reserved as standard value.
	 * zero (0) = modification
	 * positive values (1) = addition
	 * negative values (-1, !!except -128!!) = removal
	 * @param addRem
	 */
	public void setAddRem(byte p_addRem) {
		if (p_addRem != -128) {
			addRem = p_addRem;
		}
	}
	
	/**
	 * Returns addRem flag, if not set -128 (standard value) is returned.
	 * @return
	 */
	public byte getAddRem() {
		return addRem;
	}
	
	/**
	 * Adds code changes as String. If empty, nothing is added.
	 * @param changes
	 */
	public void setChanges(String p_changes) {
		if (p_changes != null && !p_changes.isEmpty()) {
			changes = p_changes;
		}
	}
	
	/**
	 * Returns code changes, if not set null is returned.
	 * @return
	 */
	public String getChanges() {
		return changes;
	}
	
	/**
	 * Adds a line number as int, type of changes as byte and code changes as String 
	 * to the respectively designated list. If no list object exists, nothing is added.
	 * @param beginningLine
	 * @param addRem
	 * @param change
	 */
	public void setChange(int beginningLine, byte addRem, String changes) {
		setBeginningLine(beginningLine);
		setAddRem(addRem);
		setChanges(changes);
	}
	
	/**
	 * Resets all values to their standards.
	 */
	public void clear() {
		beginningLine = -1;
		addRem = -128;
		changes = null;
	}
}
