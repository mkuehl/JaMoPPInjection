package com.max.jamoppinjection;


import preprocessing.gitconnector.GitConnectorCmdL;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.containers.CompilationUnit;
import org.emftext.language.java.members.Constructor;
import org.emftext.language.java.members.Field;
import org.emftext.language.java.members.Member;
import org.emftext.language.java.members.Method;
import org.emftext.language.java.modifiers.AnnotableAndModifiable;
import org.emftext.language.java.modifiers.Modifier;
import org.emftext.language.java.modifiers.Public;
import org.emftext.language.java.resource.java.util.JavaResourceUtil;

import preprocessing.diffpreprocessor.DiffPreprocessor;
import preprocessing.diffs.Changes;

public class JaMoPPInjection {
	public static void run() {
		//Get (valid) Java source code from somewhere.
		String code = "";
		
		code += "package myExample;\n";
		code += "\n";
		code += "public class PrintClass {\n";
		code += "\n";
		/*
		 *  if the two variables below the multiline comment (MLC) are not commented out, only the latter comment is 
		 *  shown at the end, when everything will get printed. The comment list is unmodifiable and thus there has 
		 *  to be another way to add comments. 
		 *  It seems that the comments are added to the following member. E.g. the MLC is part of numberToPrint.
		 */
		code += "/*This is the test class used to evaluate successive insertions.\n"
				+ "  The insertions can be members or comments at this time. \n"
				+ "  Later also removals shall be evaluated.*/\n\n";
		code += "	private int numberToPrint;\n";
		code += "	protected byte byteTest;";
		code += "//These removals include also comments and members.\n";
//		code += "\n";
		/*
		 * next to lines are just to show, that complete parsing works for the same methods that are supposed to 
		 * be injected later.
		 */
		code += "	/** Sets the number to be printed by \n"
				+ "		* invocation of printNumber().\n"
				+ "		*/";
		code += "	public void setNumberToPrint(int p_numberToPrint) {\n	numberToPrint = p_numberToPrint;\n	}\n";
//		code += "	public int increment(int p_numberToIncrement) {\n	"
//				+ "int numberToIncrement = p_numberToIncrement + 1;\n return numberToIncrement;\n	}\n";
		code += "}";
		
		//Parse using JaMoPP
		EObject abstractSyntaxTreeRoot = JavaResourceUtil.getResourceContent(code);
		
		//Do something with the AST.
		//For information on the AST structure consult:
		//- metamodel in org.emftext.language.java/metamodel/java.ecore
		//- concrete syntax in org.emftext.language.java/metamodel/java.cs
		if (abstractSyntaxTreeRoot instanceof CompilationUnit) {
			CompilationUnit compilationUnit = (CompilationUnit) abstractSyntaxTreeRoot;
			
			List<ConcreteClassifier> classifiers = compilationUnit.getClassifiers();
			
			for (ConcreteClassifier classifier : classifiers) {
				String classifierName = classifier.getName();
				
				List<Member> members = classifier.getMembers();
				List<String> comments = classifier.getComments();
				
//				System.out.println("\n\nDEEP STRUCTURE READING ATTEMPT:\n\n");
//				TreeIterator<EObject> ti = members.get(0).eAllContents();
//				while(ti.hasNext()) {
//					System.out.println("Memberelement: " + ti.next().toString());
//				}
				System.out.println(members.get(2).getComments().get(0));
				
				// print class name
				System.out.println("Classifier: " + classifierName);
				
				System.out.println("base code:");
				printMembers(members);
				printString(comments);

				GitConnectorCmdL gcl = new GitConnectorCmdL("", "");
				DiffPreprocessor diffPre = new DiffPreprocessor();
				
				gcl.executeDiff("E:\\programmaticallyCreatedGitRepo", 20, 2);
				diffPre.setInput(gcl.getDiff());
				System.out.println();
				diffPre.cleanInput();
				diffPre.separateChanges();
				
				Changes changes = diffPre.getPrepDiff().next();
//				for (Change change : changes) {
//					System.out.println(change.getAddRem() + "" + change.getChanges());
//				}
				changes.setToLast();
				while (changes.hasPrevious()) {
					EObject abstractSyntaxTreeRoot2 = JavaResourceUtil.getResourceContent(
							wrapCodeWithClass(changes.getChange().getChanges()));
					CompilationUnit cu2 = null;
					if (abstractSyntaxTreeRoot2 instanceof CompilationUnit) {
						cu2 = (CompilationUnit) abstractSyntaxTreeRoot2;

						List<ConcreteClassifier> classifiers2 = cu2.getClassifiers();
						
						//TODO bring insertions in correct order within the AST. 
						for (ConcreteClassifier classifier2 : classifiers2) {
							List<Member> members2 = classifier2.getMembers();
							// comments are not modifiable, so this approach is useless.
//							List<String> comments2 = classifier2.getComments();
//							System.out.println("CU2 MEMBERS:\t" + changes.getChange().getChanges() 
//									+ "\tto be removed? " + changes.getChange().getAddRem() 
//									+ "\n");
//							printMembers(members2);
//							printString(comments2);
							/*
							 * important for not applying false related addRem flags to previous handled members, 
							 * e.g. if an empty line shall be removed, the last added member is removed instead.
							 */
							if (!members2.isEmpty()) {
								if (changes.getChange().getAddRem() > 0) {
									addMembers(members, members2);
								} else if (changes.getChange().getAddRem() < 0) {
									removeMembers(members, members2);
								}
							}
						}
					}
					changes.previous();
				}
				
//				Method method = (Method) getResourceContent("public int increment(int p_numberToIncrement) {\n	"
//						+ "int numberToIncrement = p_numberToIncrement + 1; return numberToIncrement;	}", 
//						MembersPackage.eINSTANCE.getClassMethod());
//				if (method != null ) {
//					members.add(method);
//				}
//				method = (Method) getResourceContent("public void setNumberToPrint(int p_numberToPrint) {\n	"
//						+ "numberToPrint = p_numberToPrint;\n	}\n", MembersPackage.eINSTANCE.getClassMethod());
//				if (method != null) {
//					members.add(method);
//				}
//				Method method = (Method) getResourceContent("public void a() {\n\n}\n", 
//						MembersPackage.eINSTANCE.getClassMethod());
//				if (method != null) {
//					members.add(method);
//				}
//				// Try parsing constructor, works well.
//				Constructor constructor = (Constructor) getResourceContent("public PrintClass(int p_numberToPrint) {"
//						+ "	numberToPrint = p_numberToPrint;	}", MembersPackage.eINSTANCE.getConstructor());
//				if(constructor != null) {
//					members.add(constructor);
//				}
//				// Try parsing field, even with initialisation. No problem.
//				Field field1 = (Field) getResourceContent("private int testnumber; testnumber = 6;", 
//						MembersPackage.eINSTANCE.getField());
//				if (field1 != null) {
//					members.add(field1);
//				}
				
				System.out.println("\n");
				System.out.println("modified code:");
				printMembers(members);
				printString(comments);
//				System.out.println("\n\nDEEP STRUCTURE READING ATTEMPT:\n\n");
//				TreeIterator<EObject> ti = compilationUnit.eAllContents();
//				while(ti.hasNext()) {
//					System.out.println(ti.next().toString());
//				}
			}
		}
	}
	
