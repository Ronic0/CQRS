package framework.cqrs.domaine;

/**
 * Created by Nicolas Caline on 07-04-15.
 */
public class SalutationEvenement extends Evenement {

    private final String message;

    public SalutationEvenement(IdVersionnee aggregateRootId, String message) {
        super(aggregateRootId, aggregateRootId.getId());
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
