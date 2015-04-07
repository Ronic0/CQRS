package framework.cqrs.bus;

/**
 * Created by Nicolas Caline on 07-04-15.
 */
public class MessageHandlingException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public MessageHandlingException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageHandlingException(String message) {
        super(message);
    }

    public MessageHandlingException(Throwable cause) {
        super(cause);
    }
}
