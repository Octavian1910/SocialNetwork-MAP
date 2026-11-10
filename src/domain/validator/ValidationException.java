package domain.validator;

/**
 * Unchecked exception thrown whenever an entity violates domain validation constraints.
 */
public class ValidationException extends RuntimeException {

    /**
     * Constructs a ValidationException with a specific error message.
     *
     * @param message descriptive text detailing the validation failure(s)
     */
    public ValidationException(String message) {
        super(message);
    }
}