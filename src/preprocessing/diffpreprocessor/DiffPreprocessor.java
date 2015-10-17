package preprocessing.diffpreprocessor;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import preprocessing.diffs.ClassChanges;
import preprocessing.diffs.Commit;
import preprocessing.diffs.PreprocessedDiff;
import projecttree.Node;
import projecttree.NodeType;
import projecttree.ProjectTreeSearcher;
import deltatransformation.ModifiesTypeExaminer;

public class DiffPreprocessor {

	private String input;
	private PreprocessedDiff prepDiff;
	private LinkedList<Commit> changesList;
	private Node root;
	
	public DiffPreprocessor() {
		prepDiff = new PreprocessedDiff();
		changesList = new LinkedList<Commit>();
		root = new Node("ProjectRoot", NodeType.PROJECT);
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
		Commit changes = null;
		ClassChanges change = null;
		String lineInfoRegex = "\\-[\\d]{1,4},[\\d]{1,2}\\s\\+[\\d]{1,4},[\\d]{1,2}";
		Pattern lineInfoPattern = Pattern.compile(lineInfoRegex);
		Matcher lineInfoMatcher;
		DiffAnalyzer da = new DiffAnalyzer();
		LineChecker lc = new LineChecker();
		String[] sa = input.split("@@");
		for (String commitPart: sa) {
			if (commitPart.isEmpty() || commitPart.matches("[\\s]*")) {
				continue;
			}
			lineInfoMatcher = lineInfoPattern.matcher(commitPart);
			// if line information is given, beginning line is set to the beginning of the listing.
			if (lineInfoMatcher.find()) {
				continue;
			}
			// if no line info found, code base is reached.
			else {

				if (!commitPart.contains("package")) {
					continue;
				}
				da.analyzeDiff(commitPart, root);
				showTree(root);
				if (lc.isWholeClass(commitPart)) {
					// may return null, if there are different types of lines (existent, removed, added)
					change = separateWholeClass(commitPart);
					if (change == null) {
						continue;
					} else {
						if (changes == null) {
							changes = new Commit();
						}
						changes.add(change);
						changesList.add(changes);
					}
				}
			}
		}
		prepDiff.setModificationList(changesList);
	}
	
