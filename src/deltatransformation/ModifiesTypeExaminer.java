package deltatransformation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deltaj.deltaJ.DeltaJFactory;
import org.deltaj.deltaJ.Source;
import org.deltaj.deltaJ.Sources;
import org.eclipse.emf.ecore.EObject;

public class ModifiesTypeExaminer {

	private DeltaJFactory factory = DeltaJFactory.eINSTANCE;

	protected EObject examineModifiesType(String addRem, String modifyingCode) {
		EObject eom = null;
		if (modifyingCode.contains("import")) {
			if (addRem.equals("maim")) {
				// for adding imports
				eom = factory.createAddsImport();
//				ImportDeclaration impdec = factory.createImportDeclaration();
//				impdec.setName(modifyingCode.trim());
			} else if (addRem.equals("mrim")) {
				eom = factory.createRemovesImport();
			}
		} else if (modifyingCode.contains("implements")) {
			if (addRem.equals("main")) {
				eom = factory.createAddsInterfacesList();
			} else if (addRem.equals("mrin")) {
				eom = factory.createRemovesInterfacesList();
			}
			
			//for interfaces
//			AddsInterfacesList interfaces = factory.createAddsInterfacesList();
//			TypeList tl = factory.createTypeList();
//			Type t = factory.createType();
//			t.getArray().add(modifyingCode.trim());
//			tl.getTypes().add(t);
//			interfaces.setInterfaces(tl);
		} else if (modifyingCode.contains("extends")) {
			if (addRem.equals("masc")) {
				eom = factory.createAddsSuperclass();
			} else if (addRem.equals("mmsc")) {
				eom = factory.createModifiesSuperclass();
			} else if (addRem.equals("mrsc")) {
				eom = factory.createRemovesSuperclass();
			}
		} 
		// else is for all member declarations
		else {
			eom = factory.createModifiesAction();
			if (addRem.equals("mam")) {
				// for adding class members (methods, fields)
				eom = factory.createAddsClassBodyMemberDeclaration();
			} else if (addRem.equals("mrf")) {
				eom = factory.createRemovesField();
			} else if (addRem.equals("mrm")) {
				eom = factory.createRemovesMethod();
			}

			Source s = factory.createSource();
			Sources ss = factory.createSources();
			s.setDelta(modifyingCode.trim());
			ss.getSources().add(s);
		}
		
		return eom;
	}
	
	/**
	 * Computes key word for delta action based on addRem flag and modifiedCodeLine.
	 * addRem indicates whether it is an addition, a modification or removal while 
	 * modifiedCodeLine is searched for keywords for determination of kind of change.
	 * For example a removal has to be further determined regarding to the kind of changes
	 * like import or method etc.
	 * 
	 * Key words:
	 * a = addition (class), r = removal (class), m = modifies class. Modifies has a two
	 * level syntax as following: mam = add member, maim = add import, main = add interfaces 
	 * list, masc = add super class, mmm = modify method, mmf = modify field, mmp = modify 
	 * package, mmsc = modify super class, mrm = remove method, mrf = remove field, 
	 * mrim = remove import, mrin = remove interfaces list.
	 * @param addRem
	 * @param modifiedCodeLine
	 * @return key word for change computed from both parameters.
	 */
	public String examineModifiesType(byte addRem, String modifiedCodeLine) {
		if (modifiedCodeLine.contains("import")) {
			// there is no modifies import
			if (addRem > 0) {
				return "maim"; 
			} else if (addRem < 0) {
				return "mrim";
			}
		} else if (modifiedCodeLine.contains("interfaces")) {
			// there is no modifies interface
			if (addRem > 0) {
				return "main"; 
			} else if (addRem < 0) {
				return "mrin";
			}
		} else if (modifiedCodeLine.contains("superclass")) {
			if (addRem > 0) {
				return "masc"; 
			} else if (addRem < 0) {
				return "mrsc";
			} else {
				return "mmsc";
			}
		} 
		// if just an empty line is inserted, move on
		else if (modifiedCodeLine.matches("(\\+|-)(\\t|\\r|\\n)*")) {
			return "";
		} else if (modifiedCodeLine.length()<3) {
			return "";
		}
		// else is for all member declarations
		else {
			if (addRem > 0) {
				return "mam"; 
			} else {
				// TODO does not support arrays yet!!!
				/*  optional modifier and mandatory (return) type, type may be everything using 
				 *  letters, numbers and underscores, may be qualified therefore the . in the
				 *  second group.
				 */
				String fieldRegex = "((public|protected|private)?\\s(\\w|\\d|_|\\.){0,50}(\\w|\\d|_)+\\s"
						// name and either semicolon or equals with a new object/primitive type.
						+ "(\\w|\\d|_|\\.){0,50}(\\w|\\d|_)\\s*(;|=(\\s)*(\\w|\\d|_|\\.){0,50}(\\w|\\d|_)+;))"; 
//						 //method name with optional parameters
//						+ "(\\w|_|.){1,50}(\\w|_)\\s*\\(((\\w|_|.){1,50}(\\w|_),)*(\\w|_|.){1,50}(\\w|_)\\)";
				Pattern p = Pattern.compile(fieldRegex);
				Matcher m = p.matcher(modifiedCodeLine);
				// if field is found, return remove or modify field.
				if (m.find()) {
					if (addRem < 0) {
						return "mrf";
					} else {
						return "mmf";
					}
				}
				// otherwise return remove or modify method.
				else {
					if (addRem < 0) {
						return "mrm";
					} else {
						return "mmm";
					}
				}
			}
		}
		return "a";
	}
}
