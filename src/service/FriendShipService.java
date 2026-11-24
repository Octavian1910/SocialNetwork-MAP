package service;

import domain.Friendship;
import domain.User;
import domain.validator.ValidationException;
import domain.validator.Validator;
import repository.FriendshipPagingRepository;
import repository.Repository;
import repository.RepositoryException;
import util.paging.Page;
import util.paging.Pageable;

import java.util.*;

/**
 * Service coordinating friendship relationships between users.
 */
public class FriendShipService {
    private final Repository<String, Friendship> friendshipRepository;
    private final UserService userService;
    private final Validator<Friendship> validator;

    public FriendShipService(Repository<String, Friendship> repository,
                             UserService userService,
                             Validator<Friendship> validator) {
        this.friendshipRepository = repository;
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

        friendshipRepository.add(friendShip);
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
        Optional<Friendship> removed = friendshipRepository.remove(friendShip.getId());
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
        return friendshipRepository.findAll();
    }

    /**
     * Returns all neighbor IDs connected to a given user (adjacency list representation).
     *
     * @param userId the user ID
     * @return collection of friend IDs
     */
    public Collection<Long> getFriendIds(Long userId) {
        return friendshipRepository.findAll().stream()
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


    /**
     * Retrieves a single page of friends of a user, ordered by friend ID.
     * For a {@link FriendshipPagingRepository} (Database storage) the paging is done by the repository,
     * in SQL; for the other storage types the page is cut out of the full friend list.
     *
     * @param userId   the ID of the user whose friends are requested
     * @param pageable the requested page (zero-based page number and page size), must not be null
     * @return a {@link Page} holding the friends on that page and the total number of friends;
     * the friend list is empty if the user has no friends or the requested page lies after the last one
     * @throws IllegalArgumentException if pageable is null
     * @throws ServiceException         if the user does not exist
     */
    public Page<User> getFriendsOnPage(Long userId, Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentException("Page cannot be null!");
        }

        userService.findUserById(userId);
        if (friendshipRepository instanceof FriendshipPagingRepository friendshipPagingRepository) {
            return friendshipPagingRepository.findFriendsOnPage(userId, pageable);
        }

        // altfel (FILE / MEMORY)
        List<Long> friendIds = new ArrayList<>(getFriendIds(userId));
        Collections.sort(friendIds);
        int totalCount = friendIds.size();

        int offset = pageable.getPageNumber() * pageable.getPageSize();
        int limit = pageable.getPageSize();

        List<User> pageContent = new ArrayList<>();
        for (int i = offset; i < offset + limit && i < totalCount; i++) {
            pageContent.add(userService.findUserById(friendIds.get(i)));
        }
        return new Page<>(pageContent, totalCount);
    }
}