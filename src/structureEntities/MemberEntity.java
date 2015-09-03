package structureEntities;

import java.util.LinkedList;

public abstract class MemberEntity {

	String name = "";
	LinkedList<String> modifiers = new LinkedList<String>();
	boolean isImport = true;
	
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the modifier at position i. If i is smaller 0, greater or equal the modifier
	 * list size, the modifier list is null or has no items, null is returned.
	 * @param i
	 * @return
	 */
	public String getModifier(int i) {
		if (modifiers == null || modifiers.size() == 0 || i < 0 || i >= modifiers.size()) {
			return null;
		}
		return modifiers.get(i);
	}
	
	public boolean getIsImport() {
		return isImport;
	}
}
