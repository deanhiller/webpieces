package WEBPIECESxPACKAGE.base.mgmt;

import WEBPIECESxPACKAGE.base.libs.EducationEnum;

public class SomeBean implements SomeBeanWebpiecesManaged {

	private int count;
	private Class<?> clazz = SomeBean.class;
	private EducationEnum educationLevel;
	
	@Override
	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public int getCount() {
		return count;
	}

	@Override
	public String getCategory() {
		return "General";
	}

	@Override
	public Class<?> getPropertyNoConverter() {
		return clazz;
	}

	@Override
	public void setPropertyNoConverter(Class<?> clazz) {
		this.clazz = clazz;
	}

	@Override
	public EducationEnum getEducationLevel() {
		return educationLevel;
	}

	@Override
	public void setEducationLevel(EducationEnum educationLevel) {
		this.educationLevel = educationLevel;
	}

}
