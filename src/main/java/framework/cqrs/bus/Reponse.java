package framework.cqrs.bus;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Nicolas Caline on 07-04-15.
 */
public class Reponse {
    private final List<Object> messages = new ArrayList<Object>();

    public Reponse() {
    }

    public Reponse(Iterable<?> messages) {
        Iterables.addAll(this.messages, messages);
    }

    public List<Object> getMessages() {
        return messages;
    }

    public boolean contientReponseDeType(Class<?> type) {
        for (Object message : messages) {
            if (type.isInstance(message)) {
                return true;
            }
        }
        return false;
    }

    public <T> T getReponseDeType(Class<T> type) {
        List<T> notifications = getReponsesDeType(type);
        if (notifications.isEmpty()) {
            throw new IllegalArgumentException("no notification of type " + type.getName());
        } else if (notifications.size() > 1) {
            throw new IllegalArgumentException("multiple notifications of type " + type.getName());
        } else {
            return notifications.get(0);
        }
    }

    public <T> List<T> getReponsesDeType(Class<T> type) {
        ArrayList<T> result = new ArrayList<T>();
        for (Object notification : messages) {
            if (type.isInstance(notification)) {
                result.add(type.cast(notification));
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return  MoreObjects.toStringHelper(this).toString();
    }
}
