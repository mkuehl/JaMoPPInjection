package preprocessing.diffpreprocessor;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import deltatransformation.ModifiesTypeExaminer;
import preprocessing.diffs.Change;
import preprocessing.diffs.Changes;
import preprocessing.diffs.PreprocessedDiff;

public class DiffPreprocessor {

	private String input;
	private PreprocessedDiff prepDiff;
	private LinkedList<Changes> changesList;
	
	public DiffPreprocessor() {
		prepDiff = new PreprocessedDiff();
		changesList = new LinkedList<Changes>();
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
		prepDiff.clear();
		changesList.clear();
	}
	
	public String getInput() {
		return input;
	}
	
	public PreprocessedDiff getPrepDiff() {
		return prepDiff;
	}
	
	public void preprocessCodeBase() {
		input = Cleaner.cleanInput(input);
		int actualLine = -1,
			// for getting correct change lines, increments in each iteration. 
			beginningLine = -1;
		// for iterating over each line
		byte addRem = -128;
		String qualifiedClassName = null;
		StringBuilder modifications = new StringBuilder();
		Changes changes = null;
		String[] lines;
		String lineInfoRegex = "\\-[\\d]{1,4},[\\d]{1,2}\\s\\+[\\d]{1,4},[\\d]{1,2}";
		Pattern lineInfoPattern = Pattern.compile(lineInfoRegex);
		Matcher lineInfoMatcher;
		String[] sa = input.split("@@");
		for (String t: sa) {
//			System.out.println(t);
			if (t.isEmpty() || t.matches("[\\s]*")) {
				continue;
			}
//			if (actualLine > 2 && (t.startsWith(" public class") || t.startsWith(" protected class") 
//					|| t.startsWith(" class") || t.startsWith(" private class"))) {
//				skipFirst = true;
//			}
			lineInfoMatcher = lineInfoPattern.matcher(t);
			// if line information is given, beginning line is set to the beginning of the listing.
			if (lineInfoMatcher.find()) {
				actualLine = Integer.parseInt(t.substring(t.indexOf("+"), t.lastIndexOf(",")));
			}
			// if no line info found, code base is reached.
			else {

				if (!t.contains("package")) {
					continue;
				}
				lines = t.split("\\r?\\n");
				for (String line : lines) {
					if (line.contains("package")) {
						qualifiedClassName = line.substring(line.lastIndexOf("package") + 8, line.indexOf(";"));
					} else if (line.contains("class")) {
						qualifiedClassName += "." + line.substring(line.lastIndexOf("class")+6, line.indexOf(" {"));
					}
<<<<<<< HEAD
					if (line.equals("\\ No newline at end of file") || line.equals(lines[lines.length-1])) {
						break;
					}
=======
>>>>>>> dcd1a3b06a7c10071d7c4811488003e91ce11a3b
					// addition
					if (line.startsWith("+")) {
						// necessary for additions directly succeeding removals
						if (changes == null) {
							changes = new Changes();
						}
						if (addRem == -128) {
							addRem = 1;
							beginningLine = actualLine;
						}
						modifications.append(line.substring(1) + (line.endsWith("\n") ? "" : "\n"));
					}
				}
<<<<<<< HEAD
				changes.add(new Change(qualifiedClassName, beginningLine, addRem, "a", 
						modifications.toString()));
				modifications.delete(0, modifications.length());
=======
				changes.add(new Change(qualifiedClassName, addRem, beginningLine, modifications.toString()));
>>>>>>> dcd1a3b06a7c10071d7c4811488003e91ce11a3b
				changesList.add(changes);
			}
		}
		prepDiff.setModificationList(changesList);
	}
	
