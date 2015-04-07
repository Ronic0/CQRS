package framework.cqrs.bus;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import java.util.*;

/**
 * Created by Nicolas Caline on 07-04-15.
 */
public class LocaleEnMemoireBus implements Bus {

    private final Collection<BusSynchronization> synchronizations = new ArrayList<BusSynchronization>();
    private final Multimap<Class<?>, Handler<?>> handlers = HashMultimap.create();

    private final ThreadLocal<Queue<Object>> queueEvenements = new ThreadLocal<Queue<Object>>() {
        protected java.util.Queue<Object> valeurInitiale() {
            return new LinkedList<Object>();
        }
    };
    private final ThreadLocal<MessageCourantInformation> etat = new ThreadLocal<MessageCourantInformation>() {
        protected MessageCourantInformation valeurInitiale() {
            return new MessageCourantInformation(null);
        }
    };

    public void envoyer(Object message) throws MessageHandlingException {
        dispatchMessage(message);
        dispatchAllQueuedMessages();
    }

    public Reponse envoyerEtAttendreUneReponse(Object commande) {
        List<Reponse> reponses = dispatchMessage(commande);
        if (reponses.isEmpty()) {
            throw new MessageHandlingException("no response while executing command " + commande);
        }

        dispatchAllQueuedMessages();
        return reponses.get(0);
    }

    public void repondre(Object message) {
        repondre(Collections.singleton(message));
    }

    public void repondre(Iterable<?> messages) {
        if (getMessageCourant() == null) {
            throw new MessageHandlingException("no current message to reply to");
        }

        etat.get().ajouterReponses(messages);
        publier(messages);
    }

    public void publier(Object message) {
        publier(Collections.singleton(message));
    }

    public void publier(Iterable<?> messages) {
        Iterables.addAll(queueEvenements.get(), messages);
        if (getMessageCourant() == null) {
            dispatchAllQueuedMessages();
        }
    }

    public Object getMessageCourant() {
        return etat.get().messageCourant;
    }

    public void setHandlers(Handler<?>... injectedHandlers) {
        handlers.clear();
        for (Handler<?> handler : injectedHandlers) {
            handlers.put(handler.getMessageType(), handler);
        }
    }

    public void setBusSynchronizations(BusSynchronization... synchronizations) {
        this.synchronizations.clear();
        this.synchronizations.addAll(Arrays.asList(synchronizations));
    }

    private void dispatchAllQueuedMessages() {
        try {
            while (!queueEvenements.get().isEmpty()) {
                dispatchMessage(queueEvenements.get().poll());
            }
        } catch (RuntimeException e) {
            queueEvenements.get().clear();
            throw e;
        }
    }

    private List<Reponse> dispatchMessage(Object message) {
        MessageCourantInformation etatSauvegarde = etat.get();
        try {
            etat.set(new MessageCourantInformation(message));
            invokeBeforeHandleMessage();
            invokeHandlers(message);
            invokeAfterHandleMessage();
            return etat.get().reponses;
        } finally {
            etat.set(etatSauvegarde);
        }
    }

    private void invokeBeforeHandleMessage() {
        for (BusSynchronization synchronization : synchronizations) {
            synchronization.beforeHandleMessage();
        }
    }

    private void invokeAfterHandleMessage() {
        for (BusSynchronization synchronization : synchronizations) {
            synchronization.afterHandleMessage();
        }
    }

    @SuppressWarnings("unchecked")
    private void invokeHandlers(Object message) {
        try {
            Collection<Handler<?>> matchedHandlers = handlers.get(message.getClass());
            for (Handler handler : matchedHandlers) {
                handler.handleMessage(message);
            }
        } catch (Exception ex) {
            throw new MessageHandlingException(ex);
        }
    }

    private static class MessageCourantInformation {
        public Object messageCourant;
        public List<Reponse> reponses = new ArrayList<Reponse>();

        public MessageCourantInformation(Object messageCourant) {
            this.messageCourant = messageCourant;
        }

        void ajouterReponses(Iterable<?> messages) {
            reponses.add(new Reponse(messages));
        }
    }
}
