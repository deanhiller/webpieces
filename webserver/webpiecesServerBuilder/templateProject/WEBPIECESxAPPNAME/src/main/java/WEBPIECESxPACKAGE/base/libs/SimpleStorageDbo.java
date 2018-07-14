package WEBPIECESxPACKAGE.base.libs;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Query;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name="SIMPLE_STORAGE", 
	   uniqueConstraints=@UniqueConstraint(columnNames={"pluginKey", "mapKey"})
)
@NamedQueries({
	@NamedQuery(name = "findProperties", query = "select u from SimpleStorageDbo as u where u.pluginKey = :key"),
})
public class SimpleStorageDbo {

	@Id
	@SequenceGenerator(name="simplestorage_id_gen",sequenceName="simplestorage_sequence" ,initialValue=1,allocationSize=10)
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator="simplestorage_id_gen")
	private String id;
	
	@Column(length = 255)
	private String pluginKey;
	
	@Column(length = 255)
	private String mapKey;

	@Column(length = 2000)
	private String value;

	public SimpleStorageDbo(String pluginKey, String mapKey, String value) {
		super();
		this.pluginKey = pluginKey;
		this.mapKey = mapKey;
		this.value = value;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
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
}