	/**
	 * Separates the changes. Currently adding just Change objects to one Changes object. Normally should add as much Changes objects 
	 * as class files are affected.
	 */
	public void separateChanges() {
		input = Cleaner.cleanInput(input);
		LinkedList<String> s = new LinkedList<String>();
		// absolute beginning line for code block
		// MOMENTARILY NOT WORKING CORRECTLY
		int actualLine = -1,
			// for getting correct change lines, increments in each iteration. 
			beginningLine = -1;
		byte addRem = -128;
//		ModifiesTypeExaminer mte = new ModifiesTypeExaminer();
		//
		boolean skipFirst = false;
		/*
		 *  Can have three possible states: a (added), r (removed) and u (unmodified).
		 *  Identifies if a modification is completed, necessary for modifications
		 *  without separating non-modified lines.
		 */
		char lineBeforeWasAdded = 'u';
		StringBuilder modifications = new StringBuilder();
		Changes changes = null;
		String qualifiedClassName = null;
		// for iterating over each line
		String[] lines;
		String regexCommitExtract = "[0-9a-z]{40}";
		Pattern pCommitExtract = Pattern.compile(regexCommitExtract);
		Matcher mCommitExtract;
		// matches line information for shown changes within actual code block
		String lineInfoRegex = "\\-[\\d]{1,4},[\\d]{1,2}\\s\\+[\\d]{1,4},[\\d]{1,2}";
		Pattern lineInfoPattern = Pattern.compile(lineInfoRegex);
		Matcher lineInfoMatcher;
		String[] sa = input.split("((From)|(@@)|(Subject: ))");
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
			mCommitExtract = pCommitExtract.matcher(t);
			// if commit hash is found, save it and continue.
			if (mCommitExtract.find()) {
				// the actual changes have to be added to the superior changesList here
<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> dcd1a3b06a7c10071d7c4811488003e91ce11a3b
//				if (changes != null && changes.isEverythingSet()) {
//					changesList.add(changes);
//					changes = null;
//				}
<<<<<<< HEAD
=======
=======
				if (changes != null && changes.isEverythingSet()) {
					changesList.add(changes);
					changes = null;
				}
>>>>>>> d299962c6699a7899a042184feb16b9d25c6636b
>>>>>>> dcd1a3b06a7c10071d7c4811488003e91ce11a3b
				changes = new Changes();
				changes.setCommitHash(mCommitExtract.group());
				continue;
			}
			lineInfoMatcher = lineInfoPattern.matcher(t);
			// if line information is given, beginning line is set to the beginning of the listing.
			if (lineInfoMatcher.find()) {
				actualLine = Integer.parseInt(t.substring(t.indexOf("+"), t.lastIndexOf(",")));
				// important for not adding a Changes object with line and commit info only
				continue;
			}
			// [PATCH] marks the beginning of the commit message line.
			else if(t.startsWith("[PATCH]")) {
				changes.setCommitMessage(t.substring(7, t.indexOf("\n")));
				// continue is important for not adding a Changes object with the commit message only
				continue;
			}
			// if no line info found, check if t starts with "[PATCH]", if not, iterate over lines.
			else if (!t.startsWith("[PATCH]")) {
<<<<<<< HEAD
				Change change = new Change();
				
				if (ClassAdditionExaminer.isWholeClassAdded(t)) {
					change.setAddRem((byte) 1);
					change.setTypeOfChange("a");
				} else if (ClassAdditionExaminer.isWholeClassRemoved(t)) {
					change.setAddRem((byte) -1);
					change.setTypeOfChange("r");
				}
=======
>>>>>>> dcd1a3b06a7c10071d7c4811488003e91ce11a3b
				/* 
				 * if t contains no package name, the changes can not be assigned to a specific class since 
				 * in DeltaJ class names have to be qualified.
				 */
				if (!t.contains("package")) {
					continue;
				}
				/*
				 *  if actualLine is bigger than 2 (if git log shows 3 surrounding lines (standard)) and 
				 *  begins with a modifier and class, then the first line must be skipped.
				 *  Space at the beginning of each startsWith parameter, because git log outputs one.
				 */
				if (actualLine > 2 && (t.startsWith(" public class") || t.startsWith(" protected class") 
						|| t.startsWith(" class") || t.startsWith(" private class"))) {
					skipFirst = true;
				}
<<<<<<< HEAD

				qualifiedClassName = extractQualifiedClassName(t);
				lines = t.split("\\r?\\n");
				for (String line : lines) {
=======
				Change change = new Change();

				lines = t.split("\\r?\\n");
				for (String line : lines) {
					if (line.contains("package")) {
						qualifiedClassName = line.substring(line.lastIndexOf("package") + 8, line.indexOf(";"));
					} else if (line.contains("class")) {
						qualifiedClassName += "." + line.substring(line.lastIndexOf("class")+6, line.indexOf(" {"));
					}
>>>>>>> dcd1a3b06a7c10071d7c4811488003e91ce11a3b
					/*
					 * Necessary because a lot of lines have to be obtained to get the fully qualified name
					 * of the respective class. If ensures that all lines are skipped that have no modi-
					 * fications.
					 */
					if (!(line.startsWith("+") || line.startsWith("-") || line.equals(lines[lines.length-1]))) {
<<<<<<< HEAD

						lineBeforeWasAdded = 'u';
						/*
						 *  if there have been changes before, add, if new mods are of different kind,
						 *  e.g. first added, then removed.
						 */
						if (modifications != null && modifications.toString() != "" && 
								modifications.length() > 0) {
							// if before something was added and now is removed, add the additions first, same applies for removals succeeded by additions.
							if ((addRem == 1 && line.startsWith("-")) || (addRem == -1 && line.startsWith("+"))) {
								changes.add(createChange(addRem, beginningLine, qualifiedClassName, 
										modifications.toString()));
								modifications.delete(0, modifications.length());
								addRem = -128;
							}
						}
=======
>>>>>>> dcd1a3b06a7c10071d7c4811488003e91ce11a3b
						continue;
					}
					if (skipFirst) {
						skipFirst = false;
						qualifiedClassName = t.substring(t.lastIndexOf("class")+6, t.indexOf(" {"));
						continue;
					}
					// addition, excluding a multiline string with leading + for second and later fragments
					if (line.startsWith("+") && !line.startsWith("\"", 1)) {
<<<<<<< HEAD
						if (lineBeforeWasAdded == 'r') {
							if (changes == null) {
								changes = new Changes();
							}
							if (modifications != null && modifications.toString() != "" && 
									modifications.length() > 0) {
								changes.add(createChange(addRem, beginningLine, qualifiedClassName, 
										modifications.toString()));
								modifications.delete(0, modifications.length());
								addRem = -128;
							}
=======
						// necessary for additions directly succeeding removals
						if (addRem < 1 && addRem != -128) {
							if (changes == null) {
								changes = new Changes();
							}
							change.setAddRem(addRem);
							change.setBeginningLine(beginningLine);
<<<<<<< HEAD
							change.setQualifiedClassName(qualifiedClassName);
=======
							change.setClassFile(qualifiedClassName);
>>>>>>> d299962c6699a7899a042184feb16b9d25c6636b
							// if actual changes are null, take "" + mods
							change.setChanges((change.getChanges() == null ? "" : change.getChanges())
									+ modifications.toString());
							modifications.delete(0, modifications.length());
							addRem = -128;
>>>>>>> dcd1a3b06a7c10071d7c4811488003e91ce11a3b
						}
						lineBeforeWasAdded = 'a';
						if (addRem == -128) {
<<<<<<< HEAD
							// imports just can be added or removed, thus 0 is not allowed.
							if (line.contains("import")) {
								addRem = 1;
							} else if (line.matches("(\\+[\\s]*(public|protected|private)?\\s(\\w|\\d|_|\\.){0,50}(\\w|\\d|_)+\\s"
											// name and either semicolon or equals with a new object/primitive type.
											+ "(\\w|\\d|_|\\.){0,50}(\\w|\\d|_)\\s*(;|=(\\s)*(\\w|\\d|_|\\.){0,50}(\\w|\\d|_)+;))")) {
							// if no import, the class is modified.
								addRem = 1;
							} else {
								addRem = 0;
							}
=======
							addRem = 0;
>>>>>>> dcd1a3b06a7c10071d7c4811488003e91ce11a3b
							beginningLine = actualLine;
						}
						modifications.append(line.substring(1) + (line.endsWith("\n") ? "" : "\n"));
					}
					// removal
					else if (line.startsWith("-") && !line.startsWith("\"", 1)) {
						/*
						 *  if there were modifications that have not been added yet, 
						 *  they must be added first. 
						 */
						if (lineBeforeWasAdded == 'a') {
							if (changes == null) {
								changes = new Changes();
							}
							if (change.getChanges() != null && change.getChanges() != "" && 
									change.getChanges().length() > 0) {
								changes.add(change);
								change = new Change();
							}
						}
						lineBeforeWasAdded = 'r';
						// necessary for removals directly succeeding additions
						if (addRem > -1) {
							if (changes == null) {
								changes = new Changes();
							}
<<<<<<< HEAD
							change = createChange(addRem, beginningLine, qualifiedClassName, 
									modifications.toString());
=======
							change.setAddRem(addRem);
							change.setBeginningLine(beginningLine);
<<<<<<< HEAD
							change.setQualifiedClassName(qualifiedClassName);
=======
							change.setClassFile(qualifiedClassName);
>>>>>>> d299962c6699a7899a042184feb16b9d25c6636b
							change.setChanges(change.getChanges() + modifications.toString());
>>>>>>> dcd1a3b06a7c10071d7c4811488003e91ce11a3b
							modifications.delete(0, modifications.length());
							addRem = -128;
						}
						if (addRem == -128) {
//							if (line.contains("import")) {
//								addRem = -1;
//							} else {
//								addRem = 0;
//							}
							addRem = -1;
							beginningLine = actualLine;
							skipFirst = true;
						}
						modifications.append(line.substring(1) + (line.endsWith("\n") ? "" : "\n"));
					} else {
						if (addRem != -128) {
							if (changes == null) {
								changes = new Changes();
							}
<<<<<<< HEAD
							change = createChange(addRem, beginningLine, qualifiedClassName, 
									modifications.toString());
=======
							change.setAddRem(addRem);
							change.setBeginningLine(beginningLine);
<<<<<<< HEAD
							change.setQualifiedClassName(qualifiedClassName);
=======
							change.setClassFile(qualifiedClassName);
>>>>>>> d299962c6699a7899a042184feb16b9d25c6636b
							change.setChanges(change.getChanges() + modifications.toString());
>>>>>>> dcd1a3b06a7c10071d7c4811488003e91ce11a3b
							modifications.delete(0, modifications.length());
							addRem = -128;
							lineBeforeWasAdded = 'u';
						}
					}
					if (addRem < 1 && addRem != -128) {
						actualLine = skipFirst ? actualLine : actualLine-1;
						skipFirst = false;
					} else {
						actualLine++;
					}

					// if last line of a commit segment is reached, add potentially Change to changes.
					if (line.equals("\\ No newline at end of file") || line.equals(lines[lines.length-1])) {
						if (change.getChanges() == null || change.getChanges().equals("")) {
							continue;
						} else {
<<<<<<< HEAD
							if (change.getChanges() != null && change.getChanges() != "" && 
									change.getChanges().length() > 0) {
								changes.add(change);
								change = new Change();
							}
=======
							changes.add(change);
							change = new Change();
>>>>>>> dcd1a3b06a7c10071d7c4811488003e91ce11a3b
						}
					}
				}
				s.add(t);
			}

			changesList.add(changes);
<<<<<<< HEAD
		}
		prepDiff.setModificationList(changesList);
	}
	
	private String extractQualifiedClassName(String classCode) {
		String qualifiedClassName = "";
		String packageRegex = "(package\\s([a-zA-Z0-9_]+[\\.]?)+;)";
		String classRegex = "((public|protected|private)?\\sclass\\s[a-zA-Z0-9_]+)";//(\\s(extends|implements))?\\s([a-zA-Z0-9_<>,]+)?\\(?)";
		Pattern pattern = Pattern.compile(packageRegex);
		Matcher matcher = pattern.matcher(classCode);
		while (matcher.find()) {
//			qualifiedClassName = memberMatcher.group(0);
			qualifiedClassName += matcher.group().replaceFirst(";", "");
			qualifiedClassName = qualifiedClassName.substring(8);
		}
		pattern = Pattern.compile(classRegex);
		matcher = pattern.matcher(classCode);
		while (matcher.find()) {
			qualifiedClassName += "." + matcher.group().replaceFirst("(public|protected|private)?\\sclass\\s", "");
		}
		return qualifiedClassName;
	}
	
	private Change createChange(byte addRem, int beginningLine, String qualifiedClassName, 
			String modifications) {

		ModifiesTypeExaminer mte = new ModifiesTypeExaminer();
		Change change = new Change();
		
		change.setAddRem(addRem);
		if (change.getTypeOfChange() == null || change.getTypeOfChange() == "") {
			change.setTypeOfChange(mte.examineModifiesType(addRem, modifications.toString()));
		}
		change.setBeginningLine(beginningLine);
		change.setQualifiedClassName(qualifiedClassName);
		// if actual changes are null, take "" + mods
		change.setChanges((change.getChanges() == null ? "" : change.getChanges())
				+ modifications.toString());
		return change;
=======
		}
		prepDiff.setModificationList(changesList);
>>>>>>> dcd1a3b06a7c10071d7c4811488003e91ce11a3b
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
}
