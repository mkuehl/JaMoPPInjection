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
		for (String commitPart: sa) {
//			System.out.println(commitPart);
			if (commitPart.isEmpty() || commitPart.matches("[\\s]*")) {
				continue;
			}
//			if (actualLine > 2 && (commitPart.startsWith(" public class") || commitPart.startsWith(" protected class") 
//					|| commitPart.startsWith(" class") || commitPart.startsWith(" private class"))) {
//				skipFirst = true;
//			}
			lineInfoMatcher = lineInfoPattern.matcher(commitPart);
			// if line information is given, beginning line is set to the beginning of the listing.
			if (lineInfoMatcher.find()) {
				actualLine = Integer.parseInt(commitPart.substring(commitPart.indexOf("+"), commitPart.lastIndexOf(",")));
			}
			// if no line info found, code base is reached.
			else {

				if (!commitPart.contains("package")) {
					continue;
				}
				lines = commitPart.split("\\r?\\n");
				for (String line : lines) {
					if (line.contains("package")) {
						qualifiedClassName = line.substring(line.lastIndexOf("package") + 8, line.indexOf(";"));
					} else if (line.contains("class")) {
						qualifiedClassName += "." + line.substring(line.lastIndexOf("class")+6, line.indexOf(" {"));
					}
					if (line.equals("\\ No newline at end of file") || line.equals(lines[lines.length-1])) {
						break;
					}
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
				changes.add(new Change(qualifiedClassName, beginningLine, addRem, "a", 
						modifications.toString()));
				modifications.delete(0, modifications.length());
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
		boolean alreadyAdded = false;
		/*
		 *  Can have three possible states: a (added), r (removed) and u (unmodified).
		 *  Identifies if a modification is completed, necessary for modifications
		 *  without separating non-modified lines.
		 */
		char lineBeforeWasAdded = 'u';
		String lineBefore = null;
		LineChecker lc = new LineChecker();
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
		for (String commitPart: sa) {
			if (commitPart.isEmpty() || commitPart.matches("[\\s]*")) {
				continue;
			}
			// if commitPart is a code block, it starts with the class and a { or for the first addition with a newline. This has to be removed.
			if (commitPart.startsWith("\n")) {
				commitPart = commitPart.substring(1);
			} else if (commitPart.startsWith("\r\n")) {
				commitPart = commitPart.substring(2);
			}
			mCommitExtract = pCommitExtract.matcher(commitPart);
			// if commit hash is found, save it and continue.
			if (mCommitExtract.find()) {
				// the actual changes have to be added to the superior changesList here
//				if (changes != null && changes.isEverythingSet()) {
//					changesList.add(changes);
//					changes = null;
//				}
				changes = new Changes();
				changes.setCommitHash(mCommitExtract.group());
				continue;
			}
			lineInfoMatcher = lineInfoPattern.matcher(commitPart);
			// if line information is given, beginning line is set to the beginning of the listing.
			if (lineInfoMatcher.find()) {
				actualLine = Integer.parseInt(commitPart.substring(commitPart.indexOf("+"), commitPart.lastIndexOf(",")));
				// important for not adding a Changes object with line and commit info only
				continue;
			}
			// [PATCH] marks the beginning of the commit message line.
			else if(commitPart.startsWith("[PATCH]")) {
				changes.setCommitMessage(commitPart.substring(7, commitPart.indexOf("\n")));
				// continue is important for not adding a Changes object with the commit message only
				continue;
			}
			// if no line info found, check if commitPart starts with "[PATCH]", if not, iterate over lines.
			else if (!commitPart.startsWith("[PATCH]")) {
				Change change = new Change();
				
				if (ClassAdditionExaminer.isWholeClassAdded(commitPart)) {
					change.setAddRem((byte) 1);
					change.setTypeOfChange("a");
				} else if (ClassAdditionExaminer.isWholeClassRemoved(commitPart)) {
					change.setAddRem((byte) -1);
					change.setTypeOfChange("r");
				}
				/* 
				 * if commitPart contains no package name, the changes can not be assigned to a specific class since 
				 * in DeltaJ class names have to be qualified.
				 */
				if (!commitPart.contains("package")) {
					continue;
				}
				/*
				 *  if actualLine is bigger than 2 (if git log shows 3 surrounding lines (standard)) and 
				 *  begins with a modifier and class, then the first line must be skipped.
				 *  Space at the beginning of each startsWith parameter, because git log outputs one.
				 */
				if (actualLine > 2 && (commitPart.startsWith(" public class") || commitPart.startsWith(" protected class") 
						|| commitPart.startsWith(" class") || commitPart.startsWith(" private class"))) {
					skipFirst = true;
				}

				qualifiedClassName = extractQualifiedClassName(commitPart);
				lines = commitPart.split("\\r?\\n");
				alreadyAdded = false;
				for (String line : lines) {
					/*
					 * Necessary because a lot of lines have to be obtained to get the fully qualified name
					 * of the respective class. If ensures that all lines are skipped that have no modi-
					 * fications.
					 */
					if (!(line.startsWith("+") || line.startsWith("-") || line.equals(lines[lines.length-1]))) {

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
						continue;
					}
					if (skipFirst) {
						skipFirst = false;
						if (qualifiedClassName == null || qualifiedClassName == "") {
							qualifiedClassName = commitPart.substring(commitPart.lastIndexOf("class")+6, commitPart.indexOf(" {"));
						}
						continue;
					}
					// addition, excluding a multiline string with leading + for second and later fragments
					if (line.startsWith("+") && !line.startsWith("\"", 1)) {
						if (lineBeforeWasAdded == 'r') {
							/*
							 *  if line before was removed, check if the actual line contains the removed line. If so, the line 
							 *  has been extended.
							 */

							String temp = lc.getNewPartsOfLine(line.substring(1), lineBefore.substring(1)).trim(),
								   superclass = "",
								   interfaces = "";
//							if (line.substring(1).contains(lineBefore.substring(1).replace("{", ""))) {
							if (!temp.equals("")) {
								// additions
								// as parameters cut the first character, because it is always a - or +.
//								String temp = lc.getNewPartsOfLine(line.substring(1), lineBefore.substring(1)).trim(),
//									   superclass = "",
//									   interfaces = "";
								String[] tempArray;
								if (temp != "") {
									tempArray = temp.trim().split("(\\s|,)");
									
									if (temp.contains("extends")) {
										superclass = lc.getSuperclassFromLineArray(tempArray);
										if (superclass != "") {
											changes.add(createChange((byte) 1, beginningLine, qualifiedClassName, 
													superclass));
											alreadyAdded = true;
										}
									}
									if (temp.contains("implements")) {
										interfaces = lc.getInterfacesFromLineArray(tempArray);
										if (interfaces != "") {
											changes.add(createChange((byte) 1, beginningLine, qualifiedClassName, 
														interfaces));
											alreadyAdded = true;
										}
									}
								}
								// removals
								temp = lc.getNewPartsOfLine(lineBefore.substring(1), line.substring(1)).trim();
								if (!temp.equals("")) {
									tempArray = temp.split("\\s");
									if (line.contains("extends")) {
										superclass = lc.getSuperclassFromLineArray(tempArray);
										if (superclass != "") {
											changes.add(createChange((byte) -1, beginningLine, qualifiedClassName, 
													superclass));
											alreadyAdded = true;
										}
									}
									if (temp.contains("implements")) {
										interfaces = lc.getInterfacesFromLineArray(tempArray);
										if (!interfaces.equals("")) {
											changes.add(createChange((byte) -1, beginningLine, qualifiedClassName, 
														interfaces));
											alreadyAdded = true;
										}
									}
								}
								if (temp.contains("extends") || temp.contains("implements")) {
									modifications.delete(0, modifications.length());
								}
							}
							
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
						}
						lineBeforeWasAdded = 'a';
						if (addRem == -128) {
							// imports just can be added or removed, thus 0 is not allowed.
//							if (line.contains("import")) {
//								addRem = 1;
//							} else if (line.substring(1).matches("([\\s]*(public|protected|private)?\\s(\\w|\\d|_|\\.){0,50}(\\w|\\d|_)+\\s"
//											// name and either semicolon or equals with a new object/primitive type.
//											+ "(\\w|\\d|_|\\.){0,50}(\\w|\\d|_)\\s*(;|=(\\s)*(\\w|\\d|_|\\.){0,50}(\\w|\\d|_)+;))")) {
//							// if no import, the class is modified.
//								addRem = 1;
//							} else {
//								addRem = 1;
//							}
							addRem = 1;
							beginningLine = actualLine;
						}
						if (!alreadyAdded) {
							modifications.append(line.substring(1) + (line.endsWith("\n") ? "" : "\n"));
							alreadyAdded = false;
						} 
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
//								change = new Change();
							}
						}
						lineBeforeWasAdded = 'r';
						// necessary for removals directly succeeding additions
						if (addRem > -1 && !modifications.toString().equals("")) {
							if (changes == null) {
								changes = new Changes();
							}
							change = createChange(addRem, beginningLine, qualifiedClassName, 
									modifications.toString());
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
						if (addRem != -128 && !modifications.toString().equals("")) {
							if (changes == null) {
								changes = new Changes();
							}
							change = createChange(addRem, beginningLine, qualifiedClassName, 
									modifications.toString());
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
							if (change.getChanges() != null && change.getChanges() != "" && 
									change.getChanges().length() > 0) {
								changes.add(change);
//								change = new Change();
							}
						}
					}
					lineBefore = line;
				}
				s.add(commitPart);
			}

			changesList.add(changes);
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
		// not in loop because in case of modifications to class line (mod interfaces, superclass)
		matcher.find();
		qualifiedClassName += "." + matcher.group().replaceFirst("(public|protected|private)?\\sclass\\s", "");

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
		if (change.getTypeOfChange().equals("main")) {
			change.setChanges(change.getChanges().replace("implements ", "").trim());
		} 
		if (change.getTypeOfChange().equals("masc")) {
			change.setChanges(change.getChanges().replace("extends ", "").trim());
		}
		return change;
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
