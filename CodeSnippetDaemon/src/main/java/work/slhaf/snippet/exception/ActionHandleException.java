package work.slhaf.snippet.exception;

public class ActionHandleException extends RuntimeException
{
    public ActionHandleException(String message) {
        super(message);
    }

    public ActionHandleException(String message, Throwable cause) {
        super(message, cause);
    }
}
