package com.max.jamoppinjection;

import org.eclipse.emf.ecore.EObject;
import org.emftext.language.java.containers.CompilationUnit;
import org.emftext.language.java.resource.java.util.JavaResourceUtil;

import preprocessing.diffs.Change;

/**
 * Shall validate Change objects within a Changes object contained by a PreprocessedDiff.
 * @author Max
 *
 */
public class ChangesValidator {
	
	private static final String tempClassNameForUnModifiedClasses = "___UNMODIFIED_CLASS___";

	public ChangesValidator() {
	}
	
	public static String getTempClassName() {
		return tempClassNameForUnModifiedClasses;
	}
//	
//	// only for testing!
//	private List<Member> members;
//	public void setMembers(List<Member> p_members) {
//		members = p_members;
//	}
	
	/**
	 * Returns the compilation unit as EObject, if transformation was successful, null otherwise.
	 * @param change
	 * @return
	 */
	public EObject validateChange(Change change) {
		// TODO just for units smaller than class and not import or package statements
		EObject abstractSyntaxTreeRoot = JavaResourceUtil.getResourceContent(
				wrapCodeWithClass(change.getChanges(), change.getClassName()));
		System.out.println("CHANGES::: \t" + change.getChanges());
		
		/*
		 * TODO compilation is mandatory but then the changes needn't to be applied to main compilation
		 * unit. Changes have to be parsed to ensure correctness and then have to be handed over to 
		 * DeltaJCreator for creating deltas. 
		 */
//		CompilationUnit cu = null;
//		System.out.println("is AST CU? " + (abstractSyntaxTreeRoot instanceof CompilationUnit));
		if (abstractSyntaxTreeRoot instanceof CompilationUnit) {
//			cu = (CompilationUnit) abstractSyntaxTreeRoot;						
//			List<ConcreteClassifier> classifiers2 = cu.getClassifiers();

//			for (ConcreteClassifier classifier2 : classifiers2) {
//				List<Member> members2 = classifier2.getMembers();
//
//				printMembers(members2);
//				System.out.println("Iterating classifiers...");
//				if (!members2.isEmpty()) {
//					return abstractSyntaxTreeRoot;
//				}
//				/*
//				 * important for not applying false related addRem flags to previous handled members, 
//				 * e.g. if an empty line shall be removed, the last added member is removed instead.
//				 */
//				if (!members2.isEmpty()) {
//					if (change.getAddRem() > 0) {
//						addMembers(members, members2);
//					} else if (change.getAddRem() < 0) {
//						removeMembers(members, members2);
//					}			
//				}
//				printMembers(members2);
//			}
			return abstractSyntaxTreeRoot;
		}
		return null;
	}
			
	private String wrapCodeWithClass(String code, String className ) {
		String firstPart = "";
		boolean addClassWord = true;
		try {
			if (code.contains("package") && code.contains("class")) {
				// if it is a later class addition, the code itself is suffiecient.
				addClassWord = false;
			}
			if (code.contains("import")) {
				/*
				 *  firstPart is from the beginning (because the package may not be added) to the 
				 *  semicolon of the last import statement. code is from the last import semicolon
				 *  to the end.
				 */
				firstPart = code.substring(0, code.indexOf(";", code.lastIndexOf("import"))+1);
				code = code.substring(code.indexOf(";", code.lastIndexOf("import"))+1);
			}
			/*
			 * Interface and superclass modifications are already in DeltaJ format. Thus, there keywords
			 * must be replaced again with their Java equivalents, which is implements for interfaces
			 * and extends for superclass. This does not affect the Change object from which the code 
			 * comes from.
			 */
			if (code.contains("interfaces")) {
				className += " " + code.replace("interfaces", "implements");
				code = "";
			}
			if (code.contains("superclass")) {
				className += " " + code.replace("superclass", "extends");
				code = "";
			}
		} catch (java.lang.StringIndexOutOfBoundsException sioobe) {
		}
		// if addClassWord is true, normal subclass changes, otherwise the code is already a complete class.
		code = addClassWord ? firstPart + "class " + className + " {\n\n" + code + "\n}" : code;
		return code;
	}
	
	// Only for testing!
//	static int rounds = 0;
//	/**
//	 * Adds one member to a list of members. Supposed to be the memberlist of the main compilation unit.
//	 * @param oldMembers - list of members contained in the main compilation unit
//	 * @param newMember - 
//	 */
//	private void addMembers(List<Member> oldMembers, List<Member> newMembers) {
//		rounds++;
//		while (newMembers.size() > 0) {
//			Member newMember = newMembers.get(0);
//			if (newMember instanceof Field) {
//				oldMembers.add(newMember);
//			}
//
//			if (newMember instanceof Constructor) {
//				oldMembers.add(newMember);
//			}
//
//			if (newMember instanceof Method) {
//				oldMembers.add(newMember);
//			}
//		}		
//	}
//	
//	//Only for testing!
//	/**
//	 * Removes the members listed in membersToBeRemoved from oldMembers which come from the main compilation unit.
//	 * @param oldMembers
//	 * @param membersToBeRemoved
//	 */
//	private void removeMembers(List<Member> oldMembers, List<Member> membersToBeRemoved) {
//		for (Member memberToBeRemoved : membersToBeRemoved) {
//			if (!oldMembers.contains(memberToBeRemoved)) {
//				continue;
//			}
//			if (memberToBeRemoved instanceof Field) {
//				oldMembers.remove(memberToBeRemoved);
//			}
//
//			if (memberToBeRemoved instanceof Constructor) {
//				oldMembers.remove(memberToBeRemoved);
//			}
//
//			if (memberToBeRemoved instanceof Method) {
//				oldMembers.remove(memberToBeRemoved);
//			}
//		}	
//	}
//	
//	private void printMembers(List<Member> members) {
//		for (Member member : members) {
//			String memberName = member.getName();
//			List<Modifier> modifiers = ((AnnotableAndModifiable) member).getModifiers();
//			
//			for (Modifier modifier : modifiers) {
//				System.out.println("Modifier: " + modifier.getClass().getSimpleName());
//				
//				if (modifier instanceof Public) {
//					//Public...
//				}
//			}
//			
//			if (member instanceof Field) {
//				System.out.println("Field: " + memberName);
//			}
//			
//			if (member instanceof Constructor) {
//				System.out.println("Constructor: " + memberName);
//			}
//			
//			if (member instanceof Method) {
//				System.out.println("Method: " + memberName);
//			}
//		}
//	}
}
