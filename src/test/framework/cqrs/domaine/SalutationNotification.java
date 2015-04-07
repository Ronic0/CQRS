package framework.cqrs.domaine;

/**
 * Created by Nicolas Caline on 07-04-15.
 */
public class SalutationNotification implements Notification {
    private final String message;

    public SalutationNotification(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
