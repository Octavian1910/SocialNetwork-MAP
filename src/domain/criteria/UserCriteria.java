package domain.criteria;

import domain.User;
import java.util.function.Predicate;

/**
 * Utility class containing reusable search predicates for {@link User} entities.
 */
public final class UserCriteria { //final inseamna ca aceasta clasa nu poate sa fie mostenita

    private UserCriteria() { //nu poti crea cu new UserCriteria
        // Private constructor to prevent instantiation of utility class
    }

    /**
     * Predicate checking if a user's first name starts with the given prefix.
     *
     * @param prefix the string prefix to match
     * @return a predicate testing the user's first name
     */
    public static Predicate<User> firstNameStartsWith(String prefix) {
        return user -> user.getFirstName() != null && user.getFirstName().startsWith(prefix);
    }

    /**
     * Predicate checking if a user's last name matches the given name (case-insensitive).
     *
     * @param lastName the last name to match
     * @return a predicate testing the user's last name
     */
    public static Predicate<User> hasLastName(String lastName) {
        return user -> user.getLastName() != null && user.getLastName().equalsIgnoreCase(lastName);
    }

    /**
     * Predicate checking if a user's ID is strictly greater than the specified minimum.
     *
     * @param minId the minimum ID threshold
     * @return a predicate testing the user's ID
     */
    public static Predicate<User> idGreaterThan(int minId) {
        return user -> user.getId() > minId;
    }
}