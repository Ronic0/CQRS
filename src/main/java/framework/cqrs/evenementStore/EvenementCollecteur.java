package java.framework.cqrs.evenementStore;

/**
 * Created by Nicolas Caline on 07-04-15.
 */
public interface  EvenementCollecteur<EvenementType> {

    void setType(String type);

    void setVersion(long version);

    void setTimestamp(long timestamp);

    void setEvenements(Iterable<? extends EvenementType> evenements);
}
