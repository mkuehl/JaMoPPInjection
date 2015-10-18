package com.max.jamoppinjection;


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
	
	public static void run() {
		//Get (valid) Java source code from somewhere.
		String code = "";	
		int i = 0;
		// TODO adjust headRevision, and paths to your flavor and system.
		int headRevision = 19;
		// Path to git.exe must end with the git.exe itself!
		String gitExePath = "E:\\Program Files (x86)\\Git\\bin\\git.exe",
			   targetDirectory = "E:\\programmaticallyCreatedGitRepo\\",
			   repoUri = "https://github.com/mkuehl/TestRepo.git",
			   deltaDirectory = "E:\\DeltaJ-workspace\\PrintClassDelta";

		// Connector to git to clone, extract code base and diffs.
		GitConnectorCmdL gcl = new GitConnectorCmdL(gitExePath, "", "");
		// cleans the code base and diffs and creates a memory table for knowing lines of methods and fields.
		DiffPreprocessor diffPre = new DiffPreprocessor();
		
		// create baseline to which later changes shall be applied.
		gcl.getRepo(targetDirectory, repoUri);
		gcl.extractBaseline(targetDirectory, "HEAD~"+ headRevision, "");
		diffPre.setInput(gcl.getCodeBase());
		diffPre.preprocessCodeBase();
		
		DeltaJCreator djc = new DeltaJCreator();
		
		DeltaJUnit dju = djc.createDeltaJUnit();

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

		// get diffs.
		gcl.executeDiff(targetDirectory, headRevision, 0, "");
		diffPre.setInput(gcl.getDiff());
		diffPre.separateChanges();

		ChangesValidator cVal = new ChangesValidator();
		PreprocessedDiff prepDiff = diffPre.getPrepDiff();
		prepDiff.setToFirst();
		int size = prepDiff.size(),
				diffNo = 0;
		// iterate over changes found. Start at the last one. Create a delta for each commit.
		while (prepDiff.hasNext() || diffNo < size) {
			Commit changes = prepDiff.getChanges();
			Delta tempDelta = djc.createNewDelta(dju, "Delta" + ++diffNo);
			if (changes.size() > 0) {
				changes.setToFirst();
				for (int k = 0; k < changes.size(); k++) {
					ClassChanges c =  changes.getChange(k);
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
					djc.addToDeltaString(tempDelta, c, "Hash: " + changes.getCommitHash() 
							+ "\nCommitMessage:" + changes.getCommitMessage());

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