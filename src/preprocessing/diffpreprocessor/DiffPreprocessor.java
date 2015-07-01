package preprocessing.diffpreprocessor;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import diffs.Change;
import diffs.Changes;
import diffs.PreprocessedDiff;

public class DiffPreprocessor {

	private String input;
	private PreprocessedDiff prepDiff;
	private Changes changes;
	
	public DiffPreprocessor() {
		prepDiff = new PreprocessedDiff();
		changes = new Changes();
	}
	
	public boolean readFile(String pathToFile) {
		try {
			input = new String(Files.readAllBytes(Paths.get(pathToFile)), StandardCharsets.UTF_16);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void setInput(String p_input) {
		input = p_input;
	}
	
	public String getInput() {
		return input;
	}
	
	public PreprocessedDiff getPrepDiff() {
		return prepDiff;
	}
	
	/**
	 * Cleans the input from all not necessary meta information. Thus, just information about 
	 * starting line and number of (affected) lines is retained 
	 */
	public void cleanInput() {
		if (input != null && !input.isEmpty()) {
			// first line says, that lines start with one expression within the parenthesis followed by an optional : and a mandatory whitespace
			String regex = "((From|Date):?\\s" //removed Subject from inside parentheses
					// matches the following example: 8071651f2235b87551e757ba8f53d74509be5d3f Mon Sep 17 00:00:00 2001 (hash, day, month, date, time, year)
					+ "(([0-9a-z]{40}\\s(Mon|Tue|Wed|Thu|Fri|Sat|Sun){1}\\s(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec){1} [1-9]{2}\\s([0-9]{2}:?){3}\\s[0-9]{4})|"
					// user name and email of committer
					+ "([\\w|.|_]+\\s<[\\w|.|_]+@[\\w|\\d|.|_]+[^.]+>)|"
					// Date
					+ "((Mon|Tue|Wed|Thu|Fri|Sat|Sun){1},\\s[\\d]{1,2}\\s(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec){1}\\s[0-9]{4}\\s([0-9]{2}:?){3}\\s\\+[0-9]{4})|"
					// commit message. DOES NOT WORK YET!!!!!!!!!!!!!!!! (and probably needn't to) 
//					+ "(([\\w]+)$)))|" 
					// only because previous line is commented out
					+ "))|"
					// diff --git a/class.extension b/class.extension
					+ "((diff\\s--git\\sa\\/[^.]+\\.[\\w]{1,4}\\sb\\/[^.]+\\.[\\w]{1,4})|"
					// index 7hash..7hash (optional:) 6digits
					+ "(index\\s[0-9a-f]{7}[\\.]{2}[0-9a-f]{7})(\\s[\\d]{6})?|"
					//---/+++ followed by [a|b]/class.ext or /dev/null
					+ "(([\\-]{3})|([\\+]{3}))\\s(([a|b]\\/[^.]+\\.[\\w]{1,4})|(\\/(dev)\\/(null)))|"
					// keyword file mode 6digits
					+ "((new|modified|deleted)\\s(file)\\s(mode)\\s[\\d]{6}))";
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(input);
			System.out.println("Input: " + input);
			input = m.replaceAll("");
		}
	}
	
	/**
	 * Separates the changes. Currently adding just Change objects to one Changes object. Normally should add as much Changes objects 
	 * as class files are affected.
	 */
	public void separateChanges() {
		LinkedList<String> s = new LinkedList<String>();
		// absolute beginning line for code block
		int actualLine = -1,
			// for getting correct change lines, increments in each iteration. 
			beginningLine = -1;
		byte addRem = -128;
		//
		boolean skipFirst = false;
		StringBuilder modifications = new StringBuilder();
		// for iterating over each line
		String[] lines;
		// matches line information for shown changes within actual code block
		String lineInfoRegex = "\\-[\\d]{1,4},[\\d]{1,2}\\s\\+[\\d]{1,4},[\\d]{1,2}";
		Pattern lineInfoPattern = Pattern.compile(lineInfoRegex);
		Matcher lineInfoMatcher;
		String[] sa = input.split("(@@|(Subject: ))");
		for (String t: sa) {
			if (t.isEmpty() || t.matches("[\\s]*")) {
				continue;
			}
			// if t is a code block, it starts with the class and a { or for the first addition with a newline. This has to be removed.
			if (t.startsWith("\n")) {
				t = t.substring(1);
			} else if (t.startsWith("\r\n")) {
				t = t.substring(2);
			}
			lineInfoMatcher = lineInfoPattern.matcher(t);
			// if line information is given, beginning line is set to the beginning of the listing.
			if (lineInfoMatcher.find()) {
				actualLine = Integer.parseInt(t.substring(t.indexOf("+"), t.lastIndexOf(",")));
			}
			// if no line info found, check if t starts with "[PATCH]", if not, iterate over lines.
			else if (!t.startsWith("[PATCH]")) {
				/*
				 *  if actualLine is bigger than 2 (if git log shows 3 surrounding lines (standard)) and 
				 *  begins with a modifier and class, then the first line must be skipped.
				 *  Space at the beginning of each startsWith parameter, because git log outputs one.
				 */
				if (actualLine > 2 && (t.startsWith(" public class") || t.startsWith(" protected class") 
						|| t.startsWith(" class") || t.startsWith(" private class"))) {
					skipFirst = true;
				}
				lines = t.split("\\r?\\n");
				for (String line : lines) {
					if (skipFirst) {
						skipFirst = false;
						continue;
					}
					// addition, excluding a multiline string with leading + for second and later fragments
					if (line.startsWith("+") && !line.startsWith("\"", 1)) {
						// necessary for additions directly succeeding removals
						if (addRem < 1 && addRem != -128) {
							addChangeToChangesInIteration(addRem, beginningLine, modifications.toString());
							modifications.delete(0, modifications.length());
							addRem = -128;
						}
						if (addRem == -128) {
							addRem = 1;
							beginningLine = actualLine;
						}
						modifications.append(line.substring(1) + (line.endsWith("\n") ? "" : "\n"));
					}
					// removal
					else if (line.startsWith("-")) {
						// necessary for removals directly succeeding additions
						if (addRem > -1) {
							addChangeToChangesInIteration(addRem, beginningLine, modifications.toString());
							modifications.delete(0, modifications.length());
							addRem = -128;
						}
						if (addRem == -128) {
							addRem = -1;
							beginningLine = actualLine;
							skipFirst = true;
						}
						modifications.append(line.substring(1) + "\n");
					} else {
						if (addRem != -128) {
							addChangeToChangesInIteration(addRem, beginningLine, modifications.toString());
							modifications.delete(0, modifications.length());
							addRem = -128;
						}
					}
					if (addRem < 1 && addRem != -128) {
						actualLine = skipFirst ? actualLine : actualLine-1;
						skipFirst = false;
					} else {
						actualLine++;
					}
				}
				s.add(t);
			}
		}
		prepDiff.add(changes);
	}
	
	private void addChangeToChangesInIteration(byte addRem, int beginningLine, String modifications) {	
		changes.add(new Change(addRem, beginningLine, modifications));
	}
	
	/**
	 * Delets a dir recursively deleting anything inside it.
	 * @param dir The dir to delete
	 * @return true if the dir was successfully deleted
	 */
	public static boolean deleteDirectory(File dir) {
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
	
//	public static void main(String[] args) {
//		DiffPreprocessor diffPre = new DiffPreprocessor();
//		// delete directory first, if existent, otherwise no new dir can be created
////		deleteDirectory(new File("E:\\programmaticallyCreatedGitRepo"));
////		GitConnectorJGit gitcon = new GitConnectorJGit("peter12345678", "peTER12");
////		gitcon.getRepo("E:\\programmaticallyCreatedGitRepo", "https://github.com/mkuehl/TestRepo.git");
////		gitcon.executeDiff(21, 16);
////		StringBuilder sb = new StringBuilder("");
////		while (gitcon.hasNext()) {
////			sb.append(gitcon.nextDiff());
////		}
//		GitConnectorCmdL gcl = new GitConnectorCmdL("", "");
////		gcl.getRepo("E:\\programmaticallyCreatedGitRepo", "https://github.com/mkuehl/TestRepo.git");
//		gcl.executeDiff("E:\\programmaticallyCreatedGitRepo", 20, 2);
////		//TODO adjust path to your system
////		diffPre.readFile("C:\\Users\\Max\\Documents\\GitHub\\diffdiff.txt");
////		try {
////			PrintWriter out = new PrintWriter("E:\\sb.txt");
////			out.print(sb.toString());
////			out.close();
////		} catch (FileNotFoundException e) {
////			e.printStackTrace();
////		}
//		diffPre.setInput(gcl.getDiff()/*sb.toString()*/);
//		System.out.println();
//		diffPre.clearInput();
//		diffPre.separateChanges();
//		try {
//			PrintWriter out = new PrintWriter("E:\\sb2.txt");
//			out.print(diffPre.getInput());
//			out.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//
//		int i = 1;
//		Changes changes = diffPre.prepDiff.next();
//		System.out.println("addRem > 0 -> addition\n"
//				+ "addRem < 0 -> removal\n"
//				+ "addRem = 0 -> reserved for modifications");
//		for (Change change : changes) {
//			System.out.print("Change " + i++ + "\n\taddRem: " + change.getAddRem() 
//					+ "\n\tbeginningLine: " + change.getBeginningLine() 
//					+ "\n\tchanges:\n" + change.getChanges());
//		}
//
//	}
}
