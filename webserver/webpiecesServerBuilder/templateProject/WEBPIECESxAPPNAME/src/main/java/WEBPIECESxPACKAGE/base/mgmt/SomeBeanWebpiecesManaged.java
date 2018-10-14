package WEBPIECESxPACKAGE.base.mgmt;

import WEBPIECESxPACKAGE.base.libs.EducationEnum;

public interface SomeBeanWebpiecesManaged {

	public String getCategory();
	
	public void setCount(int count);
	
	public int getCount();
	
	//This demonstrates we read since no converter was found
	public Class<?> getPropertyNoConverter();
	public void setPropertyNoConverter(Class<?> clazz);
	
	//This demonstrates use of a converter
	public EducationEnum getEducationLevel();
	public void setEducationLevel(EducationEnum educationLevel);
	
}
