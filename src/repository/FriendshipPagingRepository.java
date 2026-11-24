package repository;
import domain.Friendship;
import domain.User;
import util.paging.Page;
import util.paging.Pageable;

public interface FriendshipPagingRepository extends Repository<String, Friendship> {
    Page<User> findFriendsOnPage(Long userId, Pageable pageable);
}
