package repository;

import domain.User;
import util.paging.Page;
import util.paging.Pageable;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Database repository implementation for User entities.
 */
public class UserDbRepository implements PagingRepository<Long, User> {

    static {
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("PostgreSQL driver loaded successfully!");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "PostgreSQL JDBC Driver not found!", e
            );
        }
    }


    private final String url;
    private final String username;
    private final String password;

    /**
     * Constructs a new UserDbRepository.
     *
     * @param url      database URL
     * @param username database user
     * @param password database password
     */
    public UserDbRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * Retrieves a single page of users, ordered by ID. The page content and the total
     * number of users are read with two statements executed on the same connection.
     *
     * @param pageable the requested page (zero-based page number and page size), must not be null
     * @return a {@link Page} holding the users on that page and the total number of users;
     * the user list is empty if the requested page lies after the last one
     * @throws IllegalArgumentException if pageable is null
     * @throws RepositoryException      if the database cannot be queried
     */
    @Override
    public Page<User> findAllOnPage(Pageable pageable) {

        if (pageable == null) {
            throw new IllegalArgumentException("pageable cannot be null!");
        }

        int offset = pageable.getPageNumber() * pageable.getPageSize();
        int limit = pageable.getPageSize();

        String sql = "SELECT id, first_name, last_name FROM users ORDER BY id LIMIT ? OFFSET ?";
        String sqlCount = "SELECT count(*) FROM users";

        List<User> users = new ArrayList<>();
        int totalCount = 0;

        try (Connection connection = DriverManager.getConnection(url, username, password)) {

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, limit);
                statement.setInt(2, offset);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        Long id = resultSet.getLong("id");
                        String firstName = resultSet.getString("first_name");
                        String lastName = resultSet.getString("last_name");

                        User user = new User(id, firstName, lastName);
                        users.add(user);
                    }
                }
            }

            try (PreparedStatement countStatement = connection.prepareStatement(sqlCount);
                 ResultSet countResultSet = countStatement.executeQuery()) {
                if (countResultSet.next()) {
                    totalCount = countResultSet.getInt(1);
                }
            }

        } catch (SQLException e) {
            throw new RepositoryException("Database error retrieving users on page " + pageable.getPageNumber() + ": " + e.getMessage(), e);
        }
        return new Page<>(users, totalCount);
    }

    @Override
    public void add(User entity) {
        if (entity == null) {
            throw new IllegalArgumentException("User must not be null!");
        }

        String sql = "INSERT INTO users (first_name, last_name) VALUES (?, ?)";


        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, entity.getFirstName());
            statement.setString(2, entity.getLastName());
            statement.executeUpdate();


            try (ResultSet generatedKeys = statement.getGeneratedKeys();) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getLong(1));
                }
            }

        } catch (SQLException e) {
            throw new RepositoryException("Database error saving user: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null!");
        }

        String sql = "SELECT id, first_name, last_name FROM users WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String firstName = resultSet.getString("first_name");
                    String lastName = resultSet.getString("last_name");
                    User user = new User(id, firstName, lastName);
                    return Optional.of(user);
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RepositoryException("Database error finding user with id " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Collection<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, first_name, last_name FROM users";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");

                User user = new User(id, firstName, lastName);
                users.add(user);
            }

            return users;
        } catch (SQLException e) {
            throw new RepositoryException("Databasae error retrieving all users: " + e.getMessage(), e);
        }

    }

    @Override
    public Optional<User> remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID must not be null!");
        }

        Optional<User> userToDelete = findById(id);
        if (userToDelete.isEmpty()) {
            return Optional.empty();
        }

        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
            return userToDelete;
        } catch (SQLException e) {
            throw new RepositoryException("Database error removing user with ID " + id + ": " + e.getMessage(), e);
        }
    }


    @Override
    public Optional<User> update(User entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity must not be null!");
        }
        if (entity.getId() == null) {
            throw new IllegalArgumentException("Entity ID must not be null!");
        }

        String sql = "UPDATE users SET first_name = ?, last_name = ? WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, entity.getFirstName());
            statement.setString(2, entity.getLastName());
            statement.setLong(3, entity.getId());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 0) {
                return Optional.of(entity);
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RepositoryException("Database error updating user: " + e.getMessage(), e);
        }
    }
}