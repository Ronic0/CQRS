package framework.cqrs.evenementStore;

import java.util.UUID;

/**
 * Created by Nicolas Caline on 07-04-15.
 */
public interface EvenementStore<EvenementType> {
    void creerFluxEvenements(UUID fluxId, EvenementSource<EvenementType> source);

    void sotckerEvenementsDansFlux(UUID fluxId, long expectedVersion, EvenementSource<EvenementType> source);

    void chagerEvenementsDepuisLaDerniereVersionDuFlux(UUID fluxId, EvenementCollecteur<EvenementType> collecteur);

    void chargerEvenementsDepuisLaVersionAttendueDuFlux( UUID fluxId, long versionAttendue, EvenementCollecteur<EvenementType> collecteur);

    void chargerEvenementsJusqueLaVersionDuFlux(UUID fluxId, long version, EvenementCollecteur<EvenementType> collecteur);

    void chargerEvenementsAvecLeTimesTampDuFlux(UUID fluxId, long timestamp, EvenementCollecteur<EvenementType> collecteur);

}
