package domain.validator;

/**
 * Strategy interface defining the contract for entity validation.
 *
 * @param <T> the type of entity to validate
 */
@FunctionalInterface    //doar o metoda abstracta
public interface Validator<T> {

    /**
     * Validates the specified entity against domain rules.
     *
     * @param entity the entity instance to validate
     * @throws ValidationException if the entity violates domain validation rules
     */
    void validate(T entity);

    default Validator<T> andThen(Validator<T> other) { //default ne permite sa scriem cod intr o interfata; metoda returnează un nou validator
        return entity -> {
            this.validate(entity);
            other.validate(entity);
        };
    }
}