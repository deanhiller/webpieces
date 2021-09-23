package org.webpieces.plugin.hibernate.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.lang.NonNull;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.*;
import org.hibernate.persister.entity.EntityPersister;
import org.webpieces.ctx.api.ContentType;
import org.webpieces.ctx.api.Current;
import org.webpieces.util.context.Context;
import org.webpieces.util.context.ContextKey;

import javax.persistence.Entity;

/**
 * An optional MeterBinder you can use to track Hibernate Entity Metrics.
 * <p>
 * It allows you to aggregate on entity name, relative request path, and service name, (and maybe more in the future)
 */
public class HibernateEntityMeterBinder implements MeterBinder {

    private final SessionFactory sessionFactory;

    public HibernateEntityMeterBinder(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void bindTo(@NonNull MeterRegistry meterRegistry) {
        if (sessionFactory instanceof SessionFactoryImplementor) {
            EventListenerRegistry eventListenerRegistry = ((SessionFactoryImplementor) sessionFactory).getServiceRegistry().getService(EventListenerRegistry.class);
            EntityEventListener eventListener = new EntityEventListener(meterRegistry);
            eventListenerRegistry.appendListeners(EventType.POST_LOAD, eventListener);
            eventListenerRegistry.appendListeners(EventType.POST_DELETE, eventListener);
            eventListenerRegistry.appendListeners(EventType.POST_UPDATE, eventListener);
            eventListenerRegistry.appendListeners(EventType.POST_INSERT, eventListener);
        }
    }

    static class EntityEventListener implements PostLoadEventListener, PostDeleteEventListener, PostUpdateEventListener, PostInsertEventListener {

        private final MeterRegistry meterRegistry;

        EntityEventListener(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;
        }

        private Tags getTags(String entityName) {
            try {
                String requestPath = (String) Context.get(ContextKey.REQUEST_PATH.toString());
                if (requestPath == null || requestPath.isBlank()) {
                    requestPath = "unknown";
                }
                return Tags.of(
                        HibernateEntityMeterTags.ENTITY_NAME, entityName,
                        HibernateEntityMeterTags.REQUEST, requestPath
                );
            } catch (NullPointerException ignore) {
                return null;
            }
        }

        /**
         * @return The name of an object's @Entity annotation (or the unqualified name of the class)
         * @see Entity
         */
        private String getEntityName(Object entity) {
            String entityName = entity.getClass().getAnnotation(Entity.class).name();
            if (entityName.isBlank()) {
                entityName = entity.getClass().getSimpleName();
            }
            return entityName;
        }

        @Override
        public void onPostLoad(PostLoadEvent event) {
            String entityName = getEntityName(event.getEntity());
            Tags tags = getTags(entityName);
            if (tags == null) return;
            Counter.builder(DatabaseMetric.DATABASE_ENTITY_LOADS.getDottedMetricName())
                    .tags(tags)
                    .description("Entity loads")
                    .register(meterRegistry)
                    .increment();
        }

        @Override
        public void onPostDelete(PostDeleteEvent event) {
            String entityName = getEntityName(event.getEntity());
            Tags tags = getTags(entityName);
            if (tags == null) return;
            Counter.builder(DatabaseMetric.DATABASE_ENTITY_DELETES.getDottedMetricName())
                    .tags(tags)
                    .description("Entity deletes")
                    .register(meterRegistry)
                    .increment();
        }

        @Override
        public void onPostUpdate(PostUpdateEvent event) {
            String entityName = getEntityName(event.getEntity());
            Tags tags = getTags(entityName);
            if (tags == null) return;
            Counter.builder(DatabaseMetric.DATABASE_ENTITY_UPDATES.getDottedMetricName())
                    .tags(tags)
                    .description("Entity updates")
                    .register(meterRegistry)
                    .increment();
        }

        @Override
        public void onPostInsert(PostInsertEvent event) {
            String entityName = getEntityName(event.getEntity());
            Tags tags = getTags(entityName);
            if (tags == null) return;
            Counter.builder(DatabaseMetric.DATABASE_ENTITY_INSERTS.getDottedMetricName())
                    .tags(tags)
                    .description("Entity inserts")
                    .register(meterRegistry)
                    .increment();
        }

        @Override
        public boolean requiresPostCommitHanding(EntityPersister persister) {
            return false;
        }

    }

}
