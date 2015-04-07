package framework.cqrs.domaine;

/**
 * Created by Nicolas Caline on 07-04-15.
 */
public class Evenement {
    private final IdVersionnee aggregateRootId;
    private final Object entiteId;

    public Evenement(IdVersionnee aggregateRootId, Object entiteId) {
        this.aggregateRootId = aggregateRootId;
        this.entiteId = entiteId;
    }

    public IdVersionnee getAggregateRootId() {
        return aggregateRootId;
    }

    public Object getEntiteId() {
        return entiteId;
    }
}
