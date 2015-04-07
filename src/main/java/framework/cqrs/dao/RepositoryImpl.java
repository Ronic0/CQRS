package framework.cqrs.dao;

import framework.cqrs.bus.Bus;
import framework.cqrs.bus.BusSynchronization;
import framework.cqrs.domaine.AggregateRoot;
import framework.cqrs.domaine.Evenement;
import framework.cqrs.domaine.IdVersionnee;
import framework.cqrs.domaine.Repository;
import framework.cqrs.evenementStore.EvenementCollecteur;
import framework.cqrs.evenementStore.EvenementSource;
import framework.cqrs.evenementStore.EvenementStore;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by Nicolas Caline on 07-04-15.
 */
public class RepositoryImpl implements Repository, BusSynchronization {
    private final EvenementStore<Evenement> evenementStore;
    private final Bus bus;

    private final ThreadLocal<Session> sessions = new ThreadLocal<Session>() {
        @Override
        protected Session initialValue() {
            return new Session();
        }
    };

    public RepositoryImpl(EvenementStore<Evenement> evenementStore, Bus bus) {
        this.evenementStore = evenementStore;
        this.bus = bus;
    }

    public <T extends AggregateRoot> T getParId(Class<T> type, UUID id) {
        return sessions.get().getParId(type, id);
    }

    public <T extends AggregateRoot> T getParIdVersionnee(Class<T> type, IdVersionnee id) {
        return sessions.get().getParIdVersionnee(type, id);
    }

    public <T extends AggregateRoot> void ajouter(T aggregate) {
        if (aggregate != null) {
            sessions.get().ajouter(aggregate);
        }
    }

    public void afterHandleMessage() {
        sessions.get().afterHandleMessage();
    }

    public void beforeHandleMessage() {
        sessions.get().beforeHandleMessage();
    }

    private class Session {

        private Map<UUID, AggregateRoot> aggregatesParId = new HashMap<UUID, AggregateRoot>();

        public <T extends AggregateRoot> T getParId(Class<T> expectedType, UUID id) {
            T resultat = expectedType.cast(aggregatesParId.get(id));
            if (resultat != null) {
                return resultat;
            }

            try {
                AggregateRootCollecteur<T> collecteur = new AggregateRootCollecteur<T>(expectedType, id);
                evenementStore.chagerEvenementsDepuisLaDerniereVersionDuFlux(id, collecteur);
                return collecteur.getAggrateRoot();
            } catch (Exception ex) {
                return null;
            }
        }

        public <T extends AggregateRoot> T getParIdVersionnee(Class<T> expectedType, IdVersionnee id) {
            T resultat = expectedType.cast(aggregatesParId.get(id.getId()));
            if (resultat != null) {
                if (!id.versionSuivante().equals(resultat.getIdVersionnee())) {

                }
                return resultat;
            }

            try {
                AggregateRootCollecteur<T> collecteur = new AggregateRootCollecteur<T>(expectedType, id.getId());
                evenementStore.chargerEvenementsDepuisLaVersionAttendueDuFlux(id.getId(), id.getVersion(), collecteur);
                resultat = collecteur.getAggrateRoot();
                ajouterALaCession(resultat);
                return resultat;
            } catch (Exception e) {
                return null;
            }
        }

        public <T extends AggregateRoot> void ajouter(T aggregate) {
            if (aggregate.getEvenementsNonSauves().isEmpty()) {
                throw new IllegalArgumentException("aggregate n'a pas de changements non enregistrés");
            }
            ajouterALaCession(aggregate);
        }

        private <T extends AggregateRoot> void ajouterALaCession(T aggregate) {
            AggregateRoot previous = aggregatesParId.put(aggregate.getIdVersionnee().getId(), aggregate);
            if (previous != null && previous != aggregate) {
                throw new IllegalStateException("plusieurs instance avec le même id " + aggregate.getIdVersionnee().getId());
            }
        }

        public void beforeHandleMessage() {
        }

        public void afterHandleMessage() {
            Collection<Object> notifications = new ArrayList<Object>();
            for (AggregateRoot aggregate : aggregatesParId.values()) {
                notifications.addAll(aggregate.getNotifications());
                aggregate.viderNotifications();

                List<? extends Evenement> evenementsNonSauves = aggregate.getEvenementsNonSauves();
                if (!evenementsNonSauves.isEmpty()) {
                    bus.publier(evenementsNonSauves);
                    sauverAggregate(aggregate);
                }
            }
            bus.repondre(notifications);

            // should be done just before transaction commit...
            aggregatesParId.clear();
        }

        private void sauverAggregate(AggregateRoot aggregate) {
            if (aggregate.getIdVersionnee().isPourVersionInitiale()) {
                evenementStore.creerFluxEvenements(aggregate.getIdVersionnee().getId(), new AggregateRootSource(aggregate));
            } else {
                evenementStore.sotckerEvenementsDansFlux(aggregate.getIdVersionnee().getId(), aggregate.getIdVersionnee().getVersion() - 1, new AggregateRootSource(aggregate));
            }
            aggregate.viderEvenementsNonSauves();
            aggregate.incrementeVersion();
        }

    }

    public static class AggregateRootSource implements EvenementSource<Evenement> {

        private final AggregateRoot aggregateRoot;

        public AggregateRootSource(AggregateRoot aggregateRoot) {
            this.aggregateRoot = aggregateRoot;
        }

        public String getType() {
            return aggregateRoot.getClass().getName();
        }

        public long getVersion() {
            return aggregateRoot.getIdVersionnee().getVersion();
        }

        public long getTimestamp() {
            return System.currentTimeMillis();
        }

        public List<? extends Evenement> getEvenements() {
            return aggregateRoot.getEvenementsNonSauves();
        }

    }

    public static class AggregateRootCollecteur<T extends AggregateRoot> implements EvenementCollecteur<Evenement> {

        private final Class<T> expectedType;
        private final UUID id;

        private Class<? extends T> actualType;
        private long actualVersion;
        private T aggregateRoot;


        public AggregateRootCollecteur(Class<T> expectedType, UUID id) {
            this.expectedType = expectedType;
            this.id = id;
        }

        public void setType(String type) {
            try {
                actualType = Class.forName(type).asSubclass(expectedType);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        }

        public void setVersion(long version) {
            actualVersion = version + 1;
        }

        public void setTimestamp(long timestamp) {
            // unused
        }

        public void setEvenements(Iterable<? extends Evenement> evenements) {
            AggregateRootInstanciation();
            aggregateRoot.chargerDepuisHistorique(evenements);
        }

        private void AggregateRootInstanciation() {
            try {
                Constructor<? extends T> constructor = actualType.getConstructor(IdVersionnee.class);
                aggregateRoot = constructor.newInstance(IdVersionnee.pourVersionSpecifique(id, actualVersion));
            } catch (SecurityException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        public T getAggrateRoot() {
            return aggregateRoot;
        }

    }
}
