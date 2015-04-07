package java.framework.cqrs.domaine;

import java.util.UUID;

/**
 * Created by Nicolas Caline on 07-04-15.
 */
public class AggregateRootNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final String aggregateRootType;
    private final UUID aggregateRootId;

    public AggregateRootNotFoundException(String type, UUID id) {
        super("aggregate root " + type + " avec id " + id);
        this.aggregateRootType = type;
        this.aggregateRootId = id;
    }

    public String getAggregateRootType() {
        return aggregateRootType;
    }

    public UUID getAggregateRootId() {
        return aggregateRootId;
    }
}
