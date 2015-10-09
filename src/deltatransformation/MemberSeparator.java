package deltatransformation;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import preprocessing.diffpreprocessor.ModificationType;

public class MemberSeparator {

	// separates all members within the given String. A member is a Field or a Method.
	public LinkedList<String> separateMembers(String allMembers, ModificationType typeOfChange) {
		LinkedList<String> separatedMembers = new LinkedList<String>();
		// Gets filled with the members signature and body. For methods, this takes several iterations.
		String member = "";
		boolean isMember = false;

		// matches packages.
		String packageRegex = "package\\s[a-zA-Z0-9_]+(\\.[a-zA-Z0-9_]+)*;";
		Pattern packagePattern = Pattern.compile(packageRegex);
		Matcher packageMatcher;
		// matches imports, like packages but must consider qualified names and the possible asterix.
		String importRegex = "import\\s[a-zA-Z0-9_]+(\\.([a-zA-Z0-9_]+|\\*))+;";
		Pattern importPattern = Pattern.compile(importRegex);
		Matcher importMatcher;
//		// matches superclasses, like packages but without the semicolon.
//		String superclassRegex = "extends\\s[a-zA-Z0-9_]+(\\.[a-zA-Z0-9_]+)*";
//		Pattern superclassPattern = Pattern.compile(superclassRegex);
//		Matcher superclassMatcher;
//		// matches interfaces, like packages but without the semicolon.
//		String interfaceRegex = "implements\\s[a-zA-Z0-9_]+(\\.[a-zA-Z0-9_]+)*([\\s]*,[\\s]*[a-zA-Z0-9_]+(\\.[a-zA-Z0-9_]+)+)*";
//		Pattern interfacePattern = Pattern.compile(interfaceRegex);
//		Matcher interfaceMatcher;
		// matches methods, optional modifier, return type, name, starting bracket for param list.
		String memberRegex = "(public|protected|private)?\\s[a-zA-Z0-9_]+\\s[a-zA-Z0-9_]+\\s?\\(?";
		Pattern memberPattern = Pattern.compile(memberRegex);
		Matcher memberMatcher;
		//			String[] sa = input.split("@@");
		// to ensure, that the member is completed, count opening and closing curly brackets.
		int openedCurlyBrackets = 0,
			closedCurlyBrackets = 0;
		
		if (typeOfChange.equals(ModificationType.ADDSINTERFACE) || 
				typeOfChange.equals(ModificationType.ADDSSUPERCLASS)) {
			separatedMembers.add(allMembers);
			return separatedMembers;
		}
		if (typeOfChange.equals(ModificationType.REMOVESINTERFACE) || 
				typeOfChange.equals(ModificationType.REMOVESSUPERCLASS)) {
			separatedMembers.add(allMembers);
			return separatedMembers;
		}
		
		// split given String by line endings.
		String[] lines = allMembers.split("\\r?\\n");
		for (String line : lines) {
			packageMatcher = packagePattern.matcher(line);
			importMatcher = importPattern.matcher(line);
//			superclassMatcher = superclassPattern.matcher(line);
//			interfaceMatcher = interfacePattern.matcher(line);
			memberMatcher = memberPattern.matcher(line);
			
			if (packageMatcher.find()) {
				separatedMembers.add(line);
				continue;
			}
			
			if (importMatcher.find()) {
				separatedMembers.add(line);
				continue;
			}
//			
//			if (interfaceMatcher.find()) {
//				separatedMembers.add(interfaceMatcher.group());
//				continue;
//			}
//			
//			if (superclassMatcher.find()) {
//				separatedMembers.add(superclassMatcher.group());
//				continue;
//			}
			/*
			 * if memberMatcher finds a member and this member is not inside a member (only 
			 * applies for methods), which is not allowed unless with not yet supported 
			 * anonymous classes for example usually used for adding listeners. 
			 */
			if (memberMatcher.find() && !isMember) {
				openedCurlyBrackets = 0;
				closedCurlyBrackets = 0;
				isMember = true;
			}

			// next line after method signature.
			if (isMember) {
				String toCheck = line;
				/*
				 * Checks if toCheck contains opening curly brackets. If so, openedCurlyBrackets
				 * gets incremented and toCheck is cut to the last match.
				 */
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
				member += line + "\n";
			}

			/*
			 * if curly bracket types are equal and thus a field has been found or a method declaration 
			 * is completed, add the actual line string to member and add member to the separated 
			 * members.
			 */
			if (openedCurlyBrackets == closedCurlyBrackets && 
					member != "" && member != "\t" && member != "\n" && member != "\r") {
				separatedMembers.add(member);
				member = "";
				isMember = false;
			}
		}
		return separatedMembers;
	}
}
