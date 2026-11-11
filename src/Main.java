import domain.Friendship;
import domain.User;
import domain.validator.FriendshipValidator;
import domain.validator.UserValidator;
import repository.Repository;
import repository.factory.RepositoryFactory;
import service.FriendShipService;
import service.NetworkService;
import service.UserService;

/**
 * Application entry point responsible for assembling components and injecting dependencies.
 */
public class Main {
    public static void main(String[] args) {
        Test.runAll();

        RepositoryFactory factory = new RepositoryFactory("src/resources/config.properties");

        UserValidator userValidator = new UserValidator();
        FriendshipValidator friendShipValidator = new FriendshipValidator();

        Repository<Long, User> userRepository = factory.createUserRepository();
        Repository<String, Friendship> friendshipRepository = factory.createFriendshipRepository();

        UserService userService = new UserService(userRepository, userValidator);
        FriendShipService friendshipService = new FriendShipService(friendshipRepository, userService, friendShipValidator);
        NetworkService networkService = new NetworkService(userService, friendshipService);

        Console console = new Console(userService, friendshipService, networkService, factory.getRepositoryType());
        console.run();
    }
}