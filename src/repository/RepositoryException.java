package repository;

/**
 * Unchecked exception thrown for data persistence and storage errors.
 */
public class RepositoryException extends RuntimeException {

    public RepositoryException(String message) {
        super(message);
    }

    /**
     * Constructs an exception supporting exception chaining.
     *
     * @param message descriptive error message
     * @param cause   the underlying lower-level cause
     */
    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}