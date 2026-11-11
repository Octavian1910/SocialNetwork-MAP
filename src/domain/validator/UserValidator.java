package domain.validator;

import domain.User;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Concrete strategy implementing domain validation for {@link User} entities.
 */
public class UserValidator implements Validator<User> {

    /**
     * Internal encapsulation of a single validation rule and its descriptive error message.
     *
     * @param predicate    condition that a valid entity must satisfy
     * @param errorMessage message returned if condition fails
     */
    private record ValidationRule(Predicate<User> predicate, String errorMessage) {
    }

    private final List<ValidationRule> rules = new ArrayList<>();

    /**
     * Constructs a new UserValidator with standard validation rules.
     */
    public UserValidator() {
        rules.add(new ValidationRule(user -> user.getId() == null || user.getId() > 0, "User ID must be positive!"));
        rules.add(new ValidationRule(user -> user.getFirstName() != null && !user.getFirstName().trim().isEmpty(), "First name cannot be empty!"));
        rules.add(new ValidationRule(user -> user.getFirstName() != null && !user.getFirstName().trim().isEmpty() && Character.isUpperCase(user.getFirstName().trim().charAt(0)), "First name must start with an uppercase letter!"));
        rules.add(new ValidationRule(user -> user.getLastName() != null && !user.getLastName().trim().isEmpty(), "Last name cannot be empty!"));
        rules.add(new ValidationRule(user -> user.getLastName() != null && !user.getLastName().trim().isEmpty() && Character.isUpperCase(user.getLastName().trim().charAt(0)), "Last name must start with an uppercase letter!"));
    }

    /**
     * Validates a User against all registered constraints.
     *
     * @param user the entity to check
     * @throws ValidationException if any validation rule is violated
     */
    @Override
    public void validate(User user) {
        if (user == null) {
            throw new ValidationException("User cannot be null!");
        }
        List<String> errors = rules.stream()
                .filter(rule -> !rule.predicate().test(user))
                .map(ValidationRule::errorMessage)
                .toList();

        if (!errors.isEmpty()) {
            throw new ValidationException(String.join("\n", errors));
        }
    }
}