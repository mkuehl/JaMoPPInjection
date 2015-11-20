Setup of eclipse and workspace

Attention: Tested on Windows 7 64-bit Professional and Windows 10 64-bit Professional with Java jdk version 1.8.0_65.

1. Download and install Java jdk (http://www.oracle.com/technetwork/java/javase/downloads/index.html).
2. Download and unpack Eclipse Kepler or newer (https://www.eclipse.org/downloads/) (tested with Luna and Mars, but not Kepler, each 64-bit).
3. Download and install git (for Windows: https://git-scm.com/download/win) .
4. Run eclipse (best as administrator).
5. Install JaMoPP in marketplace (see Installing JaMoPP).
6. Install DeltaJ in marketplace (see Installing DeltaJ).
7. Change eclipse-workspace to JaMoPPInjection (see Change Eclipse-workspace).
8. Change config.properties file to your system flavour (see Change config.properties).
9. Start delta transformation (see Delta Transformation).

Installing JaMoPP

1. Within opened Eclipse click on "Help"->"Install New Software...".
2. In textfield "Work with:" copy and paste the following (without "): "JaMoPP - http://jamopp.org/update".
3. In the list of items choose all from the following categories: 
	i. EMFText
	ii. EMFText Deprecated Features
	iii. EMFText Languages
	iv. JaMoPP (Java Model Parser and Printer)
	v. JaMoPP Applications
	vi. Language Components
	(it might be enough to install i. and iv., but I installed all of them)
4. Click "Next" 2 times.
5. Accept License Agreement and click "Finish".
6. After installation JaMoPP should be ready for usage.


Installing DeltaJ

1. Within opened Eclipse click on "Help"->"Install New Software...".
2. In textfield "Work with:" copy and paste the following (without "): DeltaJ - https://www.isf.cs.tu-bs.de/cms/research/deltas/downloads/plug-in/
3. In the list of items choose the only existing item "DeltaJ 1.5 with Full Java 1.5".
4. Click "Next" 2 times.
5. Accept License Agreement (if necessary) and click "Finish".
6. After installation DeltaJ should be ready for usage.


Change Eclipse-workspace

1. Within opened Eclipse click on "File"->"Switch Workspace"->"Other..." and choose the workspace of JaMoPPInjection project.
2. Ready


Change config.properties

1. In the JaMoPPInjection folder, open "config.properties" file (e.g. with Notepad++).
2. Change paths to your system. The following keywords where used:
	i. GitExePath: Path to git exe including the exe (for Windows). For Mac and Linux the respective git paths have to be used, it's not sure that it works on those systems.
	ii. GitRepoURL: The git project you want to process (this is for cloning).
	iii. RepoPathOnSystem: Path where git repo should be cloned to (e.g. C:\\RepoToBeTransformed\\).
	iv. DeltaDirectory: Directory for delta- and SPL-file (e.g. C:\\DeltaDirectory).
	v. DeltaFile: Name of delta-file with file type (e.g. example.deltaj).
	vi. SPLFile: Name of SPL-file with file type (e.g. example.spl).
	vii. StepsBackFromHEADRevision: The number of steps taken back from HEAD revision (e.g. 5).
	ix. LogFilePath: Path to log-file (e.g. C:\\deltatransformationlog.txt, or without ending for Linux/Mac).
	
Start Delta Transformation

1. Click on "Run As.."->"Eclipse Application". "Run As" can be found in the context menu by right-clicking the project name in the package explorer.
2. A new Eclipse instance starts.
3. In the task bar of the new Eclipse, click on "Delta creation"->"Create deltas".
4. Process should start and finish, output might be seen in source Eclipse instance.

