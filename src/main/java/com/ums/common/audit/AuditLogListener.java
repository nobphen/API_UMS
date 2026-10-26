package com.ums.common.audit;

import com.ums.common.audit.EntityChangedEvent.Action;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.http.HttpServletRequest;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.databind.json.JsonMapper;

/**
 * Watches every insert, update and delete Hibernate makes, and for {@link Auditable} entities sends an
 * {@link EntityChangedEvent} (saved to audit_logs by the security module).
 * Only simple values are kept (text, numbers, dates, enums); links to other entities and lists are left out,
 * and so are secrets such as passwords.
 */
@Component
@RequiredArgsConstructor
public class AuditLogListener implements PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener {

    private static final Set<String> HIDDEN = Set.of("password", "passwordhash", "secret", "token", "otp");

    private final EntityManagerFactory entityManagerFactory;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditorAwareImpl auditorAware;
    private final JsonMapper jsonMapper;

    /** Tells Hibernate to call this listener. */
    @PostConstruct
    void register() {
        EventListenerRegistry registry = entityManagerFactory.unwrap(SessionFactoryImplementor.class)
                .getServiceRegistry().getService(EventListenerRegistry.class);
        registry.appendListeners(EventType.POST_INSERT, this);
        registry.appendListeners(EventType.POST_UPDATE, this);
        registry.appendListeners(EventType.POST_DELETE, this);
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {
        if (!isAuditable(event.getEntity())) return;
        String[] names = event.getPersister().getPropertyNames();
        publish(Action.CREATE, event.getEntity(), event.getId(), null, toJson(names, event.getState(), null));
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        if (!isAuditable(event.getEntity())) return;
        String[] names = event.getPersister().getPropertyNames();
        int[] changed = event.getDirtyProperties();
        String oldValue = event.getOldState() == null ? null : toJson(names, event.getOldState(), changed);
        String newValue = toJson(names, event.getState(), changed);
        if (newValue == null) return;   // only links or hidden fields changed
        publish(Action.UPDATE, event.getEntity(), event.getId(), oldValue, newValue);
    }

    @Override
    public void onPostDelete(PostDeleteEvent event) {
        if (!isAuditable(event.getEntity())) return;
        String[] names = event.getPersister().getPropertyNames();
        publish(Action.DELETE, event.getEntity(), event.getId(), toJson(names, event.getDeletedState(), null), null);
    }

    /** The event is sent at once; the audit row is written after the transaction commits. */
    @Override
    public boolean requiresPostCommitHandling(EntityPersister persister) {
        return false;
    }

    private static boolean isAuditable(Object entity) {
        return entity.getClass().isAnnotationPresent(Auditable.class);
    }

    private void publish(Action action, Object entity, Object id, String oldValue, String newValue) {
        Long entityId = id instanceof Number n ? n.longValue() : null;
        Long userId = auditorAware.getCurrentAuditor().orElse(null);
        eventPublisher.publishEvent(new EntityChangedEvent(
                userId, action, entity.getClass().getSimpleName(), entityId, oldValue, newValue, clientIp()));
    }

    /** JSON of the simple fields; only the fields at {@code indexes} when given. Null when nothing is left. */
    private String toJson(String[] names, Object[] values, int[] indexes) {
        Map<String, Object> fields = new LinkedHashMap<>();
        if (indexes == null) {
            for (int i = 0; i < names.length; i++) addField(fields, names[i], values[i]);
        } else {
            for (int i : indexes) addField(fields, names[i], values[i]);
        }
        return fields.isEmpty() ? null : jsonMapper.writeValueAsString(fields);
    }

    private static void addField(Map<String, Object> fields, String name, Object value) {
        if (HIDDEN.contains(name.toLowerCase())) return;
        if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean
                || value instanceof Enum<?> || value instanceof Temporal || value instanceof Date
                || value instanceof UUID) {
            fields.put(name, value);
        }
    }

    /** The caller's IP (the first one in X-Forwarded-For when behind a proxy); null outside a web request. */
    private static String clientIp() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
