package webpiecesxxxxxpackage.db;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name="SIMPLE_STORAGE", 
	   uniqueConstraints=@UniqueConstraint(name="twoKeysConstraint", columnNames={"pluginKey", "mapKey"})
)
@NamedQueries({
	@NamedQuery(name = "findProperties", query = "select u from SimpleStorageDbo as u where u.pluginKey = :key"),
	@NamedQuery(name = "findProperty", query = "select u from SimpleStorageDbo as u where u.pluginKey = :key AND u.mapKey = :subKey")
})
public class SimpleStorageDbo {

	@Id
	@SequenceGenerator(name="simplestorage_id_gen",sequenceName="simplestorage_sequence" ,initialValue=1,allocationSize=10)
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator="simplestorage_id_gen")
	private Long id;
	
	@Column(length = 255, nullable=false)
	private String pluginKey;
	
	@Column(length = 255, nullable=false)
	private String mapKey;

	@Column(length = 2000)
	private String value;

	public SimpleStorageDbo() {
		super();
	}
	
	public SimpleStorageDbo(String pluginKey, String mapKey, String value) {
		super();
		this.pluginKey = pluginKey;
		this.mapKey = mapKey;
		this.value = value;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPluginKey() {
		return pluginKey;
	}

	public void setPluginKey(String pluginKey) {
		this.pluginKey = pluginKey;
	}

	public String getMapKey() {
		return mapKey;
	}

	public void setMapKey(String mapKey) {
		this.mapKey = mapKey;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@SuppressWarnings("unchecked")
	public static List<SimpleStorageDbo> findAll(EntityManager mgr, String key) {
		Query query = mgr.createNamedQuery("findProperties");
		query.setParameter("key", key);
		return query.getResultList();
	}
	
	public static SimpleStorageDbo find(EntityManager mgr, String key, String subKey) {
		Query query = mgr.createNamedQuery("findProperty");
		query.setParameter("key", key);
		query.setParameter("subKey", subKey);
		try {
			return (SimpleStorageDbo) query.getSingleResult();
		} catch(NoResultException e) {
			return null;
		}
	}
}
