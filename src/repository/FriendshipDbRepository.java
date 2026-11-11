package repository;

import domain.Friendship;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Skeleton repository for friendship database persistence (to be implemented via JDBC in Lab 4).
 */
public class FriendshipDbRepository implements Repository<String, Friendship> {
    private final String url;
    private final String username;
    private final String password;

    public FriendshipDbRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    private long[] parseId(String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID must not be null!");
        }

        String[] parts = id.split("-", -1);
        if (parts.length != 2) {
            throw new RepositoryException("Invalid friendship ID format (expected 'id1-id2'): " + id);
        }

        try {
            long first = Long.parseLong(parts[0].trim());
            long second = Long.parseLong(parts[1].trim());
            return new long[]{Math.min(first, second), Math.max(first, second)};
        } catch (NumberFormatException e) {
            throw new RepositoryException("Invalid numeric IDs in friendship key: " + id, e);
        }
    }

    @Override
    public void add(Friendship entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Friendship must not be null!");
        }

        String sql = "INSERT INTO friendships (id1, id2, friends_from) VALUES(?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, entity.getFirstUserId());
            statement.setLong(2, entity.getSecondUserId());
            statement.setTimestamp(3, Timestamp.valueOf(entity.getDate()));

            statement.executeUpdate();
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new RepositoryException("Friendship already exists between users "
                        + entity.getFirstUserId() + " and " + entity.getSecondUserId() + "!");
            }
            throw new RepositoryException("Database error saving friendship: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Friendship> remove(String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID must not be null!");
        }

        Optional<Friendship> friendshipToDelete = findById(id);
        if (friendshipToDelete.isEmpty()) {
            return Optional.empty();
        }

        long[] ids = parseId(id);
        String sql = "DELETE FROM friendships WHERE id1 = ? AND id2 = ?";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, ids[0]);
            statement.setLong(2, ids[1]);

            statement.executeUpdate();

            return friendshipToDelete;
        } catch (SQLException e) {
            throw new RepositoryException("Database error removing friendship with ID " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Friendship> findById(String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID must not be null!");
        }

        long[] ids = parseId(id);

        String sql = "SELECT id1, id2, friends_from FROM friendships WHERE id1 = ? AND id2 = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, ids[0]);
            statement.setLong(2, ids[1]);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    long firsUserId = resultSet.getLong("id1");
                    long secondUserId = resultSet.getLong("id2");
                    LocalDateTime date = resultSet.getTimestamp("friends_from").toLocalDateTime();

                    Friendship friendship = new Friendship(firsUserId, secondUserId, date);
                    return Optional.of(friendship);
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RepositoryException("Database error finding friendship with ID " + id + ": " + e.getMessage(), e);
        }

    }

    @Override
    public Collection<Friendship> findAll() {
        List<Friendship> friendships = new ArrayList<>();
        String sql = "SELECT id1, id2, friends_from FROM friendships";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Long firstUserId = resultSet.getLong("id1");
                Long secondUserId = resultSet.getLong("id2");
                LocalDateTime date = resultSet.getTimestamp("friends_from").toLocalDateTime();
                Friendship friendship = new Friendship(firstUserId, secondUserId, date);
                friendships.add(friendship);
            }

            return friendships;

        } catch (SQLException e) {
            throw new RepositoryException("Database error retrieving all friendships: " + e.getMessage(), e);
        }

    }

    @Override
    public Optional<Friendship> update(Friendship entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Friendship must not be null!");
        }
        if (entity.getId() == null) {
            throw new IllegalArgumentException("Friendship ID must not be null!");
        }

        String sql = "UPDATE friendships SET friends_from = ? WHERE id1 = ? AND id2 = ?";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(entity.getDate()));
            statement.setLong(2, entity.getFirstUserId());
            statement.setLong(3, entity.getSecondUserId());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 0) {
                return Optional.of(entity);
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RepositoryException("Database error updating friendship: " + e.getMessage(), e);
        }


    }
}