package projecttree;

import java.util.HashSet;

public class ProjectTreeSearcher {

	/**
	 * Searches the given ProjectTree for a match of the given line numbers and the numbers of all methods of 
	 * the specified class. Returns the name of the affected method. if the respective class does not have a 
	 * method at the specified position, "#none" is returned. If there is not even the package or the class, 
	 * the empty string is returned. In case there is a method at the specified position, the name gets extended 
	 * by #e for changes at the end of the method or #s for changes at the start of the method.
	 * @param root
	 * @param beginningLine
	 * @param length
	 * @param qualifiedClassName
	 * @return
	 */
	public String getModifiedMethodName(Node root, int beginningLine, int length, String qualifiedClassName) {
		String packageName = qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf(".")),
			   className = qualifiedClassName.substring(qualifiedClassName.lastIndexOf(".")+1);
		int numberOfCheckedMethods = 0;
		HashSet<Node> packages = root.getAllChildren();
		for (Node pack : packages) {
			if (pack.getName().equals(packageName)) {
				HashSet<Node> classes = pack.getAllChildren();
				for (Node c : classes) {
					if (c.getName().equals(className)) {
						if (beginningLine > c.getLength()) {
							return "";
						} 
						HashSet<Node> methods = c.getAllChildrenOfType(NodeType.METHOD);
						for (Node method : methods) {
							numberOfCheckedMethods++;
							int startline = method.getBeginningLine()+1,
								endline = method.getBeginningLine()+method.getLength();
							//TODO beginning line in project tree not set correctly
							// following standard formatting, the new lines begin either at the line following the signature or at the line preceding the last "}".
							if (endline == beginningLine) {
								method.setLength(method.getLength()+length);
								return method.getName() + "#e";
							} else if (startline == beginningLine) {
								method.setLength(method.getLength()+length);
								return method.getName() + "#s";
							} else if (numberOfCheckedMethods == methods.size()) {
								return "#none";
							}
						}
					}
				}
			}
		}
		return "";
	}
	
	/**
	 * Returns the Node of the method that is affected.
	 * @param root
	 * @param beginningLine
	 * @param length
	 * @param qualifiedClassName
	 * @return
	 */
	public Node getModifiedMethodNode(Node startingNode, int beginningLine, int length, String qualifiedClassName) {
		String packageName = qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf(".")),
			   className = qualifiedClassName.substring(qualifiedClassName.lastIndexOf(".")+1);
		int numberOfCheckedMethods = 0;
		HashSet<Node> packages = startingNode.getAllChildren();
		for (Node pack : packages) {
			if (pack.getName().equals(packageName)) {
				HashSet<Node> classes = pack.getAllChildren();
				for (Node c : classes) {
					if (c.getName().equals(className)) {
						HashSet<Node> methods = c.getAllChildrenOfType(NodeType.METHOD);
						for (Node method : methods) {
							numberOfCheckedMethods++;
							// +1, because beginning line is signature!
							int startline = method.getBeginningLine()+1,
								endline = method.getBeginningLine()+method.getLength();
							//TODO beginning line in project tree not set correctly
							// following standard formatting, the new lines begin either at the line following the signature or at the line preceding the last "}".
							if (endline == beginningLine || startline == beginningLine) {
								method.setLength(method.getLength()+length);
								return method;
							} else if (numberOfCheckedMethods == methods.size()) {
								return null;
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * If member is found, it is removed.
	 * @param root
	 * @param memberToRemove
	 * @param qualifiedClassName
	 */
	public void removeMember(Node root, Node memberToRemove, String qualifiedClassName) {
		String packageName = qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf(".")),
			   className = qualifiedClassName.substring(qualifiedClassName.lastIndexOf(".")+1);
		HashSet<Node> packages = root.getAllChildren();
		for (Node pack : packages) {
			if (pack.getName().equals(packageName)) {
				HashSet<Node> classes = pack.getAllChildren();
				for (Node c : classes) {
					if (c.getName().equals(className)) {
						HashSet<Node> methods = c.getAllChildrenOfType(NodeType.METHOD);
						for (Node method : methods) {
							if (method.equals(memberToRemove)) {
								c.getAllChildren().remove(memberToRemove);
							} 
						}
					}
				}
			}
		}
	}
	
}
