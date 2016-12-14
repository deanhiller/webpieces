package org.webpieces.plugins.hibernate.app.dbo;

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
public class CompanyTestDbo {
	@Id
	@SequenceGenerator(name="company_id_gen" ,sequenceName="company_sequence",initialValue=1,allocationSize=10)
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator="company_id_gen")
	private Integer id;

	@OneToMany(mappedBy="company")
	private List<UserTestDbo> users = new ArrayList<UserTestDbo>();

	private String name;

	private String description;

	private String beginDayOfWeek;

	private String getEmailYesOrNo;
	
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

	public List<UserTestDbo> getUsers() {
		return users;
	}

	public void setUsers(List<UserTestDbo> users) {
		this.users = users;
	}

	public void addUser(UserTestDbo userDbo) {
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
