package java.framework.cqrs.domaine;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Created by Nicolas Caline on 07-04-15.
 */
public abstract class AggregateRoot extends Entite<UUID>{
    public AggregateRoot(IdVersionnee idVersionnee) {
        super(new Aggregate(idVersionnee), idVersionnee.getId());
    }

    public void chargerDepuisHistorique(Iterable<? extends Evenement> evenements) {
        aggregate.chargerDepuisHistorique(evenements);
    }

    public Collection<? extends Object> getNotifications() {
        return aggregate.getNotifications();
    }

    public void viderNotifications() {
        aggregate.viderNotifications();
    }

    public List<? extends Evenement> getEvenementsNonSauves() {
        return aggregate.getEvenementsNonSauves();
    }

    public IdVersionnee getIdVersionnee() {
        return aggregate.getIdVersionnee();
    }

    public void viderEvenementsNonSauves() {
        aggregate.viderEvenementsNonSauves();
    }

    public void incrementeVersion() {
        aggregate.incrementeVersion();
    }
}
