package deltatransformation;

import java.util.LinkedList;

import org.deltaj.deltaJ.AddsClassBodyMemberDeclaration;
import org.deltaj.deltaJ.AddsEnumConstant;
import org.deltaj.deltaJ.AddsImport;
import org.deltaj.deltaJ.AddsInterfacesList;
import org.deltaj.deltaJ.AddsMember;
import org.deltaj.deltaJ.AddsMemberDeclaration;
import org.deltaj.deltaJ.AddsSuperclass;
import org.deltaj.deltaJ.ModifiesSuperclass;
import org.deltaj.deltaJ.RemovesField;
import org.deltaj.deltaJ.RemovesImport;
import org.deltaj.deltaJ.RemovesInterfacesList;
import org.deltaj.deltaJ.RemovesMethod;
import org.deltaj.deltaJ.RemovesSuperclass;
import org.eclipse.emf.ecore.EObject;

import preprocessing.diffpreprocessor.ModificationType;
import preprocessing.diffs.Change;
import projecttree.Node;
import projecttree.NodeType;

public class DeltaActionCreator {


	/**
	 * Adds multiple members to a delta. allChangesAsText is the String containing all changes of that shall be added to the delta.
	 * @param allChangesAsText
	 * @param ma
	 * @param c
	 * @return String of changes correctly separated and succeeding an "adds".
	 */
	public String createDeltaActionsForManyMembers(LinkedList<String> members, EObject ma, 
			Change c) {
		StringBuilder affectedMembers = new StringBuilder();
		int openingBrackets = 0,
			closingBrackets = 0;

		for (String s : members) {
			affectedMembers.append(typeOfChange(ma));
			if (ma instanceof RemovesMethod || ma instanceof RemovesField || 
					ma instanceof RemovesSuperclass) {
				// if superclass is not removed, further instructions are needed.
				if (!affectedMembers.toString().contains("removes superclass")) {
					String t = s.replaceAll("(public|protected|private|static|final)", "");
					//					// return type (can be qualified), method name
					//					String regex = "(\\w|\\d|_)+\\.?(\\w|\\d|_|\\.){0,50}(\\w|\\d|_)+[\\s]+(\\w|_|.){1,50}(\\w|_)[\\s]+"
					//							// a (, arbitrary number of parameters and last a )
					//							+ "\\(((\\w|_|.){1,50}(\\w|_),)*(\\w|_|.){1,50}(\\w|_)\\)";
					String[] methodParts = t.split("\\s|,");

					affectedMembers.append(" " + methodParts[3]);
					// iterates through parameters, adding only datatypes and stopping by hitting "{"
					for (int i = 4; i < methodParts.length; i++) {
						if (methodParts[i].contains("{")) {
							break;
						}
						if (methodParts[i].contains(",") || methodParts[i].endsWith(")") || 
								methodParts[i].equals("")) {
							continue;
						} 
						affectedMembers.append(methodParts[i] + " ");
					}
					if (!affectedMembers.toString().endsWith(";")) {
						openingBrackets += countNumberOfOccurencesInString(affectedMembers.toString(), "(");
						closingBrackets += countNumberOfOccurencesInString(affectedMembers.toString(), ")");
						while (closingBrackets < openingBrackets) {
							affectedMembers.append(")");
							closingBrackets++;
						}
					}
					if (!affectedMembers.toString().endsWith(";")) {
						affectedMembers.append(";\n");
					} else {
						affectedMembers.append("\n");
					}
				} else {
//					affectedMembers.append(" removes superclass;\n");
				}
			} else {
				if (c.getTypeOfChange().equals(ModificationType.MODIFIESMETHOD)) {
					affectedMembers.append(" " + c.getModifiedMethod().getName() + "(");
					String params = "",
						   paramsWithType = "";
					// params have to be named, with type for declaration, only name for calls.
					for (Node p : c.getModifiedMethod().getAllChildrenOfType(NodeType.PARAMETER)) {
						params += p.getName() + ",";
						paramsWithType += p.getJavaType() + " " + p.getName() + ",";
					}
					if (params.endsWith(",")) {
						params = params.substring(0, params.length()-1);
					}
					if (paramsWithType.endsWith(",")) {
						paramsWithType = paramsWithType.substring(0, paramsWithType.length()-1);
					}
					affectedMembers.append(paramsWithType + ") {\n" + 
							(!c.getIsMethodModifiedAtStart() ? ("original(" + params +
									");\n") : ""));
					affectedMembers.append(s.trim() + (s.trim().endsWith(";") ? "" : ";") + "\n");
					affectedMembers.append(c.getIsMethodModifiedAtStart() ? "original(" + 
							params + ");\n" : "");
				} else {
					affectedMembers.append(" " + s.trim() + "");
				}
				// TODO better with ModificationType enum !!!!!!!!!!!!!!!!!!!!!
				if (ma instanceof AddsInterfacesList || ma instanceof AddsSuperclass ||
						ma instanceof RemovesInterfacesList || ma instanceof RemovesSuperclass ||
						ma instanceof ModifiesSuperclass) {
					if (affectedMembers.toString().endsWith(",")) {
						affectedMembers.deleteCharAt(affectedMembers.length()-1);
					}
					if (!affectedMembers.toString().endsWith(";")) {
						affectedMembers.append(";");
					}
				}
				openingBrackets += countNumberOfOccurencesInString(affectedMembers.toString(), "{");
				closingBrackets += countNumberOfOccurencesInString(affectedMembers.toString(), "}");
				while (closingBrackets < openingBrackets) {
					affectedMembers.append("}");
					closingBrackets++;
				}
				affectedMembers.append("\n");
			}
		}
		return affectedMembers.toString();
	}

	/**
	 * Checks type of change and returns either "adds" or "removes" or "removes superclass" 
	 * or "modifies superclass" or "modifies". As for "removes superclass" no further 
	 * instructions are needed!
	 * @param ma
	 * @return
	 */
	private String typeOfChange(EObject ma) {
		if (ma instanceof RemovesField || ma instanceof RemovesImport ||
				ma instanceof RemovesInterfacesList || ma instanceof RemovesMethod) {
			return "removes";
		} else if (ma instanceof RemovesSuperclass) {
			return "removes superclass;";
		} else if (ma instanceof AddsClassBodyMemberDeclaration || ma instanceof AddsImport ||
		
				ma instanceof AddsInterfacesList || ma instanceof AddsMember ||
				ma instanceof AddsMemberDeclaration || ma instanceof AddsEnumConstant ||
				ma instanceof AddsSuperclass) {
			return "adds";
		} else if (ma instanceof ModifiesSuperclass) {
			return "modifies superclass";
		} else {
			return "modifies";
		}

	}
	
	private int countNumberOfOccurencesInString(String line, String searchingFor) {
		String tempLine = line;
		int indexOfLastOccurence = 0,
			occurenceCount = 0;
		while (indexOfLastOccurence != -1) {
			indexOfLastOccurence = tempLine.indexOf(searchingFor);
			if (indexOfLastOccurence == -1) {
				break;
			}
			tempLine = tempLine.substring(indexOfLastOccurence+searchingFor.length());
			occurenceCount++;
		}
		return occurenceCount;
	}
}
