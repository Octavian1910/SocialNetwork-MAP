package repository.factory;

import domain.FriendShip;
import domain.User;
import repository.*;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

/**
 * Factory class responsible for initializing and providing repository instances
 * using functional {@link Supplier} mappings driven by an external properties configuration.
 */
public class RepositoryFactory {
    private final RepositoryType repositoryType;
    private final Properties properties = new Properties();
    private final Map<RepositoryType, Supplier<Repository<Integer, User>>> userRepoSuppliers = new HashMap<>();
    private final Map<RepositoryType, Supplier<Repository<String, FriendShip>>> friendShipRepoSuppliers = new HashMap<>();

    /**
     * Constructs a RepositoryFactory by loading database and file configurations.
     *
     * @param configFilePath relative or absolute path to configuration file
     * @throws RepositoryException if config file cannot be read or repository type is invalid
     */
    public RepositoryFactory(String configFilePath) {
        try (FileReader reader = new FileReader(configFilePath)) {
            properties.load(reader);
        } catch (IOException e) {
            throw new RepositoryException("Could not read config file: " + configFilePath, e);
        }
        String typeStr = properties.getProperty("repository.type");
        if (typeStr == null || typeStr.trim().isEmpty()) {
            throw new RepositoryException("Key repository.type is missing from config file!");
        }
        try {
            this.repositoryType = RepositoryType.valueOf(typeStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RepositoryException("Unknown repository type: " + typeStr, e);
        }
        initSuppliers();
    }

    /**
     * Maps repository types to their corresponding functional suppliers.
     */
    void initSuppliers() {
        userRepoSuppliers.put(RepositoryType.MEMORY, InMemoryRepository::new);
        userRepoSuppliers.put(RepositoryType.DATABASE, () -> new UserDbRepository(
                properties.getProperty("db.url"),
                properties.getProperty("db.user"),
                properties.getProperty("db.password")
        ));
        userRepoSuppliers.put(RepositoryType.FILE, () -> {
            String userFile = properties.getProperty("file.users");
            if (userFile == null || userFile.trim().isEmpty()) {
                throw new RepositoryException("Missing configuration property: 'file.users'");
            }
            return new UserFileRepository(userFile);
        });

        friendShipRepoSuppliers.put(RepositoryType.MEMORY, InMemoryRepository::new);
        friendShipRepoSuppliers.put(RepositoryType.DATABASE, () -> new FriendShipDbRepository(
                properties.getProperty("db.url"),
                properties.getProperty("db.user"),
                properties.getProperty("db.password")
        ));
        friendShipRepoSuppliers.put(RepositoryType.FILE, () -> {
            String friendshipFile = properties.getProperty("file.friendships");
            if (friendshipFile == null || friendshipFile.trim().isEmpty()) {
                throw new RepositoryException("Missing configuration property: 'file.friendships'");
            }
            return new FriendShipFileRepository(friendshipFile);
        });
    }

    /**
     * Instantiates and returns a User repository according to current configuration.
     *
     * @return configured user repository instance
     * @throws RepositoryException if repository type is unsupported
     */
    public Repository<Integer, User> createUserRepository() {
        Supplier<Repository<Integer, User>> supplier = userRepoSuppliers.get(repositoryType);
        if (supplier == null) {
            throw new RepositoryException("Unsupported repository type: " + repositoryType);
        }
        return supplier.get();
    }

    /**
     * Instantiates and returns a Friendship repository according to current configuration.
     *
     * @return configured friendship repository instance
     * @throws RepositoryException if repository type is unsupported
     */
    public Repository<String, FriendShip> createFriendshipRepository() {
        Supplier<Repository<String, FriendShip>> supplier = friendShipRepoSuppliers.get(repositoryType);
        if (supplier == null) {
            throw new RepositoryException("Unsupported repository type: " + repositoryType);
        }
        return supplier.get();
    }
}