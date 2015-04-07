package framework.cqrs.bus;

/**
 * Created by Nicolas Caline on 07-04-15.
 */
public interface BusSynchronization {
    void beforeHandleMessage();

    void afterHandleMessage();
}
