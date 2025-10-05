package work.slhaf.snippet.exception;

public class LaunchCheckException extends RuntimeException{
    public LaunchCheckException(String message) {
        super(message);
    }

    public LaunchCheckException(String message, Throwable cause) {
        super(message, cause);
    }
}
