package projecttree;

import java.util.HashSet;

public class Node {

	private String name,
				   javaType;
	private int beginningLine,
				length;
	private NodeType type;
	private HashSet<String> modifiers;
	private Node parent;
	private HashSet<Node> children;
	private HashSet<Node> parameters;
	
	public Node(String p_name, NodeType p_type) {
		name = p_name;
		type = p_type;
		beginningLine = -1;
		length = -1;
		modifiers = new HashSet<String>();
		children = new HashSet<Node>();
		parameters = new HashSet<Node>();
	}
	
	public void setName(String p_name) {
		name = p_name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setJavaType(String p_javaType) {
		javaType = p_javaType;
	}
	
	public String getJavaType() {
		return javaType;
	}
	
	public void setBeginningLine(int p_beginningLine) {
		beginningLine = p_beginningLine;
	}
	
	public int getBeginningLine() {
		return beginningLine;
	}
	
	public void setLength(int p_length) {
		// if length not set and changed, update subsequent members.
		if (length > -1 && p_length != length) {
			boolean linesRemoved = p_length < length;
			for (Node child : parent.getAllChildren()) {
				if (child.getType().equals(NodeType.METHOD) || child.getType().equals(NodeType.FIELD)) {
					updateLinesOfSubsequentMembers(parent, child, linesRemoved);
				}
			}
		} 
		length = p_length;
	}
	
	public int getLength() {
		return length;
	}
	
	public void setType(NodeType p_type) {
		type = p_type;
	}
	
	public NodeType getType() {
		return type;
	}
	
	/**
	 * Adds a new modifier to the Node. Does nothing if modifier is not a valid Java modifier.
	 * @param p_modifier
	 */
	public void addModifier(String p_modifier) {
		if (p_modifier.matches("public|protected|private|abstract|final|volatile|native|strict|synchronized|transient")) {
			modifiers.add(p_modifier);
		}
	}
	
	/**
	 * Gets modifier at position "number". If not existent, the empty String is returned.
	 * @param number
	 * @return modifier at position number
	 */
	public String getModifier(int number) {
		String[] a = new String[5];
		a = modifiers.toArray(new String[5]);
		return (a[number] == null || a[number].equals("")) ? "" : a[number];
	}
	
	public void setAllModifiers(HashSet<String> p_modifiers) {
		modifiers = p_modifiers;
	}
	
	public HashSet<String> getAllModifiers() {
		return modifiers;
	}
	
	public void setParent(Node p_parent) {
		parent = p_parent;
	}
	
	public Node getParent() {
		return parent;
	}
	
	public void addChild(Node child) {
		if (isContained(child)) {
			return;
		}
		if (child.getType().equals(NodeType.METHOD) || child.getType().equals(NodeType.FIELD)) {
//			updateLinesOfSubsequentMembers(this, child, true);
		}
		children.add(child);
	}
	
	/**
	 * If child not contained, nothing is done.
	 * @param child
	 */
	public void removeChild(Node child) {
		if (children.contains(child)) {
			children.remove(child);
			if (child.getType().equals(NodeType.METHOD) || child.getType().equals(NodeType.FIELD)) {
				updateLinesOfSubsequentMembers(this, child, false);
			}
		}
	}
	
	public Node getChild(String name) {
		for (Node n : getAllChildren()) {
			if (n.getName().equals(name)) {
				return n;
			}
		}
		return null;
	}
	
	public HashSet<Node> getAllChildren() {
		return children;
	}
	
	/**
	 * If Node represents a Method, parameters can be added.
	 * @param parameter
	 */
	public void addParameter(Node parameter) {
		if (type.equals(NodeType.METHOD) && parameter.getType().equals(NodeType.PARAMETER)) {
			parameters.add(parameter);
		}
	}
	
	public void removeParameter(Node parameter) {
		if (parameters.contains(parameter)) {
			parameters.remove(parameter);
		}
	}
	
	public HashSet<Node> getAllParameters() {
		return parameters;
	}
	
	public HashSet<Node> getAllChildrenOfType(NodeType p_type) {
		HashSet<Node> childrenOfType = new HashSet<Node>();
		for (Node c : children) {
			if (c.getType().equals(p_type)) {
				childrenOfType.add(c);
				continue;
			}
		}
		return childrenOfType;
	}
	
	public boolean isContained(Node node) {
		HashSet<Node> members = getAllChildren();
		for (Node n : members) {
			if (n.equals(node)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @param containingClass
	 * @param affectedMember
	 * @param removeOrAdd - false = remove, true = add
	 */
	private void updateLinesOfSubsequentMembers(Node containingClass, Node affectedMember, boolean removeOrAdd) {
		int affectedLines = (removeOrAdd ? 1 : -1) * affectedMember.getLength()+1;
		HashSet<Node> members = containingClass.getAllChildrenOfType(NodeType.METHOD);
		members.addAll(containingClass.getAllChildrenOfType(NodeType.FIELD));
		for (Node member : members) {
			if (member.getBeginningLine() > affectedMember.getBeginningLine()) {
				member.setBeginningLine(member.getBeginningLine() + affectedLines);
			} 
		}
	}
}
