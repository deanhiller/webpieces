package WEBPIECESxPACKAGE.base.crud;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "COMPANIES")
public class CompanyDbo {
	@Id
	@SequenceGenerator(name="company_id_gen" ,sequenceName="company_sequence",initialValue=1,allocationSize=10)
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator="company_id_gen")
	private Integer id;

	@OneToMany(mappedBy="company")
	private List<UserDbo> users = new ArrayList<UserDbo>();

	private String name;

	private String description;

	private String beginDayOfWeek;

	private String getEmailYesOrNo;
	
	public CompanyDbo() {
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<UserDbo> getUsers() {
		return users;
	}

	public void setUsers(List<UserDbo> users) {
		this.users = users;
	}

	public void addUser(UserDbo userDbo) {
		this.users.add(userDbo);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getBeginDayOfWeek() {
		return beginDayOfWeek;
	}

	public void setBeginDayOfWeek(String beginDayOfWeek) {
		this.beginDayOfWeek = beginDayOfWeek;
	}

	public String getGetEmailYesOrNo() {
		return getEmailYesOrNo;
	}

	public void setGetEmailYesOrNo(String getEmailYesOrNo) {
		this.getEmailYesOrNo = getEmailYesOrNo;
	}

}
