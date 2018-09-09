package code.gen.tools.xlsgen.entity;

/**
 * 
 * @author wind
 * @date 2018年7月17日
 **/
public class FieldType{
	
	private String dataType;
	private String fieldName;
	private String fieldDesc;
	private Object defaultValue;
	private String fieldMethodName;
	
	public String getFieldMethodName() {
		return fieldMethodName;
	}
	public void setFieldMethodName(String fieldMethodName) {
		this.fieldMethodName = fieldMethodName;
	}
	public Object getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String getFieldDesc() {
		return fieldDesc;
	}
	public void setFieldDesc(String fieldDesc) {
		this.fieldDesc = fieldDesc;
	}
}