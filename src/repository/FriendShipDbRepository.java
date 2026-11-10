package repository;

import domain.FriendShip;

import java.util.Collection;
import java.util.Optional;

/**
 * Skeleton repository for friendship database persistence (to be implemented via JDBC in Lab 4).
 */
public class FriendShipDbRepository implements Repository<String, FriendShip> {
    private final String url;
    private final String username;
    private final String password;

    public FriendShipDbRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public void add(FriendShip entity) {
        throw new UnsupportedOperationException("Database persistence will be implemented in Lab 4.");
    }

    @Override
    public Optional<FriendShip> remove(String id) {
        throw new UnsupportedOperationException("Database persistence will be implemented in Lab 4.");
    }

    @Override
    public Optional<FriendShip> findById(String id) {
        throw new UnsupportedOperationException("Database persistence will be implemented in Lab 4.");
    }

    @Override
    public Collection<FriendShip> findAll() {
        throw new UnsupportedOperationException("Database persistence will be implemented in Lab 4.");
    }

    @Override
    public Optional<FriendShip> update(FriendShip entity) {
        throw new UnsupportedOperationException("Database persistence will be implemented in Lab 4.");
    }
}