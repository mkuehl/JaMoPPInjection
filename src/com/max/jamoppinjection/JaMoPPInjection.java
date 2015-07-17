package com.max.jamoppinjection;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.deltaj.deltaJ.Delta;
import org.deltaj.deltaJ.DeltaJUnit;
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
import preprocessing.diffs.Change;
import preprocessing.diffs.Changes;
import preprocessing.gitconnector.GitConnectorCmdL;
import deltatransformation.DeltaJCreator;

public class JaMoPPInjection {
	
	public static void run() {
		//Get (valid) Java source code from somewhere.
		String code = "";		

		// Connector to git to clone, extract code base and diffs.
		GitConnectorCmdL gcl = new GitConnectorCmdL("", "");
		// cleans the code base and diffs and creates a memory table for knowing lines of methods and fields.
		DiffPreprocessor diffPre = new DiffPreprocessor();
		
		// create code base to which later changes shall be applied
		// TODO adjust name and path to your flavor and system
		gcl.extractCodeBase("E:\\programmaticallyCreatedGitRepo", "HEAD~12");
		diffPre.setInput(gcl.getCodeBase());
		diffPre.cleanInput();
		diffPre.separateChanges();

		// for code base
		Changes codeBases = diffPre.getPrepDiff().next();
		for (Change base : codeBases) {
			code += base.getChanges();
		}
		
		//Parse using JaMoPP
		EObject abstractSyntaxTreeRoot = JavaResourceUtil.getResourceContent(code);
		
		DeltaJCreator djc = new DeltaJCreator();
		
		DeltaJUnit dju = djc.createDeltaJUnit();
		Delta d = djc.createNewDelta(dju, "coredelta");
		
		djc.addJavaUnit(d, abstractSyntaxTreeRoot, (byte) 1);
		
		// TODO adjust name and path to your flavor and system
		djc.createDeltaFile("PrintClassCoreDelta", "E:\\DeltaJ-workspace\\PrintClassDelta", d);
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

				if (!members.get(2).getComments().isEmpty()) {
					System.out.println(members.get(2).getComments().get(0));
				}
				
				// print class name
				System.out.println("Classifier: " + classifierName);
				
				System.out.println("base code:");
				printMembers(members);
				printString(comments);

//				diffPre.resetPrepDiff();
				gcl.executeDiff("E:\\programmaticallyCreatedGitRepo", 10, 0);
				diffPre.setInput(gcl.getDiff());
				System.out.println();
				diffPre.cleanInput();
				diffPre.separateChanges();
				
				Changes changes = diffPre.getPrepDiff().next();
				
				// go through changes from last to first
				changes.setToLast();
				while (changes.hasPrevious()) {
					Change change = changes.previous();
					// TODO just for units smaller than class and not import or package statements
					EObject abstractSyntaxTreeRoot2 = JavaResourceUtil.getResourceContent(
							wrapCodeWithClass(change.getChanges()));
					
					/*
					 * TODO compilation is mandatory but then the changes needn't to be applied to main compilation
					 * unit. Changes have to be parsed to ensure correctness and then have to be handed over to 
					 * DeltaJCreator for creating deltas. 
					 */
					CompilationUnit cu2 = null;
					if (abstractSyntaxTreeRoot2 instanceof CompilationUnit) {
						cu2 = (CompilationUnit) abstractSyntaxTreeRoot2;						
						List<ConcreteClassifier> classifiers2 = cu2.getClassifiers();
						
						for (ConcreteClassifier classifier2 : classifiers2) {
							List<Member> members2 = classifier2.getMembers();
							/*
							 * important for not applying false related addRem flags to previous handled members, 
							 * e.g. if an empty line shall be removed, the last added member is removed instead.
							 */
							if (!members2.isEmpty()) {
								if (change.getAddRem() > 0) {
									addMembers(members, members2);
								} else if (change.getAddRem() < 0) {
									removeMembers(members, members2);
								}
							}							
						}
					}
				}
				
				System.out.println("\n");
				System.out.println("modified code:");
				printMembers(members);
				printString(comments);
			}
			System.out.println("Members added " + rounds + " times");
			

			System.out.println(JavaResourceUtil.getText(abstractSyntaxTreeRoot));
			
		}
	}
	
	private static String wrapCodeWithClass(String code) {
		String firstPart;
		try {
			firstPart = code.substring(0, code.indexOf(";", code.lastIndexOf("import")));
			code = code.substring(code.lastIndexOf("import")+1);
		} catch (java.lang.StringIndexOutOfBoundsException sioobe) {
			firstPart = "";
		}
		code = firstPart + "public class {\n\n" + code + "\n}";
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

	static int rounds = 0;
	/**
	 * Adds one member to a list of members. Supposed to be the memberlist of the main compilation unit.
	 * @param oldMembers - list of members contained in the main compilation unit
	 * @param newMember - 
	 */
	private static void addMembers(List<Member> oldMembers, List<Member> newMembers) {
		rounds++;
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
