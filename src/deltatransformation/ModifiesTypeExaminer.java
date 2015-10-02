package deltatransformation;

import org.deltaj.deltaJ.DeltaJFactory;
import org.deltaj.deltaJ.Source;
import org.deltaj.deltaJ.Sources;
import org.eclipse.emf.ecore.EObject;

import preprocessing.diffpreprocessor.ModificationType;

public class ModifiesTypeExaminer {

	private DeltaJFactory factory = DeltaJFactory.eINSTANCE;

	protected EObject examineModifiesType(ModificationType addRem, String modifyingCode) {
		EObject eom = null;
		if (modifyingCode.contains("import")) {
			if (addRem == ModificationType.ADDSIMPORT) {
				// for adding imports
				eom = factory.createAddsImport();
//				ImportDeclaration impdec = factory.createImportDeclaration();
//				impdec.setName(modifyingCode.trim());
			} else if (addRem == ModificationType.REMOVESIMPORT) {
				eom = factory.createRemovesImport();
			}
		} else if (modifyingCode.contains("implements")) {
			if (addRem == ModificationType.ADDSINTERFACE) {
				eom = factory.createAddsInterfacesList();
			} else if (addRem == ModificationType.REMOVESINTERFACE) {
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
			if (addRem == ModificationType.ADDSSUPERCLASS) {
				eom = factory.createAddsSuperclass();
			} else if (addRem == ModificationType.MODIFIESSUPERCLASS) {
				eom = factory.createModifiesSuperclass();
			} else if (addRem == ModificationType.REMOVESSUPERCLASS) {
				eom = factory.createRemovesSuperclass();
			}
		} 
		// else is for all member declarations
		else {
			eom = factory.createModifiesAction();
			if (addRem == ModificationType.ADDSMETHOD || addRem == ModificationType.ADDSFIELD) {
				// for adding class members (methods, fields)
				eom = factory.createAddsClassBodyMemberDeclaration();
			} else if (addRem == ModificationType.REMOVESFIELD) {
				eom = factory.createRemovesField();
			} else if (addRem == ModificationType.REMOVESMETHOD) {
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
	public ModificationType examineModifiesType(byte addRem, String modifiedCodeLine) {
//		if (addRem == -128) {
//			return null;
//		}
		if (modifiedCodeLine.contains("import")) {
			// there is no modifies import
			if (addRem > 0) {
//				return "maim"; 
				return ModificationType.ADDSIMPORT;
			} else if (addRem < 0) {
//				return "mrim";
				return ModificationType.REMOVESIMPORT;
			}
		} else if (modifiedCodeLine.contains("interfaces")) {
			// there is no modifies interface
			if (addRem > 0) {
//				return "main"; 
				return ModificationType.ADDSINTERFACE;
			} else if (addRem < 0) {
//				return "mrin";
				return ModificationType.REMOVESINTERFACE;
			}
		} else if (modifiedCodeLine.contains("superclass")) {
			if (addRem > 0) {
//				return "masc"; 
				return ModificationType.ADDSSUPERCLASS;
			} else if (addRem < 0) {
//				return "mrsc";
				
				return ModificationType.REMOVESSUPERCLASS;
			} else {
//				return "mmsc";
				return ModificationType.MODIFIESSUPERCLASS;
			}
		} 
		// if just an empty line is inserted, move on
		else if (modifiedCodeLine.matches("(\\+|-)(\\t|\\r|\\n)*")) {
//			return "";
		} else if (modifiedCodeLine.length()<3) {
//			return "";
		}
		// else is for all member declarations
		else {
//			if (addRem > 0) {
////				return "mam"; 
//				return ModificationType.ADDSMEMBER;
//			} else {
				// TODO does not support arrays yet!!!
				/*  optional modifier and mandatory (return) type, type may be everything using 
				 *  letters, numbers and underscores, may be qualified therefore the . in the
				 *  second group.
				 */
//				String fieldRegex = "((public|protected|private)?\\s(\\w|\\d|_|\\.){0,50}(\\w|\\d|_)+\\s"
//						// name and either semicolon or equals with a new object/primitive type.
//						+ "(\\w|\\d|_|\\.){0,50}(\\w|\\d|_)\\s*(;|=(\\s)*(\\w|\\d|_|\\.){0,50}(\\w|\\d|_)+;))"; 
////						 //method name with optional parameters
////						+ "(\\w|_|.){1,50}(\\w|_)\\s*\\(((\\w|_|.){1,50}(\\w|_),)*(\\w|_|.){1,50}(\\w|_)\\)";
//				Pattern p = Pattern.compile(fieldRegex);
//				Matcher m = p.matcher(modifiedCodeLine);
				// if field is found, return remove or modify field.
//				if (m.find()) {
				if (modifiedCodeLine.contains("{")) {
					if (addRem < 0) {
//						return "mrm";
						return ModificationType.REMOVESMETHOD;
					} else if (addRem > 0) {
//						return "mam"; 
						return ModificationType.ADDSMETHOD;
					} else {
//						return "mmm";
						return ModificationType.MODIFIESMETHOD;
					}
				} else {
					if (addRem < 0) {
//						return "mrf";
						return ModificationType.REMOVESFIELD;
					} else if (addRem > 0) {
//						return "mam"; 
						return ModificationType.ADDSFIELD;
					} else {
//						return "mmf";
						return ModificationType.MODIFIESFIELD;
					}
				}
				// otherwise return remove or modify method.
//				else {
//					if (addRem < 0) {
////						return "mrm";
//						return ModificationType.REMOVESMETHOD;
//					} else if (addRem > 0) {
////						return "mam"; 
//						return ModificationType.ADDSMETHOD;
//					} else {
////						return "mmm";
//						return ModificationType.MODIFIESMETHOD;
//					}
//				}
			}
//		}
//		return "a";
		if (addRem == 1) {
			return ModificationType.CLASSADDITION;
		} else if (addRem == -1) {
			return ModificationType.CLASSREMOVAL;
		} else {
			return null;
		}
	}
}
