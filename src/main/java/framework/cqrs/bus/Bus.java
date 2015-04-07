package framework.cqrs.bus;

/**
 * Created by Nicolas Caline on 07-04-15.
 */
public interface Bus {
    void envoyer(Object message) throws MessageHandlingException;

    Reponse envoyerEtAttendreUneReponse(Object message) throws MessageHandlingException;

    void repondre(Object message) throws MessageHandlingException;

    void repondre(Iterable<?> messages) throws MessageHandlingException;

    void publier(Object message) throws MessageHandlingException;

    void publier(Iterable<?> messages) throws MessageHandlingException;

    Object getMessageCourant();
}
