package code.gen.tools.xlsgen.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 类的描述
 * @author wind
 * @date 2018年7月16日
 **/
public class ClassContext {
	private String packageName;
	private String className;
	private List<FieldType> fieldTypes = new ArrayList<>();
	private String fileName;
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getClassName() {
		return className;
	}
	
	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public void setClassName(String className) {
		this.className = className;
	}
	public List<FieldType> getFieldTypes() {
		return fieldTypes;
	}
	public void setFieldTypes(List<FieldType> fieldTypes) {
		this.fieldTypes = fieldTypes;
	}
}

