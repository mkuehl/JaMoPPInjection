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
		String tempDiff = diff;
//		String[] lines = diff.split("\\r?\\n");
		String packageRegex = "package\\s[a-zA-Z0-9_]+(\\.[a-zA-Z0-9_]+)*;";
		String classRegex = "(public|protected|private)?\\sclass\\s[a-zA-Z0-9_]+";
		String importRegex = "import\\s[a-zA-Z0-9_]+(\\.([a-zA-Z0-9_]+|\\*))+;";
		String methodRegex = "(public|protected|private)?\\s[a-zA-Z0-9_]+\\s[a-zA-Z0-9_]+\\s?\\(";
		String parameterRegex = "(\\s)*[a-zA-Z0-9_]+(\\s)*[a-zA-Z0-9_]+(\\s)*(,)?";
		String fieldRegex = "(public|protected|private)?\\s[a-zA-Z0-9_]+\\s[a-zA-Z0-9_]+\\s?(;|=)";
		
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
			String name = t.substring(t.indexOf("class ")+6);
			String modifiers = t.substring(0, t.indexOf("class"));
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
			String t = m.group();
			// -1 due to the linebreak of the last line.
			int beginningLine = ((tempDiff.substring(0, m.end())).split("\\n")).length-1;
			// -1 because first line is counted.
			int length = getLengthOfMember(tempDiff.substring(m.end()))-1;
			// methods might have two space characters. If so, extract the name from the second, if not, extract it from the first.
			String name = t.substring(t.indexOf(" ")+1, t.indexOf("("));
			String returnType = "";
			String[] ta = t.split("\\s");
			String parameterString;
			if (name.contains(" ")) {
				returnType = name.replaceAll("public|protected|private|abstract|final|volatile|native|strict|synchronized|transient", "");
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
				
				// find params, if existent
				parameterString = tempDiff.substring(m.end(), tempDiff.indexOf(")", m.start()));
				m2 = paramPattern.matcher(parameterString);
				while (m2.find()) {
					String parameterName = m2.group();
					String javaType = "";
					HashSet<Node> hsp = method.getAllChildrenOfType(NodeType.PARAMETER);
					javaType = parameterName.substring(0, parameterName.lastIndexOf(" "));
					parameterName = parameterName.substring(parameterName.indexOf(" ")+1);
					if (parameterName.endsWith(",")) {
						parameterName = parameterName.replace(",", "");
					}
					Node paramNode = ptf.createParameter(parameterName);
					paramNode.setJavaType(javaType);
					for (Node p : hsp) {
						if (p.getName().equals(paramNode.getName())) {
							contained = true;
							break;
						}
					}
					if (!contained) {
						method.addChild(paramNode);
						paramNode.setParent(method);
						contained = false;
					}
				}
			}
		}
		
		m = fieldPattern.matcher(tempDiff);
		while (m.find()) {
			String t = m.group();
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
				returnType = name.replaceAll("public|protected|private|abstract|final|volatile|native|strict|synchronized|transient", "");
				name = name.substring(name.lastIndexOf(" ")+1, name.length());
				returnType = returnType.substring(0, returnType.indexOf(name)).trim();
				modifiers = t.substring(0, t.indexOf(";"));
//				modifiers = modifiers.substring(0, modifiers.lastIndexOf(" "));
			} else {
				name = t.substring(0, t.indexOf("=")).trim();
				name = name.substring(name.lastIndexOf(" ")+1, name.length());
				modifiers = t.substring(0, t.indexOf("="));
//				modifiers = modifiers.substring(0, modifiers.lastIndexOf(" "));
			}
			String[] ta = modifiers.split("\\s");
			HashSet<Node> hsn = classNode.getAllChildrenOfType(NodeType.FIELD);
			Node field = null;
			boolean contained = false;
			
			field = ptf.createField(name, null);
			field.setBeginningLine(beginningLine);
			field.setLength(1);
			field.setJavaType(returnType);
			
			for (String modifier : ta) {
				field.addModifier(modifier);
			}
			for (Node nth : hsn) {
				if (nth.getName().equals(field.getName())) {
					contained = true;
					break;
				}
			}
			if (!contained) {
				classNode.addChild(field);
				field.setParent(classNode);
				contained = false;
			}
		}
		
		System.out.println("Project: " + root.getName());
		for (Node n : root.getAllChildren()) {
			System.out.println("  Package: " + n.getName());
			for (Node n2 : n.getAllChildren()) {
				System.out.println("    Class: " + n2.getName());
				for (Node n3 : n2.getAllChildrenOfType(NodeType.METHOD)) {
					System.out.println("      Method: " + n3.getName());
					System.out.println("\tReturn Type: " + n3.getJavaType());
					for (Node n4 : n3.getAllChildrenOfType(NodeType.PARAMETER)) {
						System.out.println("\tParameter: " + n4.getName() + " ParamType: " + n4.getJavaType());
					}
				}
				for (Node n3 : n2.getAllChildrenOfType(NodeType.FIELD)) {
					System.out.println("      Field: " + n3.getName());
					System.out.println("\tType: " + n3.getJavaType());
				}
			}
		}
	}
	
	private int getLengthOfMember(String memberCode) {
		int openedCurlyBrackets = 0,
			closedCurlyBrackets = 0,
			lineCount = 0;

		String[] lines = memberCode.split("\\r?\\n");
		for (String line : lines) {
			lineCount++;
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
}
