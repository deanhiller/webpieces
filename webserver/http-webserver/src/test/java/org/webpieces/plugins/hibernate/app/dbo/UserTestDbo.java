package org.webpieces.plugins.hibernate.app.dbo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="USERS", 
       indexes={
		  @Index(name="email", columnList="email", unique=true)
       }
)
@NamedQueries({
	@NamedQuery(name = "findAllUsers", query = "select u from UserTestDbo as u"),
	@NamedQuery(name = "findByEmailId", query = "select u from UserTestDbo as u where u.email=:email"),
	@NamedQuery(name = "findByIdWithRoleJoin", query = "select u from UserTestDbo as u left join fetch u.roles as r where u.id = :id")
})
public class UserTestDbo {

	@Id
	@SequenceGenerator(name="user_id_gen",sequenceName="user_sequence" ,initialValue=1,allocationSize=10)
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator="user_id_gen")
	private Integer id;

	@Column(unique = true)
	private String email;

	private String phone;
	private String password;
	private String name;
	private String firstName;
	private String lastName;

	@ManyToOne(fetch=FetchType.LAZY)
	private UserTestDbo manager;
	
	@OneToMany(mappedBy = "manager")
	private List<UserTestDbo> employees = new ArrayList<UserTestDbo>();

	private boolean isNewPasswordChange;

	//@Convert( converter = LevelEducationConverter.class )
	private LevelEducation levelOfEducation = null;
	
	@OneToMany(mappedBy = "user")
	private List<UserRoleDbo> roles = new ArrayList<UserRoleDbo>();

	public boolean isNewPasswordChange() {
		return isNewPasswordChange;
	}

	public void setNewPasswordChange(boolean isNewPasswordChange) {
		this.isNewPasswordChange = isNewPasswordChange;
	}

	public Integer getId() {
		return id;
	}

	public List<UserTestDbo> getEmployees() {
		return employees;
	}

	public void setEmployees(List<UserTestDbo> employees) {
		this.employees = employees;
	}

	public void addEmployee(UserTestDbo employee) {
		this.employees.add(employee);
	}

	public void deleteEmployee(UserTestDbo employee) {
		this.employees.remove(employee);
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public UserTestDbo getManager() {
		return manager;
	}

	public void setManager(UserTestDbo manager) {
		this.manager = manager;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public LevelEducation getLevelOfEducation() {
		return levelOfEducation;
	}

	public void setLevelOfEducation(LevelEducation levelOfEducation) {
		this.levelOfEducation = levelOfEducation;
	}

	public List<UserRoleDbo> getRoles() {
		return roles;
	}

	public void setRoles(List<UserRoleDbo> roles) {
		this.roles = roles;
	}
	
	@SuppressWarnings("unchecked")
	public static List<UserTestDbo> findAllField(EntityManager mgr) {
		Query query = mgr.createNamedQuery("findAll");
		return query.getResultList();
	}

	public static UserTestDbo findByEmailId(EntityManager mgr, String email) {
		Query query = mgr.createNamedQuery("findByEmailId");
		query.setParameter("email", email);
		try {
			return (UserTestDbo) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
	
	public static UserTestDbo findWithJoin(EntityManager mgr, int id) {
		Query query = mgr.createNamedQuery("findByIdWithRoleJoin");
		query.setParameter("id", id);
		try {
			return (UserTestDbo) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
}
