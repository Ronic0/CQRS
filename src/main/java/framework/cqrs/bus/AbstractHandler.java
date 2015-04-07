package java.framework.cqrs.bus;

/**
 * Created by Nicolas Caline on 07-04-15.
 */
public abstract class AbstractHandler<T> implements Handler<T>{
    private final Class<T> messageType;

    public AbstractHandler(Class<T> messageType) {
        this.messageType = messageType;
    }

    public final Class<T> getMessageType() {
        return messageType;
    }
}
