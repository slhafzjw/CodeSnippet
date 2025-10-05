package work.slhaf.snippet.exception;

public class SnippetManagerException extends RuntimeException{
    public SnippetManagerException(String message) {
        super(message);
    }

    public SnippetManagerException(String message, Throwable cause) {
        super(message, cause);
    }
}
