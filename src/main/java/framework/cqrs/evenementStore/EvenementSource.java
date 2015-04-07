package java.framework.cqrs.evenementStore;

import java.util.List;

/**
 * Created by Nicolas Caline on 07-04-15.
 */
public interface EvenementSource<EvenementType> {
    String getType();

    long getVersion();

    long getTimestamp();

    List<? extends EvenementType> getEvenements();
}
