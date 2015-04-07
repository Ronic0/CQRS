package framework.cqrs.domaine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nicolas Caline on 07-04-15.
 */
public class Aggregate {

    private IdVersionnee idVersionnee;
    private final Map<Object, Entite<?>> entitesParId;
    private final List<Evenement> evenementsNonSauves;
    private final List<Notification> notifications;

    public Aggregate(IdVersionnee id) {
        this.idVersionnee = id;
        this.entitesParId = new HashMap<Object, Entite<?>>();
        this.evenementsNonSauves = new ArrayList<Evenement>();
        this.notifications = new ArrayList<Notification>();
    }

    protected void appliquer(Evenement evenement) {
        Entite<?> entite = entitesParId.get(evenement.getEntiteId());
        entite.onEvenement(evenement);
        evenementsNonSauves.add(evenement);
    }

    protected void notifier(Notification notification) {
        notifications.add(notification);
    }

    public void ajouter(Entite<?> entite) {
        entitesParId.put(entite.getId(), entite);
    }

    public void retirer(Entite<?> entite) {
        entitesParId.remove(entite.getId());
    }

    public IdVersionnee getIdVersionnee() {
        return idVersionnee;
    }

    public void chargerDepuisHistorique(Iterable<? extends Evenement> evenements) {
        for (Evenement evenement : evenements) {
            Entite<?> entite = entitesParId.get(evenement.getEntiteId());
            entite.onEvenement(evenement);
        }
    }

    public List<? extends Evenement> getEvenementsNonSauves() {
        return new ArrayList<Evenement>(evenementsNonSauves);
    }

    public void viderEvenementsNonSauves() {
        evenementsNonSauves.clear();
    }

    public void incrementeVersion() {
        idVersionnee = idVersionnee.versionSuivante();
    }

    public List<Notification> getNotifications() {
        return new ArrayList<Notification>(notifications);
    }

    public void viderNotifications() {
        notifications.clear();
    }
}
