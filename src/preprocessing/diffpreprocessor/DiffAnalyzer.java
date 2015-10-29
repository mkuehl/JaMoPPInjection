package preprocessing.diffpreprocessor;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import projecttree.Node;
import projecttree.NodeType;
import projecttree.ProjectTreeFactory;

public class DiffAnalyzer {
	
	protected void analyzeDiff(String diff, Node root) {
		if (diff == null || diff.equals("") || root == null) {
			return;
		}
		ProjectTreeFactory ptf = new ProjectTreeFactory();
		Node packageNode = null,
			 classNode = null;
		Cleaner c = new Cleaner();
		String tempDiff = c.cleanDiffFromComments(diff);
//		String[] lines = diff.split("\\r?\\n");
		String packageRegex = "package[\\s]+[a-zA-Z0-9_]+(\\.[a-zA-Z0-9_]+)*;";
		String classRegex = "(\\s)*((public|protected|private)\\s)?(\\s)*(abstract\\s)?(class|interface)[\\s]+[a-zA-Z0-9_]+";
		String importRegex = "import[\\s]+[a-zA-Z0-9_]+(\\.([a-zA-Z0-9_]+|\\*))+;";
		String methodRegex = "(\\s)*((public|protected|private)\\s)?(\\s)*[a-zA-Z0-9_]*[\\s]*[a-zA-Z0-9_]+[\\s]*\\(.*\\)[\\s]*\\{";
		String parameterRegex = "(\\s)*(final)?(\\s)*[a-zA-Z0-9_]+(\\s)*[\\[\\]]*(\\s)+[a-zA-Z0-9_]+(\\s)*[\\[\\]]*(\\s)*(,)?";
		String fieldRegex = "(?!\\breturn\\b)(public|protected|private)?[\\s]+(?!\\bnull;\\b)([a-zA-Z0-9_]+)[\\s]*[\\[\\]]*(\\s)+[a-zA-Z0-9_]+[\\s]*[\\[\\]]*(\\s)*(;|=)";
//		InputStream in = this.getClass().getClassLoader().getResourceAsStream("app.RegExpressions");
//		final Properties configProp = new Properties();
//		  try {
//	          configProp.load(in);
//	      } catch (IOException e) {
//	          e.printStackTrace();
//	      }
//		String packageRegex = configProp.getProperty("IdentifyPackageRegex");
//		String classRegex = configProp.getProperty("IdentifyClassRegex");
//		String importRegex = configProp.getProperty("IdentifyImportStatementRegex");
//		String methodRegex = configProp.getProperty("IdentifyMethodRegex");
//		String parameterRegex = configProp.getProperty("IdentifyParameterRegex");
//		String fieldRegex = configProp.getProperty("IdentifyFieldRegex");
		
		Pattern packagePattern = Pattern.compile(packageRegex);
		Pattern classPattern = Pattern.compile(classRegex);
		Pattern importPattern = Pattern.compile(importRegex);
		Pattern methodPattern = Pattern.compile(methodRegex);
		Pattern paramPattern = Pattern.compile(parameterRegex);
		Pattern fieldPattern = Pattern.compile(fieldRegex);
		Matcher m;
		Matcher m2;
		
		//Packages
		m = packagePattern.matcher(tempDiff);
		if (m.find()) {
			String t = m.group();
			t = t.replace("package ", "");
			t = t.replace(";", "");
			packageNode = ptf.createPackage(t);
			HashSet<Node> hsn = root.getAllChildren();
			boolean contained = false;
			for (Node nth : hsn) {
				if (nth.getName().equals(packageNode.getName())) {
					packageNode = nth;
					contained = true;
					break;
				}
			}
			if (!contained) {
				root.addChild(packageNode);
				packageNode.setParent(root);
				contained = false;
			}
		}
		
		m = classPattern.matcher(tempDiff);
		if (m.find()) {
			String t = m.group();
			int length = tempDiff.split("\\n").length;
			String name;
			String modifiers = null;
			if (t.contains("interface")) {
				name = t.substring(t.indexOf("interface ")+10).trim();
				modifiers = t.substring(0, t.indexOf("interface")).trim();
			} else {
				name = t.substring(t.indexOf("class ")+6).trim();
				modifiers = t.substring(0, t.indexOf("class")).trim();
			}
			String[] ta = modifiers.split("\\s");
			HashSet<String> classModifiers = new HashSet<String>();
			HashSet<Node> hsn = packageNode.getAllChildren();
			boolean contained = false;
			
			for (String modifier : ta) {
				classModifiers.add(modifier);
			}
			classNode = ptf.createClass(name, classModifiers);
			classNode.setBeginningLine(0);
			classNode.setLength(length);
			for (Node nth : hsn) {
				if (nth.getName().equals(classNode.getName())) {
					classNode = nth;
					contained = true;
					break;
				}
			}
			if (!contained) {
				packageNode.addChild(classNode);
				classNode.setParent(packageNode);
				contained = false;
			}
		} else {
			System.out.println("No Class Found!!!!!!!!!!!");
			System.out.println();
			System.out.println(tempDiff);
		}
		
		m = importPattern.matcher(tempDiff);
		while (m.find()) {
			String t = m.group();
			String qualifiedName = t.substring(t.indexOf("import ")+7, t.lastIndexOf(";"));
			HashSet<Node> hsn = classNode.getAllChildrenOfType(NodeType.IMPORT);
			Node n = ptf.createImport(qualifiedName);
			boolean contained = false;
			
			for (Node nth : hsn) {
				if (nth.getName().equals(n.getName())) {
					contained = true;
					break;
				}
			}
			if (!contained) {
				classNode.addChild(n);
				n.setParent(classNode);
				contained = false;
			}
		}
		
		m = methodPattern.matcher(tempDiff);
		while(m.find()) {
			String t = m.group().trim();
			if (t.replace("(", "").trim().equals("if") || t.replace("(", "").trim().equals("else if") ||
					t.contains("new")) {
				continue;
			}
//			String tt = (tempDiff.substring(0, m.end()));
//			String[] tta = tt.split("\\n");
			// -1 due to the linebreak of the last line.
			int beginningLine = getBeginningLineOfMember(tempDiff, t);//tta.length - 1;
			// -1 because first line is counted.
			String ttt = tempDiff.substring(m.end());
			int length = getLengthOfMember(ttt)-1;
			// if name was changed.
			if (beginningLine == -1) {
				continue;
			}
			String name = null;
			try {
				// methods might have two space characters. If so, extract the name from the second, if not, extract it from the first.
				name = t.substring(t.indexOf(" ")+1, t.indexOf("(")).trim();
			} catch (StringIndexOutOfBoundsException e) {
				//not whitespace character
				name = t.substring(0, t.indexOf("(")).trim();
			}
			String returnType = "";
			String[] ta = t.split("\\s");
			String parameterString;
			if (t.contains(" ")) {
				returnType = t.replaceAll("public|protected|private|abstract|final|volatile|native|strict|synchronized|transient", "");
				name = name.substring(name.indexOf(" ")+1);
				returnType = returnType.substring(0, returnType.indexOf(name)).trim();
			} else {
				// in case of constructors
				returnType = name;
			}
			HashSet<Node> hsn = classNode.getAllChildrenOfType(NodeType.METHOD);
			Node method = null;
			boolean contained = false;
			
			method = ptf.createMethod(name, null);
			method.setBeginningLine(beginningLine);
			method.setLength(length);
			method.setJavaType(returnType);
			for (String modifier : ta) {
				method.addModifier(modifier);
			}
			for (Node nth : hsn) {
				if (nth.getName().equals(method.getName())) {
					contained = true;
					break;
				}
			}
			if (!contained) {
				classNode.addChild(method);
				method.setParent(classNode);
				contained = false;
				if (!t.contains("(") && !t.contains(")")) {
					continue;
				}
				
				// find params, if existent
				parameterString = tempDiff.substring(tempDiff.indexOf("(", m.start()), tempDiff.indexOf(")", m.start())+1);
				m2 = paramPattern.matcher(parameterString);
				while (m2.find()) {
					if (parameterString.contains(";")) {
						break;
					}
					String parameterName = m2.group().trim();
					String javaType = "";
					boolean isFinal = false;
					HashSet<Node> hsp = method.getAllChildrenOfType(NodeType.PARAMETER);
					javaType = parameterName.substring(0, parameterName.lastIndexOf(" ")).trim();
					isFinal = (javaType.contains("final") ? true : false);
					parameterName = parameterName.substring(parameterName.lastIndexOf(" ")+1).trim();
					if (parameterName.endsWith(",")) {
						parameterName = parameterName.replace(",", "");
					}
					Node paramNode = ptf.createParameter(parameterName);
					if (isFinal) {
						javaType = javaType.replace("final", "").trim();
						paramNode.addModifier("final");
					}
					paramNode.setJavaType(javaType);
					for (Node p : hsp) {
						if (p.getName().equals(paramNode.getName())) {
							contained = true;
							break;
						}
					}
					if (!contained) {
						if (paramNode != null && !paramNode.getName().equals("") && !paramNode.getJavaType().equals("")) {
							method.addChild(paramNode);
							paramNode.setParent(method);
							contained = false;
						}
					}
				}
			}
		}
		
		m = fieldPattern.matcher(tempDiff);
		while (m.find()) {
			String t = m.group();
			// no return statements.
			if (t.contains("return") || t.contains("package")) {
				continue;
			}
			// -1 due to the linebreak of the last line.
			int beginningLine = tempDiff.substring(0, m.end()).split("\\n").length-1;
			// -1 because first line is counted.
//			int length = getLengthOfMember(tempDiff.substring(m.end()))-1;
			// methods might have two space characters. If so, extract the name from the second, if not, extract it from the first.
			String name;
			String returnType = "";
			String modifiers;
			if (t.contains(";")) {
				name = t.replace(";", "").trim();
				returnType = t.replaceAll("public|protected|private|abstract|final|volatile|native|strict|synchronized|transient", "");
				name = name.substring(name.lastIndexOf(" ")+1, name.length());
				returnType = returnType.substring(0, returnType.indexOf(name)).trim();
				modifiers = t.substring(0, t.indexOf(";"));
//				modifiers = modifiers.substring(0, modifiers.lastIndexOf(" "));
			} else {
				name = t.substring(0, t.indexOf("=")).trim();
				name = name.substring(name.lastIndexOf(" ")+1, name.length());
				returnType = t.replaceAll("public|protected|private|abstract|final|volatile|native|strict|synchronized|transient", "");
				returnType = returnType.substring(0, returnType.indexOf(name)).trim();
				modifiers = t.substring(0, t.indexOf("="));
//				modifiers = modifiers.substring(0, modifiers.lastIndexOf(" "));
			}
			String[] ta = modifiers.split("\\s");
			HashSet<Node> hsn = classNode.getAllChildrenOfType(NodeType.FIELD);
			hsn.addAll(classNode.getAllChildrenOfType(NodeType.METHOD));
			Node field = null;
			Node containingMethod = null;
			boolean contained = false;
			boolean containedByMethod = false;
			
			field = ptf.createField(name, null);
			field.setBeginningLine(beginningLine+1);
			field.setLength(1);
			field.setJavaType(returnType);
			
			for (String modifier : ta) {
				field.addModifier(modifier);
			}
			for (Node nth : hsn) {
				if (nth.getType().equals(NodeType.METHOD) && (nth.getBeginningLine() < field.getBeginningLine() && 
						(nth.getBeginningLine()+nth.getLength()) > (field.getBeginningLine()+field.getLength()))) {
					// if field lies between method borders
					contained = false;
					containedByMethod = true;
					containingMethod = nth;
					break;
				} else if (nth.getName().equals(field.getName()) && nth.getType().equals(NodeType.FIELD)) {
					contained = true;
					break;
				} 
			}
			if (!contained) {

				if (containedByMethod) {
					containingMethod.addChild(field);
					field.setParent(containingMethod);
					containedByMethod = false;
				} else {
					classNode.addChild(field);
					field.setParent(classNode);
					contained = false;
				}
			} 
		}

	}
	
