package org.webpieces.plugins.hibernate.app.dbo;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

@Entity
public class UserRoleDbo {

	@Id
	@SequenceGenerator(name="roleuser_id_gen",sequenceName="roleuser_sequence" ,initialValue=1,allocationSize=10)
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator="roleuser_id_gen")
	private Integer id;

	@ManyToOne(fetch=FetchType.LAZY)
	private UserTestDbo user;

	private Role role;

	public UserRoleDbo() {
	}
	
	public UserRoleDbo(UserTestDbo user, Role r) {
		this.user = user;
		this.role = r;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public UserTestDbo getUser() {
		return user;
	}

	public void setUser(UserTestDbo user) {
		this.user = user;
	}
	
}
