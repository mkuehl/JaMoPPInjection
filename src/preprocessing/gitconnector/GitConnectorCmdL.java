package preprocessing.gitconnector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.util.Scanner;

public class GitConnectorCmdL {
	
	private StringBuilder diff;

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
		localDir.mkdir();
//		localPath.delete();

		System.setProperty("user.dir", pathToDir);
		System.out.println(System.getProperty("user.dir") + "\t" + pathToDir);
		Process p = null;
		String[] cloneCommand = {"E:\\Program Files (x86)\\Git\\bin\\git", "clone", uri, pathToDir};
		ProcessBuilder builder = new ProcessBuilder(cloneCommand);
////		builder.directory(new File(/ngs/app/abc));
		new File("E:\\clonelog.txt").delete();
		File f = new File("E:\\clonelog.txt");
		builder.redirectOutput(Redirect.appendTo(f));

		try {
			p = builder.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
			
		@SuppressWarnings("resource")
		Scanner s = new Scanner(p.getErrorStream());
		while(s.hasNext()) {
			System.out.println(s.nextLine());
		}
	}
	
	/**
	 * Creates the diffs for the given numbers. The numbers specify how many commits to step back.
	 * Therefore the start has to be bigger than the end.
	 * @param pathToDir
	 * @param startNumberOfCommitFromHEAD - higher number, farther away from HEAD
	 * @param endNumberOfCommitFromHEAD - lower number, nearer to HEAD
	 */
	public void executeDiff(String pathToDir, int startNumberOfCommitFromHEAD, int endNumberOfCommitFromHEAD) {
		Process p = null;
//		String[] command = {"cmd", "/c", "dir", "/a:-d", pathToDir};
		String range = "HEAD~" + startNumberOfCommitFromHEAD + "..HEAD~" + endNumberOfCommitFromHEAD;
		String[] logCommand = {"E:\\Program Files (x86)\\Git\\bin\\git.exe", "log", "-p", "--pretty=email", range, "PrintClass.java"};
		for (String l : logCommand) {
			System.out.println(l);
		}
		ProcessBuilder pb = new ProcessBuilder(logCommand);
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
			pb.directory(new File(pathToDir));
			System.out.println(pb.directory().getPath());
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
		}
	}
	
	public String getDiff() {
		return diff.toString();
	}
}