	/**
	 * Iterates over all lines adding the lines to the returned Change object. If not all lines have the same 
	 * modification operator (+ or -) null is returned. 
	 * @param diffCode
	 * @return
	 */
	private ClassChanges separateWholeClass(String diffCode) {
		int actualLine = -1,
			// for getting correct change lines, increments in each iteration. 
			beginningLine = -1;
		// for iterating over each line
		byte addRem = -128;
		boolean firstLine = true;
		ModificationType modificationType = null;
		String qualifiedClassName = null;
		StringBuilder modifications = new StringBuilder();
		Commit changes = null;
		String[] lines = diffCode.split("\\r?\\n");
		for (String line : lines) {
			if (firstLine && line.equals("")) {
				firstLine = false;
				continue;
			}
			if (line.contains("package")) {
				qualifiedClassName = line.substring(line.lastIndexOf("package") + 8, line.indexOf(";"));
			} else if (line.contains("class")) {
				qualifiedClassName += "." + line.substring(line.lastIndexOf("class")+6, line.indexOf(" {"));
			}
			// addition, addRem for the case, that there exist added and removed lines and thus not the whole class is affected.
			if (line.startsWith("+")) {
				if (addRem == -1) {
					return null;
				}
				// necessary for additions directly succeeding removals
				if (changes == null) {
					changes = new Commit();
				}
				if (addRem == -128) {
					addRem = 1;
					modificationType = ModificationType.CLASSADDITION;
					beginningLine = actualLine;
				}
				modifications.append(line.substring(1) + (line.endsWith("\n") ? "" : "\n"));
			} else if (line.startsWith("-")) {
				if (addRem == 1) {
					return null;
				}
				// necessary for additions directly succeeding removals
				if (changes == null) {
					changes = new Commit();
				}
				if (addRem == -128) {
					addRem = -1;
					modificationType = ModificationType.CLASSREMOVAL;
					beginningLine = actualLine;
				}
				modifications.append(line.substring(1) + (line.endsWith("\n") ? "" : "\n"));
			} else if (line.equals("\\ No newline at end of file") || line.equals(lines[lines.length-1])) {
				break;
			} else {
				return null;
			}
		}
		return new ClassChanges(qualifiedClassName, beginningLine, addRem, 
				modificationType, true, modifications.toString());
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
		//
		boolean skipFirst = false;
		boolean alreadyProcessed = false;
		/*
		 *  Can have three possible states: a (added), r (removed) and u (unmodified).
		 *  Identifies if a modification is completed, necessary for modifications
		 *  without separating non-modified lines.
		 */
		char lineBeforeWasAdded = 'u';
		String lineBefore = null;
		DiffAnalyzer da = new DiffAnalyzer();
		LineChecker lc = new LineChecker();
		StringBuilder modifications = new StringBuilder();
		Commit changes = null;
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
				changes = new Commit();
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
				ClassChanges change = new ClassChanges();
				root = null;
				root = new Node("ProjectRoot", NodeType.PROJECT);
				da.analyzeDiff(commitPart, root);
//				showTree(root);
				
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

				if (lc.isWholeClass(commitPart)) {
					// may return null, if there are different types of lines (existent, removed, added)
					ClassChanges c = separateWholeClass(commitPart);
					if (c == null) {
						continue;
					}
					if (changes == null) {
						changes = new Commit();
					}
					changes.add(c);
					changesList.add(changes);
					continue;
				}
				lines = commitPart.split("\\r?\\n");
				alreadyProcessed = false;
				lineBeforeWasAdded = 'u';
				for (String line : lines) {
					/*
					 * Necessary because a lot of lines have to be obtained to get the fully qualified name
					 * of the respective class. If ensures that all lines are skipped that have no modi-
					 * fications.
					 */
					if (!(line.startsWith("+") || line.startsWith("-") || line.equals(lines[lines.length-1]))) {

						/*
						 *  if there have been changes before, add, if new mods are of different kind,
						 *  e.g. first added, then removed.
						 */
						if (modifications != null && modifications.toString() != "" && 
								modifications.length() > 0) {
							// if before something was added and now is removed, add the additions first, same applies for removals succeeded by additions.
							if ((addRem == 1 && line.startsWith("-")) || (addRem == -1 && line.startsWith("+"))) {

								beginningLine = getBeginningLineOfChange(commitPart, modifications.toString());
								changes.add(createChange(addRem, beginningLine, qualifiedClassName, 
										false, modifications.toString()));
								modifications.delete(0, modifications.length());
								addRem = -128;
							} else if ((addRem == 1 || addRem == -1) && !(line.startsWith("-") || line.startsWith("+"))) {
							// if line before was added or removed and the actual line is not changed.
								beginningLine = getBeginningLineOfChange(commitPart, modifications.toString());
								changes.add(createChange(addRem, beginningLine, qualifiedClassName, 
										false, modifications.toString()));
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
							String[] tempArray;
							
							// additions
							if (temp != "") {
								tempArray = temp.trim().split("(\\s|,)");

								if (temp.contains("extends")) {
									superclass = lc.getSuperclassFromLineArray(tempArray);
									if (superclass != "") {

										beginningLine = getBeginningLineOfChange(commitPart, interfaces.replace("superclass ", "extends "));
										changes.add(createChange((byte) 1, beginningLine, qualifiedClassName, 
												false, superclass));
										alreadyProcessed = true;
									}
								}
								if (temp.contains("implements")) {
									interfaces = lc.getInterfacesFromLineArray(tempArray);
									if (interfaces != "") {
										beginningLine = getBeginningLineOfChange(commitPart, interfaces.replace("interfaces ", "implements "));
										changes.add(createChange((byte) 1, beginningLine, qualifiedClassName, 
												false, interfaces));
										alreadyProcessed = true;
									}
								}
							}

							// removals
							superclass = "";
							interfaces = "";
							temp = lc.getNewPartsOfLine(lineBefore.substring(1), line.substring(1)).trim();
							if (temp.endsWith(",")) {
								temp = temp.substring(0, temp.length()-1);
							}
							if (!temp.equals("")) {
								tempArray = temp.split("\\s");
								if (lineBefore.contains("extends") && !line.contains("extends")) {
									superclass = lc.getSuperclassFromLineArray(tempArray);
									if (superclass != "") {
										beginningLine = getBeginningLineOfChange(commitPart, interfaces.replace("superclass ", "extends "));
										changes.add(createChange((byte) -1, beginningLine, qualifiedClassName, 
												false, superclass));
										alreadyProcessed = true;
									}
								}
								if (temp.contains("implements")) {
									interfaces = lc.getInterfacesFromLineArray(tempArray);
									if (!interfaces.equals("")) {

										beginningLine = getBeginningLineOfChange(commitPart, interfaces.replace("interfaces ", "implements "));
										changes.add(createChange((byte) -1, beginningLine, qualifiedClassName, 
												false, interfaces));
										alreadyProcessed = true;
									}
								}
							}
							if (temp.contains("extends") || temp.contains("implements")) {
								modifications.delete(0, modifications.length());
							}
							addRem = -128;


							
							if (changes == null) {
								changes = new Commit();
							}
							// modifications must contain something AND if the line has not been processed before and don't equals lineBefore!
							if (modifications != null && modifications.toString() != "" && 
									modifications.length() > 0 && 
									!(alreadyProcessed || modifications.toString().equals(lineBefore.substring(1)))) {

								beginningLine = getBeginningLineOfChange(commitPart, modifications.toString());
								changes.add(createChange(addRem, beginningLine, qualifiedClassName, 
										false, modifications.toString()));
								modifications.delete(0, modifications.length());
								addRem = -128;
							} else {
								modifications.delete(0, modifications.length());
							}
						}
						lineBeforeWasAdded = 'a';
						if (addRem < 1) {
							// imports just can be added or removed, thus 0 is not allowed.
							addRem = 1;
						}
						// if changes have been already added (for interfaces and superclasses) don't add them again
						if (!alreadyProcessed) {
							modifications.append(line.substring(1) + (line.endsWith("\n") ? "" : "\n"));
						} 
						alreadyProcessed = false;
					}
					// removal
					else if (line.startsWith("-") && !line.startsWith("\"", 1)) {
						/*
						 *  if there were modifications that have not been added yet, 
						 *  they must be added first. 
						 */
						if (lineBeforeWasAdded == 'a') {
							// Attention! Removals of interfaces and superclasses are treated in addition case!
							if (changes == null) {
								changes = new Commit();
							}
							if (change.getChanges() != null && change.getChanges() != "" && 
									change.getChanges().length() > 0) {
								changes.add(change);
							}
						}
						lineBeforeWasAdded = 'r';
						// necessary for removals directly succeeding additions
						if (addRem > -1 && !modifications.toString().equals("")) {
							if (changes == null) {
								changes = new Commit();
							}
							beginningLine = getBeginningLineOfChange(commitPart, modifications.toString());
							change = createChange(addRem, beginningLine, qualifiedClassName, 
									false, modifications.toString());
							modifications.delete(0, modifications.length());
							addRem = -128;
						}
						if (addRem == -128) {
							addRem = -1;
							beginningLine = actualLine;
							skipFirst = true;
						}
						modifications.append(line.substring(1) + (line.endsWith("\n") ? "" : "\n"));
					} else {
						if (addRem != -128 && !modifications.toString().equals("")) {
							if (changes == null) {
								changes = new Commit();
							}	

							beginningLine = getBeginningLineOfChange(commitPart, modifications.toString());
							change = createChange(addRem, beginningLine, qualifiedClassName, 
									false, modifications.toString());
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

			if (!changesList.contains(changes)) {
				changesList.add(changes);
			}
		}
		prepDiff.setModificationList(changesList);
	}
	
	/**
	 * 
	 * @param commitPart
	 * @param modifications
	 * @return
	 */
	private int getBeginningLineOfChange(String commitPart, String modifications) {
		// remove lines that are marked "removed" to get the correct line for additions and modifications.
		String tempCommitPart = commitPart.replaceAll("(?m)^-.*", "");
		String mods = modifications.trim();
		String innersubstring = "";
		if (mods.contains("\n")) {
			innersubstring = mods.substring(0, mods.indexOf("\n"));
		} else {
			innersubstring = mods;
		}
		
		int indexddd = tempCommitPart.indexOf(innersubstring);
		// if not contained, look, if it is a removal, that previously would have been removed.
		if (indexddd < 0) {
			/*
			 * with adding or removing interfaces the problem may occur, that "implements" and the respective interface/s
			 * are separated. Thus, substring or contains will not fit in this case and "implements" is used instead.
			 */
			if (innersubstring.contains("implements")) {
				innersubstring = "implements";
			}
			// get everything from the beginning of the original commitPart up to the begin of the modifications.
			String t = commitPart.substring(0, commitPart.indexOf(innersubstring));
			String[] ta = t.split("\\n");
			if (ta[ta.length-1].startsWith("-")) {
				return ta.length-1;
			} else {
				return -1;
			}
		}
		String outersubstring = tempCommitPart.substring(0, indexddd);
		String[] ttta = outersubstring.split("\\n");
		return ttta.length;
	}
	
	private String extractQualifiedClassName(String classCode) {
		String qualifiedClassName = "";
		String packageRegex = "(package\\s([a-zA-Z0-9_]+[\\.]?)+;)";
		String classRegex = "((public|protected|private)?\\sclass\\s[a-zA-Z0-9_]+)";
		Pattern pattern = Pattern.compile(packageRegex);
		Matcher matcher = pattern.matcher(classCode);
		while (matcher.find()) {
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
	
	private ClassChanges createChange(byte addRem, int beginningLine, String qualifiedClassName, boolean isWholeClass,
			String modifications) {

		ModifiesTypeExaminer mte = new ModifiesTypeExaminer();
		ProjectTreeSearcher pts = new ProjectTreeSearcher();
		ClassChanges change = new ClassChanges();
		Node containingPackage = null,
			 containingClass = null,
			 modifiedMethod = null;
		String modifiedMethodName = null;
		
		
		change.setBeginningLine(beginningLine);
		change.setQualifiedClassName(qualifiedClassName);
		change.setIsWholeClass(isWholeClass);

		change.setAddRem(addRem);
		if (change.getTypeOfChange() == null) {
			change.setTypeOfChange(mte.examineModifiesType(addRem, modifications.toString()));
			
			if (change.getTypeOfChange() != null && change.getTypeOfChange().equals(ModificationType.ADDSFIELD)) {
				modifiedMethod = pts.getModifiedMethodNode(root, beginningLine, modifications.split("\\n").length, qualifiedClassName);
				if (modifiedMethod != null) {
					modifiedMethodName = pts.getModifiedMethodName(root, beginningLine, modifications.split("\\n").length, qualifiedClassName);
					change.setTypeOfChange(ModificationType.MODIFIESMETHOD);
					if (modifiedMethodName.substring(modifiedMethodName.length()-2).equals("#s")) {
						change.setIsMethodModifiedAtStart(true);
					} else {
						change.setIsMethodModifiedAtStart(false);
					}
				}
			}
		} 
		
		if (change.getTypeOfChange() != null && change.getTypeOfChange().equals(ModificationType.REMOVESMETHOD)) {
			modifiedMethod = pts.getModifiedMethodNode(root, beginningLine, modifications.split("\\n").length, qualifiedClassName);
			containingPackage = root.getChild(change.getPackageName());
			containingClass = containingPackage.getChild(change.getClassName());
			containingClass.removeChild(modifiedMethod);
		}
		if (modifiedMethod != null) {
			change.setModifiedMethod(modifiedMethod);
		}
		if (change.getTypeOfChange() == null) {
			
		}

		// if actual changes are null, take "" + mods
		change.setChanges((change.getChanges() == null ? "" : change.getChanges())
				+ modifications.toString());
		if (change.getTypeOfChange().equals(ModificationType.ADDSINTERFACE)) {
			change.setChanges(change.getChanges().replace("implements ", "").trim());
		} 
		if (change.getTypeOfChange().equals(ModificationType.ADDSSUPERCLASS)) {
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
	
	private void showTree(Node root) {	
		System.out.println("#################################################################################################################################################\n" 
				+ "#################################################################################################################################################");
		System.out.println("Project: " + root.getName());
		for (Node n : root.getAllChildren()) {
			System.out.println("  Package: " + n.getName());
			for (Node n2 : n.getAllChildren()) {
				System.out.println("    Class: " + n2.getName());
				System.out.println("      Length: " + n2.getLength());
				for (Node n3 : n2.getAllChildrenOfType(NodeType.METHOD)) {
					System.out.println("      Method: " + n3.getName());
					System.out.println("\tReturn Type: " + n3.getJavaType());
					System.out.println("\tBeginningLine: " + n3.getBeginningLine());
					System.out.println("\tLength: " + n3.getLength());
					for (Node n4 : n3.getAllChildrenOfType(NodeType.PARAMETER)) {
						System.out.println("\tParameter: " + n4.getName() + " ParamType: " + n4.getJavaType());
					}
					for (Node n4 : n3.getAllChildrenOfType(NodeType.FIELD)) {
						System.out.println("\tLocal Field: " + n4.getName() + " Type: " + n4.getJavaType());
					}
				}
				for (Node n3 : n2.getAllChildrenOfType(NodeType.FIELD)) {
					System.out.println("      Field: " + n3.getName());
					System.out.println("\tType: " + n3.getJavaType());
					System.out.println("\tBeginningLine: " + n3.getBeginningLine());
					System.out.println("\tPARENT: " + n3.getParent().getType().toString() + " " + n3.getParent().getName());
				}
			}
		}
		System.out.println("#################################################################################################################################################\n" 
				+ "#################################################################################################################################################");
	}
}
