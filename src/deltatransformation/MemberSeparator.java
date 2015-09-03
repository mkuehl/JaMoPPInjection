package deltatransformation;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MemberSeparator {

	// separates all members within the given String. A member is a Field or a Method.
	public LinkedList<String> separateMembers(String allMembers) {
		LinkedList<String> separatedMembers = new LinkedList<String>();
		String member = "";
		boolean isMember = false;
		// matches methods
		String memberRegex = "(public|protected|private)?\\s[a-zA-Z0-9_]+\\s[a-zA-Z0-9_]+\\s?\\(?";
		Pattern memberPattern = Pattern.compile(memberRegex);
		Matcher memberMatcher;
		//			String[] sa = input.split("@@");
		// to ensure, that the member is completed, count opening and closing curly brackets.
		int openedCurlyBrackets = 0,
			closedCurlyBrackets = 0;
		// split given String by line endings.
		String[] lines = allMembers.split("\\r?\\n");
		for (String line : lines) {
			memberMatcher = memberPattern.matcher(line);
			/*
			 * if methodMatcher finds a method and this method is not inside a method, which is
			 * not allowed unless with not yet supported anonymous classes for example usually
			 * used for adding listeners. 
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
