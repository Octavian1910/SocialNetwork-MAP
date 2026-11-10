package domain.validator;

import domain.FriendShip;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Concrete strategy implementing domain validation for {@link FriendShip} entities.
 */
public class FriendShipValidator implements Validator<FriendShip> {

    private record ValidationRule(Predicate<FriendShip> predicate, String errorMessage) {}

    private final List<ValidationRule> rules = new ArrayList<>();

    public FriendShipValidator() {
        rules.add(new ValidationRule(
                fs -> fs.getFirstUserId() > 0,
                "First user ID must be positive!"
        ));

        rules.add(new ValidationRule(
                fs -> fs.getSecondUserId() > 0,
                "Second user ID must be positive!"
        ));

        rules.add(new ValidationRule(
                fs -> fs.getFirstUserId() != fs.getSecondUserId(),
                "A user cannot be friends with themselves!"
        ));
    }

    @Override
    public void validate(FriendShip friendShip) {
        if (friendShip == null) {
            throw new ValidationException("Friendship cannot be null!");
        }

        List<String> errors = rules.stream()
                .filter(rule -> !rule.predicate().test(friendShip))
                .map(rule -> rule.errorMessage())
                .toList();

        if (!errors.isEmpty()) {
            throw new ValidationException(String.join(" ", errors));
        }
    }
}