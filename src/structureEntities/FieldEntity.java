package structureEntities;

/**
 * Representation if fields within lookup tree.
 * @author Max
 *
 */
public class FieldEntity extends MemberEntity {

	private String type;
	
	public FieldEntity() {
		type = "";
	}
	
	public FieldEntity(String p_type) {
		type = p_type;
	}
	
	public void setType(String p_type) {
		type = p_type;
	}
	
	public String getType() {
		return type;
	}
}
