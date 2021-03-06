package preprocessing.gitconnector;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Scanner;

import com.max.jamoppinjection.PropertiesReader;

import logger.Logger;

public class GitConnectorCmdL {
	
	private String gitExePath;
	private StringBuilder diff;
	private StringBuilder codeBase;
	private PropertiesReader configReader;
	private Logger log;

	/**
	 * The only mandatory parameter is pathToGitExe. It specifies the path where the git.exe
	 * is located in the system. The path must be specified with the exe itself, e.g. 
	 * "C:\\Program Files\\Git\\git.exe". This is important because some commands need the 
	 * file extension explicitly otherwise they don't work.
	 * 
	 * @param pathToGitExe
	 * @param usrnam
	 * @param pwd
	 */
	public GitConnectorCmdL(String pathToGitExe, String usrnam, String pwd) {
		gitExePath = pathToGitExe;
		configReader = new PropertiesReader("config.properties");
		try {
			log = new Logger(configReader.getPropValue("LogFilePath"), false);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/**
	 * Creates a directory under the given path to clone the given repository into.
	 * @param pathToDir - directory to be created as clone target
	 * @param uri - repository to be cloned
	 */
	public void getRepo(String pathToDir, String uri) {
		log.writeToLog(this.getClass().toString() + " : Creating locale git repository...");
		File localDir = null;
		localDir = new File(pathToDir);

		deleteDirectory(localDir);
		localDir.mkdir();

		System.setProperty("user.dir", pathToDir);
		Process p = null;
		String[] cloneCommand = {gitExePath, "clone", uri, pathToDir};
		ProcessBuilder builder = new ProcessBuilder(cloneCommand);
		new File("E:\\clonelog.txt").delete();
		File f = new File("E:\\clonelog.txt");
		builder.redirectOutput(Redirect.appendTo(f));

		try {
			p = builder.start();
			p.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to create locale git repository");
			log.writeToLog(this.getClass().toString() + " : " + log.getFailMessage() + ".");
			
			return;
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println("Failed to create locale git repository. Process got stuck.");
			log.writeToLog(this.getClass().toString() + " : " + log.getFailMessage() + ".");
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
		System.out.println("Successfully created locale git repository");
		log.writeToLog(this.getClass().toString() + " : " + log.getSuccessMessage() + ".");
	}
	
	/**
	 * Gets all commits from the first up to the specified. The initial commit is compared with the specified latest 
	 * commit.
	 * @param pathToDir
	 * @param latestCommitHash - either hash or HEAD~i, where i is the number of steps to take back from HEAD
	 * @param optionalClass - if just changes for a particular class are required. Otherwise use ""
	 */
	public void extractBaseline(String pathToDir, String latestCommitHash, String optionalClass) {
		log.writeToLog(this.getClass().toString() + " : Extracting baseline code...");
		Process p = null;
		File repoDirectory = new File(pathToDir);
		String initialCommitHash = "";
		String[] revlistCommand = {gitExePath, "rev-list", "--max-parents=0", "HEAD"};
		String[] diffCommand = {gitExePath, "diff", "", optionalClass};
		String[] logCommandForInitialCommit = {gitExePath, "log", "-p", "--pretty=email", 
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
				revlistCommand[2] = "--max-parents=1";

				pb = new ProcessBuilder(revlistCommand);
				pb.directory(repoDirectory);
				p = pb.start();
				s = new Scanner(p.getInputStream());
				latestCommitHash = s.next();
				initialCommitHash = s.next();
				// Set second position to initialCommitHash only to get initial commit.
				logCommandForInitialCommit[5] = initialCommitHash;
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
			System.out.println("Failed to extract baseline code.");
			log.writeToLog(this.getClass().toString() + " : " + log.getFailMessage() + ".");
			return;
		}
		
		System.out.println("Successfully extracted baseline code.");
		log.writeToLog(this.getClass().toString() + " : " + log.getSuccessMessage() + ".");
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
		System.out.println("LOG: ");
		log.writeToLog(this.getClass().toString() + " : Requesting commit history...");
		Process p = null;
		String range = "HEAD~" + startNumberOfCommitFromHEAD + "..HEAD~" + endNumberOfCommitFromHEAD;
		// set git programm location, command, options
		String[] logCommand = {gitExePath, "log", "-p", "-U10000", "--pretty=email", "--reverse", range, 
				optionalClass};

		ProcessBuilder pb = new ProcessBuilder(logCommand);
		
		try {
			// set the path of the local git repo on which the command shall be performed.
			pb.directory(new File(pathToDir));
			p = pb.start();
			@SuppressWarnings("resource")
			Scanner s = new Scanner(p.getInputStream());
			diff = new StringBuilder("");
//			boolean addPublic = false;
			while(s.hasNextLine()) {
				String t = s.nextLine();
//				if (t.trim().equals("public")) {
//					addPublic = true;
//				} else {
//					if (t.contains("class") && !t.contains("public") && addPublic) {
//						t = "public " + t;
//						addPublic = false;
//					}
					diff.append(t + "\n");
//				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to get requested commit history");
			log.writeToLog(this.getClass().toString() + " : " + log.getFailMessage() + ".");
		}
		System.out.println("Successfully got requested commit history.");
		log.writeToLog(this.getClass().toString() + " : " + log.getSuccessMessage() + ".");
		
	}
	
	public String getDiff() {
		return diff.toString();
	}
	
	public String getCodeBase() {
		return codeBase.toString();
	}
	
	/**
	 * Delets a dir recursively deleting anything inside it.
	 * @param dir The dir to delete
	 * @return true if the dir was successfully deleted
	 */
	public boolean deleteDirectory(File dir) {
	    if(! dir.exists() || !dir.isDirectory())    {
	        return false;
	    }

	    String[] files = dir.list();
	    for(int i = 0, len = files.length; i < len; i++)    {
	        File f = new File(dir, files[i]);
	        if(f.isDirectory()) {
	            deleteDirectory(f);
	        }else   {
	            f.delete();
	        }
	    }
	    return dir.delete();
	}
}