	private static String wrapCodeWithClass(String code) {
		code = "public class {\n\n" + code + "\n}";
		return code;
	}
	
	private static void printMembers(List<Member> members) {
		for (Member member : members) {
			String memberName = member.getName();
			List<Modifier> modifiers = ((AnnotableAndModifiable) member).getModifiers();
			
			for (Modifier modifier : modifiers) {
				System.out.println("Modifier: " + modifier.getClass().getSimpleName());
				
				if (modifier instanceof Public) {
					//Public...
				}
			}
			
			if (member instanceof Field) {
				System.out.println("Field: " + memberName);
			}
			
			if (member instanceof Constructor) {
				System.out.println("Constructor: " + memberName);
			}
			
			if (member instanceof Method) {
				System.out.println("Method: " + memberName);
			}
		}
	}
	
	private static void printString(List<String> texts) {
		for (String s : texts) {
			System.out.println("Comment: " + s);
		}
	}

	/**
	 * Adds one member to a list of members. Supposed to be the memberlist of the main compilation unit.
	 * @param oldMembers - list of members contained in the main compilation unit
	 * @param newMember - 
	 */
	private static void addMembers(List<Member> oldMembers, List<Member> newMembers) {
		while (newMembers.size() > 0) {
			Member newMember = newMembers.get(0);
			if (newMember instanceof Field) {
				oldMembers.add(newMember);
			}

			if (newMember instanceof Constructor) {
				oldMembers.add(newMember);
			}

			if (newMember instanceof Method) {
				oldMembers.add(newMember);
			}
		}		
	}
	
	/**
	 * Removes the members listed in membersToBeRemoved from oldMembers which come from the main compilation unit.
	 * @param oldMembers
	 * @param membersToBeRemoved
	 */
	private static void removeMembers(List<Member> oldMembers, List<Member> membersToBeRemoved) {
		for (Member memberToBeRemoved : membersToBeRemoved) {
			if (!oldMembers.contains(memberToBeRemoved)) {
				continue;
			}
			if (memberToBeRemoved instanceof Field) {
				oldMembers.remove(memberToBeRemoved);
			}

			if (memberToBeRemoved instanceof Constructor) {
				oldMembers.remove(memberToBeRemoved);
			}

			if (memberToBeRemoved instanceof Method) {
				oldMembers.remove(memberToBeRemoved);
			}
		}	
	}
	
	// Your method
    public static Resource getResource(byte[] content, ResourceSet resourceSet, Map<?, ?> loadOptions) {

        org.emftext.language.java.resource.java.mopp.JavaMetaInformation metaInformation = new org.emftext.language.java.resource.java.mopp.JavaMetaInformation();

        metaInformation.registerResourceFactory();

        URI uri = URI.createURI("temp." + metaInformation.getSyntaxName());

        Resource resource = resourceSet.createResource(uri);

        if (resource == null) {
            return null;
        }

        ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
        try {
            resource.load(inputStream, loadOptions);
        } catch (IOException ioe) {
        	ioe.printStackTrace();
            return null;
        } catch (NullPointerException npe) {
        	npe.printStackTrace();
        	return null;
        }
        return resource;
    }

    // Your method
    public static EObject getResourceContent(String text, EClass startEClass) {
        Map<Object, Object> loadOptions = new LinkedHashMap<Object, Object>();
        if (startEClass != null) {
            loadOptions.put(org.emftext.language.java.resource.java.IJavaOptions.RESOURCE_CONTENT_TYPE, startEClass);
        }

        Resource resource = getResource(text.getBytes(), new ResourceSetImpl(), loadOptions);
        if (resource == null) {
            return null;
        }

        List<EObject> contents = resource.getContents();
        if (contents == null || contents.isEmpty()) {
            return null;
        }
        EObject root = contents.get(0);
        return root;

    }
}
