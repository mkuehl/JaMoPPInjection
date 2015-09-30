package projecttree;

import java.util.HashSet;

public class ProjectTreeFactory {

	
	public Node createProject(String name) {
		Node n = new Node(name, NodeType.PROJECT);
		return n;
	}
	
	public Node createPackage(String name) {
		Node n = new Node(name,  NodeType.PACKAGE);
		return n;
	}
	
	public Node createClass(String name, HashSet<String> modifiers) {
		Node n = new Node(name,  NodeType.CLASS);
		if (modifiers != null) {
			n.setAllModifiers(modifiers);
		}
		return n;
	}
	
	public Node createInterface(String name, HashSet<String> modifiers) {
		Node n = new Node(name,  NodeType.INTERFACE);
		if (modifiers != null) {
			n.setAllModifiers(modifiers);
		}
		return n;
	}
		
	public Node createImport(String name) {
		Node n = new Node(name,  NodeType.IMPORT);
		return n;
	}
	
	public Node createSuperClass(String name) {
		Node n = new Node(name,  NodeType.SUPERCLASS);
		return n;
	}
	
	/**
	 * Creates an implements-Node. Each implemented interface of a class must have its own Node.
	 * @param name
	 * @return
	 */
	public Node createImplements(String name) {
		Node n = new Node(name,  NodeType.IMPLEMENTS);
		return n;
	}
	
	public Node createMethod(String name, HashSet<String> modifiers) {
		Node n = new Node(name,  NodeType.METHOD);
		if (modifiers != null) {
			n.setAllModifiers(modifiers);
		}
		return n;
	}
	
	public Node createParameter(String name) {
		Node n = new Node(name,  NodeType.PARAMETER);
		return n;
	}
	
	public Node createField(String name, HashSet<String> modifiers) {
		Node n = new Node(name,  NodeType.FIELD);
		if (modifiers != null) {
			n.setAllModifiers(modifiers);
		}
		return n;
	}
}
