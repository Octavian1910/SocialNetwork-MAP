package repository;
import domain.Friendship;
import domain.User;
import util.paging.Page;
import util.paging.Pageable;

public interface FriendshipPagingRepository extends Repository<String, Friendship> {

    /**
     * Retrieves a single page of friends of the given user, ordered by friend identifier.
     *
     * @param userId   identifier of the user whose friends are requested, must not be null
     * @param pageable the requested page (zero-based page number and page size), must not be null
     * @return a {@link Page} holding the friends on that page and the total number of friends of the user;
     * the element list is empty if the requested page lies after the last one
     * @throws IllegalArgumentException if userId or pageable is null
     */
    Page<User> findFriendsOnPage(Long userId, Pageable pageable);
}
