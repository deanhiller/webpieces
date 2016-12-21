package org.webpieces.plugins.hibernate;

import java.util.function.Function;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

import org.hibernate.metamodel.internal.EntityTypeImpl;
import org.webpieces.router.api.EntityLookup;
import org.webpieces.router.impl.params.ObjectTranslator;
import org.webpieces.router.impl.params.ParamNode;
import org.webpieces.router.impl.params.ParamTreeNode;
import org.webpieces.router.impl.params.ValueNode;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class HibernateLookup implements EntityLookup {

	private static final Logger log = LoggerFactory.getLogger(HibernateLookup.class);
	
	@Override
	public <T> boolean isManaged(Class<T> paramTypeToCreate) {
		EntityManager entityManager = Em.get();
		try {
			ManagedType<T> managedType = entityManager.getMetamodel().managedType(paramTypeToCreate);
			EntityTypeImpl<T> entityType = (EntityTypeImpl<T>) managedType;
			if(!entityType.hasSingleIdAttribute()) {
				log.warn("You generally should be using beans with hibernate ids since this is a hibernate class");
				return false; //if no single id attribute, let the default creator create the bean
			}
			
		} catch(IllegalArgumentException e) {
			return false;
		}
		return true;
	}

	@Override
	public <T> T find(Class<T> paramTypeToCreate, ParamTreeNode tree, 
			ObjectTranslator translator, Function<Class<T>, T> beanCreate) {
		EntityManager entityManager = Em.get();
		Metamodel metamodel = entityManager.getMetamodel();
		ManagedType<T> managedType = metamodel.managedType(paramTypeToCreate);
		EntityTypeImpl<T> entityType = (EntityTypeImpl<T>) managedType;
		Class<?> idClazz = entityType.getIdType().getJavaType();
		SingularAttribute<? super T, ?> idAttribute = entityType.getId(idClazz);
		String name = idAttribute.getName();
		ParamNode paramNode = tree.get(name);
		if(!(paramNode instanceof ValueNode))
			throw new IllegalStateException("The id field in the hibernate entity should have matched to a "
					+ "ValueNode on incoming data and did not.  bad multipart form?  (Please "
					+ "let us know so we can pair with you on this and I can add better error messaging)");
		ValueNode node = (ValueNode) paramNode;
		String value = node.getValue();
		if(value == null)
			return beanCreate.apply(paramTypeToCreate);
		
		Function<String, Object> unmarshaller = translator.getUnmarshaller(idClazz);
		Object id = unmarshaller.apply(value);
		return entityManager.find(paramTypeToCreate, id);
	}

}
