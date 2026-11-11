package service;

import domain.Friendship;
import domain.validator.ValidationException;
import domain.validator.Validator;
import repository.Repository;
import repository.RepositoryException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * Service coordinating friendship relationships between users.
 */
public class FriendShipService {
    private final Repository<String, Friendship> repository;
    private final UserService userService;
    private final Validator<Friendship> validator;

    public FriendShipService(Repository<String, Friendship> repository,
                             UserService userService,
                             Validator<Friendship> validator) {
        this.repository = repository;
        this.userService = userService;
        this.validator = validator;
    }

    /**
     * Adds a friendship between two existing users.
     *
     * @param firstUserId  first user ID
     * @param secondUserId second user ID
     * @throws ValidationException if friendship violates validation rules
     * @throws ServiceException    if users do not exist
     * @throws RepositoryException if friendship already exists
     */
    public void addFriendShip(Long firstUserId, Long secondUserId) {
        Friendship friendShip = new Friendship(firstUserId, secondUserId);
        validator.validate(friendShip);

        // Verificam existenta ambilor utilizatori inainte de salvare
        userService.findUserById(firstUserId);
        userService.findUserById(secondUserId);

        repository.add(friendShip);
    }

    /**
     * Removes a friendship relationship.
     *
     * @param firstUserId  first user ID
     * @param secondUserId second user ID
     * @throws ServiceException if the friendship does not exist
     */
    public void removeFriendShip(Long firstUserId, Long secondUserId) {
        Friendship friendShip = new Friendship(firstUserId, secondUserId);
        Optional<Friendship> removed = repository.remove(friendShip.getId());
        if (removed.isEmpty()) {
            throw new ServiceException("Friendship between " + firstUserId + " and " + secondUserId + " does not exist!");
        }
    }

    /**
     * Retrieves all registered friendships.
     *
     * @return collection of friendships
     */
    public Collection<Friendship> getAllFriendShips() {
        return repository.findAll();
    }

    /**
     * Returns all neighbor IDs connected to a given user (adjacency list representation).
     *
     * @param userId the user ID
     * @return collection of friend IDs
     */
    public Collection<Long> getFriendIds(Long userId) {
        return repository.findAll().stream()
                .filter(fs -> Objects.equals(fs.getFirstUserId(), userId) || Objects.equals(fs.getSecondUserId(), userId))
                .map(fs -> Objects.equals(fs.getFirstUserId(), userId) ? fs.getSecondUserId() : fs.getFirstUserId())
                .toList();
    }

    /**
     * Removes all friendships associated with a specific user.
     *
     * @param userId the user ID
     */
    public void removeAllFriendshipsOfUser(Long userId) {
        new ArrayList<>(getFriendIds(userId))
                .forEach(friendId -> removeFriendShip(userId, friendId));
    }
}