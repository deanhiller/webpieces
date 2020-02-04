package org.webpieces.plugins.hibernate;

import java.lang.annotation.Annotation;
import java.util.function.Function;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

import org.hibernate.metamodel.model.domain.internal.EntityTypeImpl;
import org.webpieces.router.api.extensions.EntityLookup;
import org.webpieces.router.api.extensions.ObjectStringConverter;
import org.webpieces.router.impl.params.Meta;
import org.webpieces.router.impl.params.ObjectTranslator;
import org.webpieces.router.impl.params.ParamMeta;
import org.webpieces.router.impl.params.ParamNode;
import org.webpieces.router.impl.params.ParamTreeNode;
import org.webpieces.router.impl.params.ValueNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateLookup implements EntityLookup {

	private static final Logger log = LoggerFactory.getLogger(HibernateLookup.class);
	private ObjectTranslator translator;
	
	@Inject
	public HibernateLookup(ObjectTranslator translator) {
		this.translator = translator;
	}
	
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

	@SuppressWarnings("unchecked")
	@Override
	public <T> T find(Meta paramMeta, ParamTreeNode tree, 
			Function<Class<T>, T> beanCreate) {
		if(!(paramMeta instanceof ParamMeta))
			throw new UnsupportedOperationException("this plugin does not support type="+paramMeta.getClass());
		
		ParamMeta m = (ParamMeta) paramMeta;
		Class<T> paramTypeToCreate = (Class<T>) m.getFieldClass();
		EntityManager entityManager = Em.get();
		Metamodel metamodel = entityManager.getMetamodel();
		ManagedType<T> managedType = metamodel.managedType(paramTypeToCreate);
		EntityTypeImpl<T> entityType = (EntityTypeImpl<T>) managedType;
		Class<?> idClazz = entityType.getIdType().getJavaType();
		SingularAttribute<? super T, ?> idAttribute = entityType.getId(idClazz);
		String name = idAttribute.getName();
		ParamNode paramNode = tree.get(name);
		
		String value = null;
		if(paramNode != null) {
			if(!(paramNode instanceof ValueNode))
				throw new IllegalStateException("The id field in the hibernate entity should have matched to a "
						+ "ValueNode on incoming data and did not. node="+paramNode+".  bad multipart form?  (Please "
						+ "let us know so we can pair with you on this and I can add better error messaging)");
			ValueNode node = (ValueNode) paramNode;
			value = node.getValue();
		}
		
		if(value == null)
			return beanCreate.apply(paramTypeToCreate);
		
		@SuppressWarnings("rawtypes")
		ObjectStringConverter unmarshaller = translator.getConverter(idClazz);
		Object id = unmarshaller.stringToObject(value);
		
		UseQuery namedQuery = fetchUseQuery(m.getAnnotations());
		if(namedQuery == null) 
			return entityManager.find(paramTypeToCreate, id);
		
		Query query = entityManager.createNamedQuery(namedQuery.value());
		query.setParameter(namedQuery.id(), id);
		return (T) query.getSingleResult();
	}

	private UseQuery fetchUseQuery(Annotation[] annotations) {
		for(Annotation anno : annotations) {
			if(anno instanceof UseQuery)
				return (UseQuery)anno; 
		}
		return null;
	}


}
