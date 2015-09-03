package structureEntities;

public class ParameterEntity {

	private String type;
	private String name;
	
	public ParameterEntity() {
		type = "";
		name = "";
	}
	
	public ParameterEntity(String p_type, String p_name) {
		type = p_type;
		name = p_name;
	}

	public void setType(String p_type) {
		type = p_type;
	}
	
	public String getType() {
		return type;
	}
	
	public void setName(String p_name) {
		name = p_name;
	}
	
	public String getName() {
		return name;
	}
}
