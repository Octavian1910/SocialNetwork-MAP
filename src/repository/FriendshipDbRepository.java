package repository;

import domain.Friendship;
import domain.User;
import util.paging.Page;
import util.paging.Pageable;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Repository for friendship database persistence using JDBC.
 */
public class FriendshipDbRepository implements FriendshipPagingRepository {
    private final String url;
    private final String username;
    private final String password;

    /**
     * Constructs a new FriendshipDbRepository.
     *
     * @param url      the database URL
     * @param username the database username
     * @param password the database password
     */
    public FriendshipDbRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * Retrieves a paginated list of friends for a specific user using a single SQL JOIN query.
     *
     * @param userId   the ID of the user whose friends are being retrieved
     * @param pageable the pagination parameters (page number and page size)
     * @return a Page object containing the list of friend User entities and the total count
     */
    @Override
    public Page<User> findFriendsOnPage(Long userId, Pageable pageable) {
        if (userId == null || pageable == null) {
            throw new IllegalArgumentException("userId or pageable is null");
        }

        int offset = pageable.getPageNumber() * pageable.getPageSize();
        int limit = pageable.getPageSize();

        String sql = "SELECT u.id,u.first_name,u.last_name " +
                "FROM users u " +
                "JOIN friendships f " +
                "ON (u.id = f.id1 OR u.id = f.id2) " +
                "WHERE (f.id1 = ? OR f.id2 = ?) AND u.id != ? " +
                "ORDER BY u.id LIMIT ? OFFSET ?";

        String sqlCount = "SELECT count(*) FROM friendships WHERE id1 = ? OR id2 = ?";

        List<User> friends = new ArrayList<>();
        int totalCount = 0;

        try (Connection connection = DriverManager.getConnection(url, username, password)) {

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, userId);
                statement.setLong(2, userId);
                statement.setLong(3, userId);
                statement.setInt(4, limit);
                statement.setInt(5, offset);

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        Long id = resultSet.getLong("id");
                        String firstName = resultSet.getString("first_name");
                        String lastName = resultSet.getString("last_name");

                        User user = new User(id, firstName, lastName);
                        friends.add(user);
                    }
                }
            }

            try (PreparedStatement countStatement = connection.prepareStatement(sqlCount)) {
                countStatement.setLong(1, userId);
                countStatement.setLong(2, userId);

                try (ResultSet countResultSet = countStatement.executeQuery()) {
                    if (countResultSet.next()) {
                        totalCount = countResultSet.getInt(1);
                    }
                }
            }

        } catch (SQLException e) {
            throw new RepositoryException("Database error retrieving users on page "
                    + pageable.getPageNumber() + ": " + e.getMessage(), e);
        }

        return new Page<>(friends, totalCount);
    }

    /**
     * Parses a composite friendship ID string into an ordered array of two long IDs.
     *
     * @param id the composite ID string (expected format 'id1-id2')
     * @return a sorted long array of length 2
     */
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

    /**
     * Adds a new friendship to the database.
     *
     * @param entity the friendship entity to add
     */
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

    /**
     * Removes a friendship by its composite ID.
     *
     * @param id the composite ID string of the friendship to remove
     * @return an Optional containing the removed friendship, or Optional.empty() if not found
     */
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

    /**
     * Finds a friendship by its composite ID.
     *
     * @param id the composite ID string
     * @return an Optional containing the friendship if found, or Optional.empty() otherwise
     */
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

    /**
     * Retrieves all friendships from the database.
     *
     * @return a collection of all stored friendships
     */
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

    /**
     * Updates an existing friendship in the database.
     *
     * @param entity the friendship entity with updated data
     * @return Optional.empty() if successful, or an Optional containing the entity if it wasn't found
     */
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