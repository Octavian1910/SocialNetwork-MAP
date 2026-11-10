package service;

import domain.FriendShip;
import domain.validator.ValidationException;
import domain.validator.Validator;
import repository.Repository;
import repository.RepositoryException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Service coordinating friendship relationships between users.
 */
public class FriendShipService {
    private final Repository<String, FriendShip> repository;
    private final UserService userService;
    private final Validator<FriendShip> validator;

    public FriendShipService(Repository<String, FriendShip> repository,
                             UserService userService,
                             Validator<FriendShip> validator) {
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
    public void addFriendShip(int firstUserId, int secondUserId) {
        FriendShip friendShip = new FriendShip(firstUserId, secondUserId);
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
    public void removeFriendShip(int firstUserId, int secondUserId) {
        FriendShip friendShip = new FriendShip(firstUserId, secondUserId);
        Optional<FriendShip> removed = repository.remove(friendShip.getId());
        if (removed.isEmpty()) {
            throw new ServiceException("Friendship between " + firstUserId + " and " + secondUserId + " does not exist!");
        }
    }

    /**
     * Retrieves all registered friendships.
     *
     * @return collection of friendships
     */
    public Collection<FriendShip> getAllFriendShips() {
        return repository.findAll();
    }

    /**
     * Returns all neighbor IDs connected to a given user (adjacency list representation).
     *
     * @param userId the user ID
     * @return collection of friend IDs
     */
    public Collection<Integer> getFriendIds(int userId) {
        return repository.findAll().stream()
                .filter(fs -> fs.getFirstUserId() == userId || fs.getSecondUserId() == userId)
                .map(fs -> fs.getFirstUserId() == userId ? fs.getSecondUserId() : fs.getFirstUserId())
                .toList();
    }

    /**
     * Removes all friendships associated with a specific user.
     *
     * @param userId the user ID
     */
    public void removeAllFriendshipsOfUser(int userId) {
        new ArrayList<>(getFriendIds(userId))
                .forEach(friendId -> removeFriendShip(userId, friendId));
    }
}