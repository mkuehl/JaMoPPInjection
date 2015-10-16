package preprocessing.gitconnector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.util.Scanner;

import preprocessing.diffpreprocessor.DiffPreprocessor;

public class GitConnectorCmdL {
	
	private StringBuilder diff;
	private StringBuilder codeBase;

	public GitConnectorCmdL(String usrnam, String pwd) {
	}

	/**
	 * Creates a directory under the given path to clone the given repository into.
	 * @param pathToDir - directory to be created as clone target
	 * @param uri - repository to be cloned
	 */
	public void getRepo(String pathToDir, String uri) {
		File localDir = null;
		//			localPath = File.createTempFile("TestGitRepositoryJGIT", "");
		localDir = new File(pathToDir);

		DiffPreprocessor.deleteDirectory(localDir);
//		localDir.delete();
		localDir.mkdir();

		System.setProperty("user.dir", pathToDir);
//		System.out.println(System.getProperty("user.dir") + "\t" + pathToDir);
		Process p = null;
		String[] cloneCommand = {"E:\\Program Files (x86)\\Git\\bin\\git", "clone", uri, pathToDir};
		ProcessBuilder builder = new ProcessBuilder(cloneCommand);
////		builder.directory(new File(/ngs/app/abc));
		new File("E:\\clonelog.txt").delete();
		File f = new File("E:\\clonelog.txt");
		builder.redirectOutput(Redirect.appendTo(f));

		try {
			p = builder.start();
			p.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to create locale git repository");
			return;
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println("Failed to create locale git repository. Process got stuck.");
			return;
		}
		/*
		 * Process must be killed somehow. If not it blocks the repo directory for succeeding processes 
		 * like in code base creation. Simply calling destroy leads to an empty directory und thus 
		 * everything fails.
		 */
		if (p.exitValue() != 0) {
			p.destroy();
		}
//		@SuppressWarnings("resource")
//		Scanner s = new Scanner(p.getErrorStream());
//		while(s.hasNext()) {
//			System.out.println(s.nextLine());
//		}
		System.out.println("Successfully created locale git repository");
	}
	
	/**
	 * Gets all commits from the first up to the specified. The initial commit is compared with the specified latest 
	 * commit.
	 * @param pathToDir
	 * @param latestCommitHash - either hash or HEAD~i, where i is the number of steps to take back from HEAD
	 * @param optionalClass - if just changes for a particular class are required. Otherwise use ""
	 */
	public void extractBaseline(String pathToDir, String latestCommitHash, String optionalClass) {
		Process p = null;
		File repoDirectory = new File(pathToDir);
		String initialCommitHash = "";
		String[] revlistCommand = {"E:\\Program Files (x86)\\Git\\bin\\git.exe", "rev-list", "--max-parents=0", "HEAD"};
		String[] diffCommand = {"E:\\Program Files (x86)\\Git\\bin\\git.exe", "diff", "", optionalClass};
		String[] logCommandForInitialCommit = {"E:\\Program Files (x86)\\Git\\bin\\git.exe", "log", "-p", "--pretty=email", 
				"--reverse", "", optionalClass};
		ProcessBuilder pb = new ProcessBuilder(revlistCommand);
		try {
			pb.directory(repoDirectory);
			p = pb.start();
			@SuppressWarnings("resource")
			Scanner s = new Scanner(p.getInputStream());
			initialCommitHash = s.next();
			
			// diff
			diffCommand[2] = initialCommitHash + ".." + latestCommitHash;
			pb = new ProcessBuilder(diffCommand);
			pb.directory(repoDirectory);
			p = pb.start();
			s = new Scanner(p.getInputStream());
			codeBase = new StringBuilder("");
			while (s.hasNextLine()) {
				codeBase.append(s.nextLine() + "\n");
			}
			if (codeBase.toString().equals("")) {
				// Set second position to initialCommitHash only to get initial commit.
				logCommandForInitialCommit[4] = initialCommitHash;
				pb = new ProcessBuilder(logCommandForInitialCommit);
				pb.directory(repoDirectory);
				p = pb.start();
				s = new Scanner(p.getInputStream());
				codeBase = new StringBuilder("");
				while (s.hasNextLine()) {
					codeBase.append(s.nextLine() + "\n");
				}
				
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.out.println("Failed to extract code base.");
			return;
		}
		
		System.out.println("Successfully extracted code base");
	}
	
	/**
	 * Creates the diffs for the given numbers. The numbers specify how many commits to step back.
	 * Therefore the start has to be bigger than the end.
	 * @param pathToDir
	 * @param startNumberOfCommitFromHEAD - higher number, farther away from HEAD
	 * @param endNumberOfCommitFromHEAD - lower number, nearer to HEAD
	 * @param optionalClass - if just changes for a particular class are required. Otherwise use ""
	 */
	public void executeDiff(String pathToDir, int startNumberOfCommitFromHEAD, int endNumberOfCommitFromHEAD, 
			String optionalClass) {
		Process p = null;
//		String[] command = {"cmd", "/c", "dir", "/a:-d", pathToDir};
		String range = "HEAD~" + startNumberOfCommitFromHEAD + "..HEAD~" + endNumberOfCommitFromHEAD;
		// set git programm location, command, options
		String[] logCommand = {"E:\\Program Files (x86)\\Git\\bin\\git.exe", "log", "-p", "-U10000", "--pretty=email", "--reverse", range, 
				optionalClass};
//		for (String l : logCommand) {
//			System.out.println(l);
//		}
		ProcessBuilder pb = new ProcessBuilder(logCommand);
		// create a log file for the log command
		new File("E:\\loglog.txt").delete();
		File f = new File("E:\\loglog.txt");
		
		try {
//			pb.redirectOutput(Redirect.appendTo(f));
//			p2 = pb.start();
//			Scanner s2 = new Scanner(p2.getInputStream());
//			while(s2.hasNext()) {
//				System.out.println(s2.nextLine());
//			}
//			pb = new ProcessBuilder(logCommand);
//			pb.redirectOutput(Redirect.appendTo(f));
			// set the path of the local git repo on which the command shall be performed.
			pb.directory(new File(pathToDir));
//			System.out.println(pb.directory().getPath());
			p = pb.start();
			@SuppressWarnings("resource")
			Scanner s = new Scanner(p.getInputStream());
			diff = new StringBuilder("");
			while(s.hasNextLine()) {
				diff.append(s.nextLine() + "\n");
			}
			try {
				PrintWriter out = new PrintWriter(f);
				out.print(diff);
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
//			s2 = new Scanner(p.getInputStream());
//			while(s2.hasNext()) {
//				System.out.println(s2.nextLine());
//			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to get requested commit history");
		}
		System.out.println("Successfully got requested commit history");
	}
	
	public String getDiff() {
		return diff.toString();
	}
	
	public String getCodeBase() {
		return codeBase.toString();
	}
}
