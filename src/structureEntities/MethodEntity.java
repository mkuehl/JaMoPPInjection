package structureEntities;

import java.util.LinkedList;

/**
 * Representation of methods within lookup tree.
 * @author Max
 *
 */
public class MethodEntity extends MemberEntity {

	private String returnType;
	private  LinkedList<ParameterEntity> params;
	
	public MethodEntity() {
		returnType = "";
		params = new LinkedList<ParameterEntity>();
	}
	
	public void setReturnType(String p_returnType) {
		returnType = p_returnType;
	}
	
	public String getReturnType() {
		return returnType;
	}
	
	public void addParameter(ParameterEntity p_param) {
		params.add(p_param);
	}
	
	/**
	 * Returns ParameterEntity at position index in parameter list. 
	 * If index is smaller 0, greater or equal the list size or the
	 * list has no items, null is returned. 
	 * @param index
	 * @return
	 */
	public ParameterEntity getParameter(int index) {
		if (params == null || params.size() == 0 || index < 0 || index >= params.size()) {
			return null;
		}
		return params.get(index);
	}
}
