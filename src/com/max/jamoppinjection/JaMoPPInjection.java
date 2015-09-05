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
		String code = "";	
		int i = 0;

		// Connector to git to clone, extract code base and diffs.
		GitConnectorCmdL gcl = new GitConnectorCmdL("", "");
		// cleans the code base and diffs and creates a memory table for knowing lines of methods and fields.
		DiffPreprocessor diffPre = new DiffPreprocessor();
		
		// create code base to which later changes shall be applied
		// TODO adjust name and path to your flavor and system
		gcl.getRepo("E:\\programmaticallyCreatedGitRepo\\", "https://github.com/mkuehl/TestRepo.git");
		gcl.extractCodeBase("E:\\programmaticallyCreatedGitRepo\\", "HEAD~11", /*"Printer.java"*/"");
		diffPre.setInput(gcl.getCodeBase());
		diffPre.preprocessCodeBase();
		
		DeltaJCreator djc = new DeltaJCreator();
		
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
	
		//Do something with the AST.
		//For information on the AST structure consult:
		//- metamodel in org.emftext.language.java/metamodel/java.ecore
		//- concrete syntax in org.emftext.language.java/metamodel/java.cs
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
				gcl.executeDiff("E:\\programmaticallyCreatedGitRepo", 11, 0, "");
				diffPre.setInput(gcl.getDiff());
				diffPre.separateChanges();
				
				ChangesValidator cVal = new ChangesValidator();
				PreprocessedDiff prepDiff = diffPre.getPrepDiff();
				prepDiff.setToLast();
				int noPrev = prepDiff.size(),
					j = 1;
				while (prepDiff.hasPrevious() || noPrev > 0) {
					Changes changes = prepDiff.getChanges();
					Delta tempDelta = djc.createNewDelta(dju, "Delta" + j++);
					if (changes.size() > 0) {
						changes.setToFirst();
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

							if (!(c.getChanges().contains("interfaces") || c.getChanges().contains("superclass"))) {
								djc.closeDeltaString();
							}
						}

						djc.write("PrintClassCoreDelta", "E:\\DeltaJ-workspace\\PrintClassDelta");
					} 
					prepDiff.previous();
					noPrev--;
				}
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
