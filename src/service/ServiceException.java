package service;

/**
 * Exception thrown for business logic and service-level errors.
 */
public class ServiceException extends RuntimeException {
    /**
     * Constructs a new ServiceException with the specified error message.
     *
     * @param message detailed error description
     */
    public ServiceException(String message) {
        super(message);
    }
}