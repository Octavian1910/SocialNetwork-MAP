import domain.Friendship;
import domain.User;
import domain.criteria.UserCriteria;
import domain.validator.FriendshipValidator;
import domain.validator.UserValidator;
import domain.validator.ValidationException;
import domain.validator.Validator;
import repository.FriendshipFileRepository;
import repository.InMemoryRepository;
import repository.RepositoryException;
import repository.UserFileRepository;
import service.FriendShipService;
import service.NetworkService;
import service.ServiceException;
import service.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class Test {
    private static final String USERS_FILE = "test_users.txt";
    private static final String FRIENDSHIPS_FILE = "test_friendships.txt";
    private static final String CORRUPTED_USERS_FILE = "test_corrupted_users.txt";

    @FunctionalInterface
    interface Executable {
        void execute() throws Exception;
    }

    private static <T extends Throwable> void assertThrows(Class<T> expectedType, Executable executable, String expectedMessagePart) {
        try {
            executable.execute();
            assert false : "Expected exception: " + expectedType.getSimpleName();
        } catch (Throwable actual) {
            assert expectedType.isInstance(actual) : "Wrong exception type: " + actual.getClass();
            if (expectedMessagePart != null) {
                assert actual.getMessage() != null && actual.getMessage().contains(expectedMessagePart)
                        : "Message does not contain: " + expectedMessagePart;
            }
        }
    }

    public static void runAll() {
        testDomainAndValidators();
        testLambdaValidatorAndCriteria();
        testRepositories();
        testCorruptedFiles();
        testUserService();
        testFriendShipService();
        testNetworkService();
        System.out.println("All tests passed successfully!");
    }

    private static void resetFiles() {
        writeFile(USERS_FILE,
                "1;Ana;Pop",
                "2;Ion;Ionescu",
                "3;Maria;Marin",
                "4;Alex;Pop",
                "5;Elena;Matei",
                "6;George;Vasile",
                "7;Cristina;Stan",
                "8;Dan;Dumitru",
                "9;Radu;Enache",
                "10;Ioana;Micu"
        );
        writeFile(FRIENDSHIPS_FILE,
                "1;2;2026-10-19T10:30:00",
                "2;3;2026-10-19T11:00:00",
                "3;4;2026-10-19T11:30:00",
                "4;5;2026-10-19T12:00:00",
                "6;7;2026-10-20T09:00:00",
                "7;8;2026-10-20T09:30:00",
                "6;8;2026-10-20T10:00:00",
                "9;10;2026-10-20T10:30:00"
        );
        writeFile(CORRUPTED_USERS_FILE,
                "1;Mihai;Popescu",
                "2;Elena",
                "abc;Andrei;Radu",
                "4;Ioana;Dumitrescu",
                "cinci;Cristian;Stan",
                "6;George;Vasile;ExtraField",
                "7;Vlad;Munteanu"
        );
    }

    private static void testDomainAndValidators() {
        User u1 = new User(1L, "Ana", "Pop");
        User u2 = new User(1L, "AltNume", "AltPrenume");
        User u3 = new User(2L, "Ion", "Ionescu");
        assert u1.equals(u2) && !u1.equals(u3);
        assert u1.getId().equals(1L) && u1.getFirstName().equals("Ana") && u1.getLastName().equals("Pop");

        UserValidator userVal = new UserValidator();
        userVal.validate(u1);
        assertThrows(ValidationException.class, () -> userVal.validate(null), "null");
        assertThrows(ValidationException.class, () -> userVal.validate(new User(-1L, "ana", "")), "positive");
        assertThrows(ValidationException.class, () -> userVal.validate(new User(-1L, "ana", "")), "uppercase");
        assertThrows(ValidationException.class, () -> userVal.validate(new User(-1L, "ana", "")), "empty");

        Friendship f1 = new Friendship(1L, 2L);
        Friendship f2 = new Friendship(2L, 1L);
        assert f1.equals(f2) && f1.getId().equals("1-2") && f2.getId().equals("1-2");

        FriendshipValidator fsVal = new FriendshipValidator();
        fsVal.validate(f1);
        assertThrows(ValidationException.class, () -> fsVal.validate(new Friendship(1L, 1L)), "themselves");
        assertThrows(ValidationException.class, () -> fsVal.validate(new Friendship(-1L, 0L)), "positive");
    }

    private static void testLambdaValidatorAndCriteria() {
        resetFiles();
        UserFileRepository repo = new UserFileRepository(USERS_FILE);

        UserValidator baseValidator = new UserValidator();
        Validator<User> composedValidator = baseValidator.andThen(user -> {
            if (user.getLastName().length() < 3) {
                throw new ValidationException("Last name must have at least 3 characters!");
            }
        });
        User validUser = new User(20L, "Vasile", "Popescu");
        composedValidator.validate(validUser);
        User shortLastNameUser = new User(21L, "Vasile", "Po");
        assertThrows(ValidationException.class, () -> composedValidator.validate(shortLastNameUser), "at least 3 characters");

        Predicate<User> startsWithA = UserCriteria.firstNameStartsWith("A");
        Predicate<User> namePop = UserCriteria.hasLastName("Pop");
        Predicate<User> idAbove5 = UserCriteria.idGreaterThan(5L);

        List<User> matchedUsers = repo.findAll(startsWithA.and(namePop));
        assert matchedUsers.size() == 2;

        List<User> notPopAndHighId = repo.findAll(Predicate.not(namePop).and(idAbove5));
        assert notPopAndHighId.size() == 5;
    }

    private static void testRepositories() {
        resetFiles();
        InMemoryRepository<Long, User> memRepo = new InMemoryRepository<>();
        memRepo.add(new User(11L, "Radu", "Iliescu"));
        assert memRepo.findAll().size() == 1 && memRepo.findById(11L).isPresent();
        assertThrows(RepositoryException.class, () -> memRepo.add(new User(11L, "Alt", "Nume")), "already exists");
        assert memRepo.remove(999L).isEmpty();
        assert memRepo.remove(11L).isPresent() && memRepo.findAll().isEmpty();

        UserFileRepository userRepo = new UserFileRepository(USERS_FILE);
        assert userRepo.findAll().size() == 10 && userRepo.findById(1L).isPresent() && userRepo.findById(999L).isEmpty();
        assertThrows(RepositoryException.class, () -> userRepo.add(new User(1L, "Duplicat", "Nume")), "already exists");
        userRepo.add(new User(11L, "Paul", "Enache"));
        assert userRepo.findAll().size() == 11;

        FriendshipFileRepository fsRepo = new FriendshipFileRepository(FRIENDSHIPS_FILE);
        assert fsRepo.findAll().size() == 8;
        assert fsRepo.findById("2-1").isPresent() && fsRepo.findById("8-7").isPresent() && fsRepo.findById("10-1").isEmpty();
        assertThrows(RepositoryException.class, () -> fsRepo.add(new Friendship(2L, 1L)), "already exists");
        assert fsRepo.remove("999-1000").isEmpty();
        assert fsRepo.remove("10-9").isPresent() && fsRepo.findAll().size() == 7;
    }

    private static void testCorruptedFiles() {
        resetFiles();
        UserFileRepository corruptRepo = new UserFileRepository(CORRUPTED_USERS_FILE);
        assert corruptRepo.findAll().size() == 3;
        assert corruptRepo.findById(1L).isPresent();
        assert corruptRepo.findById(4L).isPresent();
        assert corruptRepo.findById(7L).isPresent();
        assert corruptRepo.findById(2L).isEmpty();
        assert corruptRepo.findById(6L).isEmpty();
    }

    private static void testUserService() {
        resetFiles();
        UserValidator validator = new UserValidator();
        UserFileRepository repo = new UserFileRepository(USERS_FILE);
        UserService service = new UserService(repo, validator);

        assert service.getAllUsers().size() == 10;
        assert service.findUserById(1L).getFirstName().equals("Ana");
        assertThrows(ServiceException.class, () -> service.findUserById(999L), "does not exist");
        assertThrows(ValidationException.class, () -> service.addUser(-1L, "Ion", "Pop"), "positive");
        assertThrows(RepositoryException.class, () -> service.addUser(1L, "Alt", "Nume"), "already exists");
        service.addUser(11L, "George", "Vasile");
        assert service.getAllUsers().size() == 11;
        assertThrows(ServiceException.class, () -> service.removeUser(999L), "cannot be deleted");
        service.removeUser(11L);
        assert service.getAllUsers().size() == 10;
    }

    private static void testFriendShipService() {
        resetFiles();
        UserValidator userVal = new UserValidator();
        FriendshipValidator fsVal = new FriendshipValidator();
        UserService userService = new UserService(new UserFileRepository(USERS_FILE), userVal);
        FriendshipFileRepository fsRepo = new FriendshipFileRepository(FRIENDSHIPS_FILE);
        FriendShipService service = new FriendShipService(fsRepo, userService, fsVal);

        assert service.getAllFriendShips().size() == 8;
        assert service.getFriendIds(2L).contains(1L) && service.getFriendIds(2L).contains(3L);
        assertThrows(ValidationException.class, () -> service.addFriendShip(1L, 1L), "themselves");
        assertThrows(ServiceException.class, () -> service.addFriendShip(1L, 999L), "does not exist");
        assertThrows(RepositoryException.class, () -> service.addFriendShip(2L, 1L), "already exists");
        assertThrows(ServiceException.class, () -> service.removeFriendShip(1L, 10L), "does not exist");
        service.addFriendShip(1L, 10L);
        assert service.getAllFriendShips().size() == 9 && service.getFriendIds(1L).contains(10L);
        service.removeFriendShip(10L, 1L);
        assert service.getAllFriendShips().size() == 8 && !service.getFriendIds(1L).contains(10L);
        service.removeAllFriendshipsOfUser(7L);
        assert service.getFriendIds(7L).isEmpty() && service.getAllFriendShips().size() == 6;
    }

    private static void testNetworkService() {
        resetFiles();
        UserValidator userVal = new UserValidator();
        FriendshipValidator fsVal = new FriendshipValidator();
        UserService userService = new UserService(new UserFileRepository(USERS_FILE), userVal);
        FriendShipService fsService = new FriendShipService(new FriendshipFileRepository(FRIENDSHIPS_FILE), userService, fsVal);
        NetworkService networkService = new NetworkService(userService, fsService);

        assert networkService.getNumberOfCommunities() == 3;
        Set<Long> comm1 = networkService.getCommunity(1L);
        assert comm1.size() == 5 && comm1.contains(1L) && comm1.contains(5L);
        Set<Long> comm2 = networkService.getCommunity(7L);
        assert comm2.size() == 3 && comm2.contains(6L) && comm2.contains(8L);
        Set<Long> comm3 = networkService.getCommunity(10L);
        assert comm3.size() == 2 && comm3.contains(9L) && comm3.contains(10L);
        assertThrows(ServiceException.class, () -> networkService.getCommunity(999L), "does not exist");

        Set<Long> mostSociable = networkService.getMostSociableCommunity();
        assert mostSociable.size() == 5 && mostSociable.contains(1L) && mostSociable.contains(5L);
        assertThrows(ServiceException.class, () -> networkService.removeUserAndFriendships(999L), "cannot be deleted");
        networkService.removeUserAndFriendships(3L);
        assert userService.getAllUsers().size() == 9;
        assert fsService.getFriendIds(2L).size() == 1 && fsService.getFriendIds(4L).size() == 1;
        assert networkService.getNumberOfCommunities() == 4;
        Set<Long> splitA = networkService.getCommunity(1L);
        Set<Long> splitB = networkService.getCommunity(5L);
        assert splitA.size() == 2 && splitB.size() == 2;
    }

    private static void writeFile(String path, String... lines) {
        try {
            Files.write(Path.of(path), List.of(lines));
        } catch (IOException e) {
            throw new RuntimeException("Eroare la scriere: " + path, e);
        }
    }
}