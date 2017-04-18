package WEBPIECESxPACKAGE.base.libs;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

@Entity
public class UserRole {

	@Id
	@SequenceGenerator(name="roleuser_id_gen",sequenceName="roleuser_sequence" ,initialValue=1,allocationSize=10)
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator="roleuser_id_gen")
	private Integer id;

	@ManyToOne(fetch=FetchType.LAZY)
	private UserDbo user;

	private RoleEnum role;

	public UserRole() {
	}
	
	public UserRole(UserDbo user, RoleEnum r) {
		this.user = user;
		this.user.addRole(this);
		this.role = r;
	}

	public RoleEnum getRole() {
		return role;
	}

	public void setRole(RoleEnum role) {
		this.role = role;
	}

	public UserDbo getUser() {
		return user;
	}

	public void setUser(UserDbo user) {
		this.user = user;
	}
	
}
