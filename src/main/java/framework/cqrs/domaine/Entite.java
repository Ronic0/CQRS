package java.framework.cqrs.domaine;

/**
 * Created by Nicolas Caline on 07-04-15.
 */
public abstract class Entite<IdType> {
    private final IdType id;
    protected final Aggregate aggregate;

    public Entite(Aggregate aggregateContext, IdType id) {
        this.aggregate = aggregateContext;
        this.id = id;
        this.aggregate.ajouter(this);
    }

    public IdType getId() {
        return id;
    }

    protected void appliquer(Evenement evenement) {
        aggregate.appliquer(evenement);
    }

    protected void notifier(Notification notification) {
        aggregate.notifier(notification);
    }

    protected abstract void onEvenement(Evenement evenement);
}
