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
	
	public void setLength(int p_length) {
		length = p_length;
	}
	
	public int getLength() {
		return length;
	}
	
	public void setBeginningLine(int p_beginningLine) {
		length = p_beginningLine;
	}
	
	public int getBeginningLine() {
		return beginningLine;
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
		children.add(child);
	}
	
	/**
	 * If child not contained, nothing is done.
	 * @param child
	 */
	public void removeChild(Node child) {
		if (children.contains(child)) {
			children.remove(child);
		}
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
}
