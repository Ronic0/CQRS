package java.framework.cqrs.domaine;

import java.util.UUID;

/**
 * Created by Nicolas Caline on 07-04-15.
 */
public interface Repository {

    <T extends AggregateRoot> T getParId(Class<T> type, UUID id);

    < T extends AggregateRoot> T getParIdVersionnee(Class<T> type, IdVersionnee id);

    <T extends AggregateRoot> void ajouter(T aggregate);

}