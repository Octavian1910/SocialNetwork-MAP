package repository;

import domain.User;
import domain.validator.Validator;

import java.nio.file.OpenOption;
import java.sql.*;
import java.util.*;

/**
 * Database repository implementation for User entities.
 */
public class UserDbRepository implements Repository<Long, User> {

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