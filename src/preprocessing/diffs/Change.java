package preprocessing.diffs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import preprocessing.diffpreprocessor.ModificationType;
import projecttree.Node;

/**
 * This class is designed for represent all changes affecting the specified class.
 * @author Max
 *
 */
public class Change {

	// contains the class/file name
	private String qualifiedClassName;
	// Marks the beginning line of the respective changes
	private int beginningLine;
	// Flags if the respective change is a modification, removal or an addition
	private byte addRem;
//	private String typeOfChange;
	private ModificationType typeOfChange;
	private boolean isWholeClass;
	private boolean isMethodModifiedAtStart;
	private Node modifiedMethod;
	// Contains the changes
	private String changes;
	
	public Change() {
		qualifiedClassName = "";
		beginningLine = -1;
		addRem = -128;
		typeOfChange = null;
		isWholeClass = false;
		setIsMethodModifiedAtStart(false);
		setModifiedMethod(null);
		changes = null;
	}
	
	/**
	 * Parameterized constructor. If p_qualifiedClassName or p_typeOfChange do not correspond
	 * to their respective keyword catalogue, they are simply not set.
	 * While p_qualifiedClassName has just to correspond to (package.)+class (at least one 
	 * package and a class name), p_typeOfChange has the following specified 
	 * list: a = addition (class), r = removal (class), m = modifies class. Modifies has a two
	 * level syntax as following: mam = add member, maim = add import, main = add interfaces 
	 * list, masc = add super class, mmm = modify method, mmf = modify field, mmp = modify 
	 * package, mmsc = modify super class, mrm = remove method, mrf = remove field, 
	 * mrim = remove import, mrin = remove interfaces list.
	 *
	 * @param p_qualifiedClassName
	 * @param p_beginningLine
	 * @param p_addRem
	 * @param p_typeOfChange
	 * @param p_changes
	 */
	public Change(String p_qualifiedClassName, int p_beginningLine, byte p_addRem, 
			ModificationType p_typeOfChange, boolean p_isWholeClass, String p_changes) {
		setQualifiedClassName(p_qualifiedClassName);
		beginningLine = p_beginningLine;
		addRem = p_addRem;
		setTypeOfChange(p_typeOfChange);
		isWholeClass = p_isWholeClass;
		changes = p_changes;
	}
	
	/**
	 * p_qualifiedClassName has to correspond to (package.)+class 
	 * (at least one package and a class name)
	 * @param p_qualifiedClassName
	 */
	public void setQualifiedClassName(String p_qualifiedClassName) {
		if (qualifiedClassNameValidator(p_qualifiedClassName)) {
			qualifiedClassName = p_qualifiedClassName;
		}
	}
	
	/**
	 * Returns package and classname.
	 * @return
	 */
	public String getQualifiedClassName() {
		return qualifiedClassName;
	}
	
	/**
	 * Returns class name only.
	 * @return
	 */
	public String getClassName() {
		return qualifiedClassName.substring(qualifiedClassName.lastIndexOf(".")+1, qualifiedClassName.length());
	}
	
	/**
	 * Returns fully qualified package name only.
	 * @return
	 */
	public String getPackageName() {
		return qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf("."));
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
	 * p_typeOfChange has the following specified 
	 * list: a = addition (class), r = removal (class), m = modifies class. Modifies has a two
	 * level syntax as following: mam = add member, maim = add import, main = add interfaces 
	 * list, masc = add super class, mmm = modify method, mmf = modify field, mmp = modify 
	 * package, mmsc = modify super class, mrm = remove method, mrf = remove field, 
	 * mrim = remove import, mrin = remove interfaces list.
	 * @param p_typeOfChange
	 */
	public void setTypeOfChange(ModificationType p_typeOfChange) {
//		if (typeOfChangeValidator(p_typeOfChange)) {
			typeOfChange = p_typeOfChange;
//		}
	}
	
	public ModificationType getTypeOfChange() {
		return typeOfChange;
	}
	
	public void setIsWholeClass(boolean p_isWholeClass) {
		isWholeClass = p_isWholeClass;
	}
	
	public boolean getIsWholeClass() {
		return isWholeClass;
	}
	

	public boolean getIsMethodModifiedAtStart() {
		return isMethodModifiedAtStart;
	}

	public void setIsMethodModifiedAtStart(boolean isMethodModifiedAtStart) {
		this.isMethodModifiedAtStart = isMethodModifiedAtStart;
	}

	public Node getModifiedMethod() {
		return modifiedMethod;
	}

	public void setModifiedMethod(Node modifiedMethod) {
		this.modifiedMethod = modifiedMethod;
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
	
	public void appendChanges(String p_changesAppendage) {
		if (p_changesAppendage != null && !p_changesAppendage.isEmpty()) {
			changes += p_changesAppendage;
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
	 * While p_qualifiedClassName has just to correspond to (package.)+class (at least one 
	 * package and a class name), p_typeOfChange has the following specified 
	 * list: a = addition (class), r = removal (class), m = modifies class. Modifies has a two
	 * level syntax as following: mam = add member, maim = add import, main = add interfaces 
	 * list, masc = add super class, mmm = modify method, mmf = modify field, mmp = modify 
	 * package, mmsc = modify super class, mrm = remove method, mrf = remove field, 
	 * mrim = remove import, mrin = remove interfaces list.
	 * 
	 * @param beginningLine
	 * @param addRem
	 * @param change
	 */
	public void setChange(String p_qualifiedClassName, int beginningLine, byte addRem, 
			ModificationType p_typeOfChange, boolean p_isWholeClass, String changes) {
		setQualifiedClassName(p_qualifiedClassName);
		setBeginningLine(beginningLine);
		setAddRem(addRem);
		setTypeOfChange(p_typeOfChange);
		setIsWholeClass(p_isWholeClass);
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
	
	private boolean qualifiedClassNameValidator(String p_qualifiedClassName) {
		String regex = "((\\w|_){1,50})+(.)+(\\w|_){1,50}";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(p_qualifiedClassName);
		return m.matches();
	}
	
//	/**
//	 * Checks if the String typeOfChange matches its designated form.
//	 * @param p_typeOfChange
//	 * @return true, if the p_typeOfChange matches a defined modification keyword.
//	 */
//	private boolean typeOfChangeValidator(String p_typeOfChange) {
//		if (p_typeOfChange == null) {
//			return false;
//		}
//		String regex = "(a|r|m(am|aim|ain|asc|mm|mf|mp|msc|rm|rf|rim|rin))";
//		Pattern p = Pattern.compile(regex);
//		Matcher m = p.matcher(p_typeOfChange);
//		return m.matches();
//	}
}
