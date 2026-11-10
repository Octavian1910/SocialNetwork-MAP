package repository;

import domain.User;

import java.util.Collection;
import java.util.Optional;

/**
 * Skeleton repository for database persistence (to be implemented via JDBC in Lab 4).
 */
public class UserDbRepository implements Repository<Integer, User> {
    private final String url;
    private final String username;
    private final String password;

    public UserDbRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public void add(User entity) {
        throw new UnsupportedOperationException("Database persistence will be implemented in Lab 4.");
    }

    @Override
    public Optional<User> remove(Integer id) {
        throw new UnsupportedOperationException("Database persistence will be implemented in Lab 4.");
    }

    @Override
    public Optional<User> findById(Integer id) {
        throw new UnsupportedOperationException("Database persistence will be implemented in Lab 4.");
    }

    @Override
    public Collection<User> findAll() {
        throw new UnsupportedOperationException("Database persistence will be implemented in Lab 4.");
    }

    @Override
    public Optional<User> update(User entity) {
        throw new UnsupportedOperationException("Database persistence will be implemented in Lab 4.");
    }
}