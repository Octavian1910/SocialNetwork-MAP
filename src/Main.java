import domain.FriendShip;
import domain.User;
import domain.validator.FriendShipValidator;
import domain.validator.UserValidator;
import repository.Repository;
import repository.factory.RepositoryFactory;
import service.FriendShipService;
import service.NetworkService;
import service.UserService;
import ui.Console;

/**
 * Application entry point responsible for assembling components and injecting dependencies.
 */
public class Main {
    public static void main(String[] args) {
        Test.runAll();

        RepositoryFactory factory = new RepositoryFactory("src/resources/config.properties");

        UserValidator userValidator = new UserValidator();
        FriendShipValidator friendShipValidator = new FriendShipValidator();

        Repository<Integer, User> userRepository = factory.createUserRepository();
        Repository<String, FriendShip> friendshipRepository = factory.createFriendshipRepository();

        UserService userService = new UserService(userRepository, userValidator);
        FriendShipService friendshipService = new FriendShipService(friendshipRepository, userService, friendShipValidator);
        NetworkService networkService = new NetworkService(userService, friendshipService);

        Console console = new Console(userService, friendshipService, networkService);
        console.run();
    }
}