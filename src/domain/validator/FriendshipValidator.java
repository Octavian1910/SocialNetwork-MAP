package domain.validator;

import domain.Friendship;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class FriendshipValidator implements Validator<Friendship> {
    private record ValidationRule(Predicate<Friendship> predicate, String errorMessage) {}
    private final List<ValidationRule> rules = new ArrayList<>();

    public FriendshipValidator() {
        rules.add(new ValidationRule(
                fs -> fs.getFirstUserId() != null && fs.getFirstUserId() > 0,
                "First user ID must be positive!"
        ));
        rules.add(new ValidationRule(
                fs -> fs.getSecondUserId() != null && fs.getSecondUserId() > 0,
                "Second user ID must be positive!"
        ));
        rules.add(new ValidationRule(
                fs -> !Objects.equals(fs.getFirstUserId(), fs.getSecondUserId()),
                "A user cannot be friends with themselves!"
        ));
    }

    @Override
    public void validate(Friendship friendShip) {
        if (friendShip == null) {
            throw new ValidationException("Friendship cannot be null!");
        }
        List<String> errors = rules.stream()
                .filter(rule -> !rule.predicate().test(friendShip))
                .map(ValidationRule::errorMessage)
                .toList();
        if (!errors.isEmpty()) {
            throw new ValidationException(String.join(" ", errors));
        }
    }
}