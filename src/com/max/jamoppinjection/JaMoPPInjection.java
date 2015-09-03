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
import org.emftext.language.java.resource.java.util.JavaResourceUtil;

import preprocessing.diffpreprocessor.DiffPreprocessor;
import preprocessing.diffs.Change;
import preprocessing.diffs.Changes;
import preprocessing.diffs.PreprocessedDiff;
import preprocessing.gitconnector.GitConnectorCmdL;
import deltatransformation.DeltaJCreator;

public class JaMoPPInjection {
	
	public static void run() {
		//Get (valid) Java source code from somewhere.
<<<<<<< HEAD
		String code = "";	
		int i = 0;
=======
		String code = "";		
>>>>>>> dcd1a3b06a7c10071d7c4811488003e91ce11a3b

		// Connector to git to clone, extract code base and diffs.
		GitConnectorCmdL gcl = new GitConnectorCmdL("", "");
		// cleans the code base and diffs and creates a memory table for knowing lines of methods and fields.
		DiffPreprocessor diffPre = new DiffPreprocessor();
		
		// create code base to which later changes shall be applied
		// TODO adjust name and path to your flavor and system
		gcl.getRepo("E:\\programmaticallyCreatedGitRepo\\", "https://github.com/mkuehl/TestRepo.git");
<<<<<<< HEAD
		gcl.extractCodeBase("E:\\programmaticallyCreatedGitRepo\\", "HEAD~7", /*"Printer.java"*/"");
		diffPre.setInput(gcl.getCodeBase());
		diffPre.preprocessCodeBase();
=======
<<<<<<< HEAD
		gcl.extractCodeBase("E:\\programmaticallyCreatedGitRepo\\", "HEAD~6", "Printer.java");
=======
		gcl.extractCodeBase("E:\\programmaticallyCreatedGitRepo\\", "HEAD~3", "Printer.java");
>>>>>>> d299962c6699a7899a042184feb16b9d25c6636b
		diffPre.setInput(gcl.getCodeBase());
//		diffPre.separateChanges();
		diffPre.preprocessCodeBase();

		// for code base
		Changes codeBases = diffPre.getPrepDiff().next();
		for (Change base : codeBases) {
			code += base.getChanges();
		}
>>>>>>> dcd1a3b06a7c10071d7c4811488003e91ce11a3b
		
		DeltaJCreator djc = new DeltaJCreator();
		
<<<<<<< HEAD
		DeltaJUnit dju = djc.createDeltaJUnit();

		// for code base
		Changes codeBases = diffPre.getPrepDiff().next();
		for (Change base : codeBases) {
			code += base.getChanges();
			
			//Parse using JaMoPP
//			abstractSyntaxTreeRoots.add(JavaResourceUtil.getResourceContent(code));

			Delta d = djc.createNewDelta(dju, "coredelta" + i++);
			djc.addJavaUnit(d, JavaResourceUtil.getResourceContent(code), "", "", (byte) 1, "a");
			code = "";

			// TODO adjust name and path to your flavor and system
			djc.addToDeltaString(d, new Change());
			djc.closeDeltaString();
		}

		djc.write("PrintClassCoreDelta", "E:\\DeltaJ-workspace\\PrintClassDelta");
	
=======
		DeltaJCreator djc = new DeltaJCreator();
		
		DeltaJUnit dju = djc.createDeltaJUnit();
		Delta d = djc.createNewDelta(dju, "coredelta");
		
		djc.addJavaUnit(d, abstractSyntaxTreeRoot, "", "", (byte) 1);
		
		// TODO adjust name and path to your flavor and system
<<<<<<< HEAD
		djc.createDeltaFile("PrintClassCoreDelta", "E:\\DeltaJ-workspace\\PrintClassDelta", d, new Change());
=======
		djc.createDeltaFile("PrintClassCoreDelta", "E:\\DeltaJ-workspace\\PrintClassDelta", d);
>>>>>>> d299962c6699a7899a042184feb16b9d25c6636b
>>>>>>> dcd1a3b06a7c10071d7c4811488003e91ce11a3b
		//Do something with the AST.
		//For information on the AST structure consult:
		//- metamodel in org.emftext.language.java/metamodel/java.ecore
		//- concrete syntax in org.emftext.language.java/metamodel/java.cs
<<<<<<< HEAD
//		if (abstractSyntaxTreeRoot instanceof CompilationUnit) {
//			CompilationUnit compilationUnit = (CompilationUnit) abstractSyntaxTreeRoot;
//
//			List<ConcreteClassifier> classifiers = compilationUnit.getClassifiers();
//			
//			for (ConcreteClassifier classifier : classifiers) {
//				String classifierName = classifier.getName();
//				
//				List<Member> members = classifier.getMembers();
//				List<String> comments = classifier.getComments();
//
//				if (members != null && members.size() > 2 && !members.get(2).getComments().isEmpty()) {
//					System.out.println(members.get(2).getComments().get(0));
//				}
//				
//				// print class name
//				System.out.println("Classifier: " + classifierName);
//				
//				System.out.println("base code:");
//				printMembers(members);
//				printString(comments);

//				diffPre.resetPrepDiff();
				gcl.executeDiff("E:\\programmaticallyCreatedGitRepo", 7, 0, "Printer.java");
				diffPre.setInput(gcl.getDiff());
				diffPre.separateChanges();
				
				ChangesValidator cVal = new ChangesValidator();
=======
		if (abstractSyntaxTreeRoot instanceof CompilationUnit) {
			CompilationUnit compilationUnit = (CompilationUnit) abstractSyntaxTreeRoot;

			List<ConcreteClassifier> classifiers = compilationUnit.getClassifiers();
			
			for (ConcreteClassifier classifier : classifiers) {
				String classifierName = classifier.getName();
				
				List<Member> members = classifier.getMembers();
				List<String> comments = classifier.getComments();

<<<<<<< HEAD
//				if (members != null && members.size() > 2 && !members.get(2).getComments().isEmpty()) {
//					System.out.println(members.get(2).getComments().get(0));
//				}
=======
				if (members != null && members.size() > 2 && !members.get(2).getComments().isEmpty()) {
					System.out.println(members.get(2).getComments().get(0));
				}
>>>>>>> d299962c6699a7899a042184feb16b9d25c6636b
				
				// print class name
				System.out.println("Classifier: " + classifierName);
				
				System.out.println("base code:");
				printMembers(members);
				printString(comments);

//				diffPre.resetPrepDiff();
<<<<<<< HEAD
				gcl.executeDiff("E:\\programmaticallyCreatedGitRepo", 6, 3, "Printer.java");
=======
				gcl.executeDiff("E:\\programmaticallyCreatedGitRepo", 3, 0, "Printer.java");
>>>>>>> d299962c6699a7899a042184feb16b9d25c6636b
				diffPre.setInput(gcl.getDiff());
//				System.out.println("INPUT: " + diffPre.getInput());
				diffPre.separateChanges();
				
				ChangesValidator cVal = new ChangesValidator();
<<<<<<< HEAD
=======
				// TODO For testing only
				cVal.setMembers(members);
>>>>>>> d299962c6699a7899a042184feb16b9d25c6636b
>>>>>>> dcd1a3b06a7c10071d7c4811488003e91ce11a3b
				PreprocessedDiff prepDiff = diffPre.getPrepDiff();
				prepDiff.setToLast();
				int noPrev = prepDiff.size(),
					j = 1;
				while (prepDiff.hasPrevious() || noPrev > 0) {
					Changes changes = prepDiff.getChanges();
					Delta tempDelta = djc.createNewDelta(dju, "Delta" + j++);
					if (changes.size() > 0) {
						changes.setToFirst();
<<<<<<< HEAD
						// TODO why are changes not returned correctly??????
						//TODO
						//TODO
						//TODO
						for (int k = 0; k < changes.size(); k++) {
							Change c =  changes.getChange(k);
							String packageName,
								   className;
							packageName = c.getPackageName();
							className = c.getClassName();
							djc.addJavaUnit(tempDelta, cVal.validateChange(c), packageName, className, 
									c.getAddRem(), c.getTypeOfChange());
							if (k == changes.size()-2) {
								
							}
							djc.addToDeltaString(tempDelta, c);

							djc.closeDeltaString();
						}

						djc.write("PrintClassCoreDelta", "E:\\DeltaJ-workspace\\PrintClassDelta");
=======
						// TODO why changes are not returned correctly??????
						//TODO
						//TODO
						//TODO
						for (Change c : changes) {
<<<<<<< HEAD
//							System.out.println("Package Name: " + c.getPackageName() + "\tClass Name: " + c.getClassName());
//							System.out.println("AST within JaMoPP loop: " + JavaResourceUtil.getText(cVal.validateChange(c)));
							String packageName,
								   className;
//							System.out.println(c.getClassName());
							packageName = c.getPackageName();
							className = c.getClassName();
//							System.out.println("Changes within loop of JaMoPPInjection: " + c.getChanges());
							djc.addJavaUnit(tempDelta, cVal.validateChange(c), packageName, className, 
									c.getAddRem());
							
							djc.createDeltaFile("PrintClassCoreDelta", "E:\\DeltaJ-workspace\\PrintClassDelta", tempDelta, c);
=======
							String packageName,
							className;
							System.out.println(c.getClassName());
							packageName = c.getClassName().substring(0, c.getClassName().lastIndexOf("."));
							className = c.getClassName().substring(c.getClassName().lastIndexOf(".")+1, 
									c.getClassName().length());
							System.out.println("Changes within loop of JaMoPPInjection: " + c.getChanges());
							djc.addJavaUnit(tempDelta, cVal.validateChange(c), packageName, className, 
									c.getAddRem());
>>>>>>> d299962c6699a7899a042184feb16b9d25c6636b
						}
//						Change change = changes.getChange();
//						String packageName,
//							   className;
//						System.out.println(change.getClassName());
//						packageName = change.getClassName().substring(0, change.getClassName().lastIndexOf(".")-1);
//						className = change.getClassName().substring(change.getClassName().lastIndexOf("."), 
//								change.getClassName().length());
//						System.out.println("Changes within loop of JaMoPPInjection: " + change.getChanges());
//						djc.addJavaUnit(tempDelta, cVal.validateChange(change), packageName, className, 
//								change.getAddRem());
<<<<<<< HEAD
=======
						djc.createDeltaFile("PrintClassCoreDelta", "E:\\DeltaJ-workspace\\PrintClassDelta", tempDelta);
>>>>>>> d299962c6699a7899a042184feb16b9d25c6636b
>>>>>>> dcd1a3b06a7c10071d7c4811488003e91ce11a3b
						prepDiff.previous();
						noPrev--;
					}
				}
<<<<<<< HEAD
=======
				
				System.out.println("\n");
				System.out.println("modified code:");
				printMembers(members);
				printString(comments);
			}			

<<<<<<< HEAD
//			System.out.println(JavaResourceUtil.getText(abstractSyntaxTreeRoot));
=======
			System.out.println(JavaResourceUtil.getText(abstractSyntaxTreeRoot));
>>>>>>> d299962c6699a7899a042184feb16b9d25c6636b
			
		}
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
>>>>>>> dcd1a3b06a7c10071d7c4811488003e91ce11a3b
	}
	
	// Your method
    public static Resource getResource(byte[] content, ResourceSet resourceSet, Map<?, ?> loadOptions) {

        org.emftext.language.java.resource.java.mopp.JavaMetaInformation metaInformation = 
        		new org.emftext.language.java.resource.java.mopp.JavaMetaInformation();

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