	private int getLengthOfMember(String memberCode) {
		int openedCurlyBrackets = 1,
			closedCurlyBrackets = 0,
			lineCount = 0;

		String[] lines = memberCode.split("\\r?\\n");
		for (String line : lines) {
			if (line.equals("")) {
				continue;
			}
			if (!line.startsWith("-")) {
				lineCount++;
			}
			/*
			 * Checks if toCheck contains opening curly brackets. If so, openedCurlyBrackets
			 * gets incremented and toCheck is cut to the last match.
			 */	
			String toCheck = line;
				
			while (toCheck.contains("{")) {
				openedCurlyBrackets++;
				toCheck = toCheck.substring(toCheck.indexOf("{")+1);
			}
			toCheck = line;
			// same as with opening curly brackets for closing ones.
			while (toCheck.contains("}")) {
				closedCurlyBrackets++;
				toCheck = toCheck.substring(toCheck.indexOf("}")+1);
			}
			/*
			 * if curly bracket types are equal and thus a field has been found or a method declaration 
			 * is completed, add the actual line string to member and add member to the separated 
			 * members.
			 */
			if (openedCurlyBrackets == closedCurlyBrackets) {
				return lineCount;
			}
		}
		return -1;
	}
	
	private int getBeginningLineOfMember(String diff, String memberCode) {
		String[] lines = diff.split("\\n");
		for (int i = 0; i < lines.length; i++) {
//			if (lines[i].contains(memberCode) && lines[i].startsWith("-")) {
//				return -1;
//			}
			if (lines[i].contains(memberCode)) {
				return i + (lines[0].equals("") ? 0 : 1);
			}
		}
		return -1;
	}
}
