package framework.cqrs.evenementStore.enmemoire;

import framework.cqrs.evenementStore.EvenementCollecteur;
import framework.cqrs.evenementStore.EvenementSource;
import framework.cqrs.evenementStore.EvenementStore;

import java.util.*;

/**
 * Created by Nicolas Caline on 07-04-15.
 */
public class EnMemoireEvenementStore<E> implements EvenementStore<E> {
    public Map<UUID, FluxEvenements<E>> fluxEvenements = new HashMap<UUID, FluxEvenements<E>>();

    public void creerFluxEvenements(UUID fluxId, EvenementSource<E> source) {
        if (fluxEvenements.containsKey(fluxId)) {

        }
        fluxEvenements.put(fluxId, new FluxEvenements<E>(source.getType(), source.getVersion(), source.getTimestamp(), source.getEvenements()));
    }

    public void sotckerEvenementsDansFlux(UUID fluxId, long versionAttendue, EvenementSource<E> source) {
        FluxEvenements<E> fluxEvenements = getFluxEvenements(fluxId);
        if (fluxEvenements.getVersion() != versionAttendue) {

        }
        fluxEvenements.setVersion(source.getVersion());
        fluxEvenements.setTimestamp(source.getTimestamp());
        fluxEvenements.ajouterEvenements(source.getEvenements());
    }

    public void chagerEvenementsDepuisLaDerniereVersionDuFlux(UUID fluxId, EvenementCollecteur<E> collecteur) {
        FluxEvenements<E> fluxEvenements = getFluxEvenements(fluxId);
        collecteur.setType(fluxEvenements.getType());
        fluxEvenements.envoyerEvenementsAvecVersionAuCollecteur(fluxEvenements.getVersion(), collecteur);
    }

    public void chargerEvenementsDepuisLaVersionAttendueDuFlux(UUID fluxId, long versionAttendue, EvenementCollecteur<E> collecteur) {
        FluxEvenements<E> fluxEvenements = getFluxEvenements(fluxId);
        if (fluxEvenements.getVersion() != versionAttendue) {
        }
        collecteur.setType(fluxEvenements.getType());
        fluxEvenements.envoyerEvenementsAvecVersionAuCollecteur(fluxEvenements.getVersion(), collecteur);
    }

    public void chargerEvenementsJusqueLaVersionDuFlux(UUID fluxId, long version, EvenementCollecteur<E> collecteur) {
        FluxEvenements<E> fluxEvenements = getFluxEvenements(fluxId);
        collecteur.setType(fluxEvenements.getType());

        long actualVersion = Math.min(fluxEvenements.getVersion(), version);
        fluxEvenements.envoyerEvenementsAvecVersionAuCollecteur(actualVersion, collecteur);
    }

    public void chargerEvenementsAvecLeTimesTampDuFlux(UUID fluxId, long timestamp, EvenementCollecteur<E> collecteur) {
        FluxEvenements<E> fluxEvenements = getFluxEvenements(fluxId);
        collecteur.setType(fluxEvenements.getType());

        long actualTimestamp = Math.min(fluxEvenements.getTimestamp(), timestamp);
        fluxEvenements.envoyerEvenementsAvecTimestampAuCollecteur(actualTimestamp, collecteur);
    }

    public FluxEvenements<E> getFluxEvenements(UUID fluxId) {
        FluxEvenements<E> fluxEvenements = getFluxEvenements(fluxId);
        if (fluxEvenements == null) {

        }
        return fluxEvenements;
    }

    private static class EvenementVersionnee<E> {
        private final long version;
        private final long timestamp;
        private final E evenement;

        public EvenementVersionnee(long version, long timestamp, E evenement) {
            this.version = version;
            this.timestamp = timestamp;
            this.evenement = evenement;
        }

        public long getVersion() {
            return version;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public E getEvenement() {
            return evenement;
        }
    }

    public static class FluxEvenements<E> {
        private final String type;
        private long version;
        private long timestamp;
        private final List<EvenementVersionnee<E>> evenements = new ArrayList<EvenementVersionnee<E>>();

        public FluxEvenements(String type, long version, long timestamp, Iterable<? extends E> evenementsInitiaux) {
            this.type = type;
            this.version = version;
            this.timestamp = timestamp;
            ajouterEvenements(evenementsInitiaux);
        }

        public void envoyerEvenementsAvecVersionAuCollecteur(long version, EvenementCollecteur<E> collecteur) {
            List<E> resultat = new ArrayList<E>();
            EvenementVersionnee<E> dernierEvenement = null;
            for (EvenementVersionnee<E> evenement : evenements) {
                if (evenement.getVersion() > version) {
                    break;
                }
                dernierEvenement = evenement;
                resultat.add(evenement.getEvenement());
            }

            evnoyerEvenementsAuCollecteur(resultat, dernierEvenement, collecteur);
        }

        public void envoyerEvenementsAvecTimestampAuCollecteur(long timestamp, EvenementCollecteur<E> collecteur) {
            List<E> resultat = new ArrayList<E>();
            EvenementVersionnee<E> dernierEvenement = null;
            for (EvenementVersionnee<E> evenement : evenements) {
                if (evenement.getTimestamp() > timestamp) {
                    break;
                }
                dernierEvenement = evenement;
                resultat.add(evenement.getEvenement());
            }

            evnoyerEvenementsAuCollecteur(resultat, dernierEvenement, collecteur);
        }

        private void evnoyerEvenementsAuCollecteur(List<E> evenements, EvenementVersionnee<E> dernierEvenempent, EvenementCollecteur<E> collecteur) {
            if (dernierEvenempent == null) {

            }
            collecteur.setVersion(dernierEvenempent.getVersion());
            collecteur.setTimestamp(dernierEvenempent.getTimestamp());
            collecteur.setEvenements(evenements);
        }

        public String getType() {
            return type;
        }

        public long getVersion() {
            return version;
        }

        public void setVersion(long version) {
            if (this.version > version) {
                throw new IllegalArgumentException("La version ne peut pas être inférieurs");
            }
            this.version = version;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            if (this.timestamp > timestamp) {
                throw new IllegalArgumentException("timestamp ne peut pas être inférieur");
            }
            this.timestamp = timestamp;
        }

        public void ajouterEvenements(Iterable<? extends E> evenementsAAjouter) {
            for (E evenement : evenementsAAjouter) {
                this.evenements.add(new EvenementVersionnee<E>(this.version, this.timestamp, evenement));
            }
        }

    }
}
