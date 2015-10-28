package com.max.jamoppinjection;


import java.io.IOException;

import logger.Logger;

import org.deltaj.deltaJ.Delta;
import org.deltaj.deltaJ.DeltaJUnit;
import org.emftext.language.java.resource.java.util.JavaResourceUtil;

import preprocessing.diffpreprocessor.DiffPreprocessor;
import preprocessing.diffpreprocessor.ModificationType;
import preprocessing.diffs.ClassChanges;
import preprocessing.diffs.Commit;
import preprocessing.diffs.PreprocessedDiff;
import preprocessing.gitconnector.GitConnectorCmdL;
import deltatransformation.DeltaJCreator;

public class JaMoPPInjection {
	

	static private Logger log;
	
	public static void run() {
		//Get (valid) Java source code from somewhere.
		String code = "";	
		int i = 0;

		PropertiesReader configReader = new PropertiesReader("config.properties");
//		boolean withBaseline = true;
//		try {
//			withBaseline = Boolean.parseBoolean(configReader.getPropValue("IncludeBaseLine"));
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		log = new Logger("E:\\loglog.txt", true);
		// Connector to git to clone, extract code base and diffs.
		GitConnectorCmdL gcl;
		// cleans the code base and diffs and creates a memory table for knowing lines of methods and fields.
		DiffPreprocessor diffPre = new DiffPreprocessor();

		DeltaJCreator djc = new DeltaJCreator();
		
		DeltaJUnit dju = djc.createDeltaJUnit();

		log.writeToLog("JaMoPPInjection : Starting delta transformation...");
		// TODO adjust headRevision, and paths to your flavor and system.
		int headRevision = 8;
//		// Path to git.exe must end with the git.exe itself!
//		String gitExePath = "E:\\Program Files (x86)\\Git\\bin\\git.exe",
//			   targetDirectory = "E:\\programmaticallyCreatedGitRepo\\",
//			   repoUri = "https://github.com/mkuehl/BankAccountV1.git",
//			   deltaDirectory = "E:\\DeltaJ-workspace\\PrintClassDelta";
		
		String gitExePath = null,
			   targetDirectory = null,
			   repoUri = null,
			   deltaDirectory = null;


		

		log.writeToLog("JaMoPPInjection : Reading config file...");

		try {
			gitExePath = configReader.getPropValue("GitExePath");
			repoUri = configReader.getPropValue("GitRepoURL");
			targetDirectory = configReader.getPropValue("RepoPathOnSystem");
			deltaDirectory = configReader.getPropValue("DeltaDirectory");
			headRevision = Integer.parseInt(configReader.getPropValue("StepsBackFromHEADRevision"));
		} catch (NumberFormatException e) {
			e.printStackTrace();
			log.writeToLog("JaMoPPInjection : Error while reading config file...");
		} catch (IOException e) {
			e.printStackTrace();
			log.writeToLog("JaMoPPInjection : Error while reading config file...");
		}
		
		if (gitExePath == null || targetDirectory == null || repoUri == null ||  deltaDirectory == null) {
			log.writeToLog("JaMoPPInjection : Cancel");
			return;
		}
	
		log.writeToLog("JaMoPPInjection : " + log.getSuccessMessage() + ".");
		
		gcl = new GitConnectorCmdL(gitExePath, "", "");

		// create baseline to which later changes shall be applied.
		gcl.getRepo(targetDirectory, repoUri);
		gcl.extractBaseline(targetDirectory, "HEAD~"+ headRevision, "");
		diffPre.setInput(gcl.getCodeBase());
		diffPre.preprocessCodeBase();

		// add code from baseline classes to coredelta.
		Commit codeBases = diffPre.getPrepDiff().next();
		for (ClassChanges base : codeBases) {
			code += base.getChanges();

			Delta d = djc.createNewDelta(dju, "coredelta" + i++);
			djc.addJavaUnit(d, JavaResourceUtil.getResourceContent(code), "", "", (byte) 1, ModificationType.CLASSADDITION);
			code = "";

			djc.addToDeltaString(d, new ClassChanges(), "");
			djc.closeDeltaString();
		}

		// write baseline delta into delta file.
		djc.write("PrintClassCoreDelta", deltaDirectory);

		
		int diffNo = 0;
		
		for (int revisionSubtraction = headRevision; revisionSubtraction > 0; revisionSubtraction--) {
			// get diffs.
			gcl.executeDiff(targetDirectory, revisionSubtraction, revisionSubtraction-1, "");
			if (diffNo == 11) {
				System.out.println();
			}
			diffPre.setInput(gcl.getDiff());
			diffPre.separateChanges();

			ChangesValidator cVal = new ChangesValidator();
			PreprocessedDiff prepDiff = diffPre.getPrepDiff();
			prepDiff.setToFirst();
			int size = prepDiff.size();
					
			// iterate over changes found. Start at the last one. Create a delta for each commit.
			while (prepDiff.hasNext() || diffNo < size) {
				Commit changes = prepDiff.getChanges();
				Delta tempDelta = djc.createNewDelta(dju, "Delta" + ++diffNo);
				if (changes.size() > 0) {
					changes.setToFirst();
					for (int k = 0; k < changes.size(); k++) {
						ClassChanges c =  changes.getChange(k);
						if (c == null) {
							continue;
						}
						String packageName = "",
								className = "";

						if (c.getChanges() == null || c.getChanges().equals("")) {
							continue;
						}
						if (!c.getIsWholeClass() || c.getTypeOfChange().equals(ModificationType.CLASSREMOVAL)) {
							packageName = c.getPackageName();
							className = c.getClassName();
						}
						djc.addJavaUnit(tempDelta, cVal.validateChange(c), packageName, className, 
								c.getAddRem(), c.getTypeOfChange());
						if (k == changes.size()-2) {

						}
						if (c.getChanges() != null) {
							//						if (c.getChanges().contains("TestInterface1")) {
							//							System.out.println();
							//						}
							djc.addToDeltaString(tempDelta, c, "Hash: " + changes.getCommitHash() 
									+ "\nCommitMessage:" + changes.getCommitMessage());
						}

						// interfaces and superclasses have already a semicolon, if the deltastring has one as well, don't close it!
						if (c.getTypeOfChange().equals(ModificationType.CLASSADDITION)) {
							djc.closeDeltaString();
						}
					}
					djc.write("PrintClassCoreDelta", deltaDirectory);
				} 
				prepDiff.next();
			}
		}
	}
}