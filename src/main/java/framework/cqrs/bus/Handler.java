package framework.cqrs.bus;

/**
 * Created by Nicolas Caline on 07-04-15.
 */
public interface Handler<T> {

    Class<T> getMessageType();

    void handleMessage(T message) throws Exception;
}
