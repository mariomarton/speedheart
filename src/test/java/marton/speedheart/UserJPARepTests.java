package marton.speedheart;

import marton.speedheart.domain.User;
import marton.speedheart.dto.UserDTO;
import marton.speedheart.repositories.UserJPARep;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")  // Activates the "test" profile to use the test-specific configuration
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)  // Ensures no use of production DB
@ExtendWith(SpringExtension.class)
@Transactional
class UserJPARepTests {

    @Autowired
    private UserJPARep userJPARep;

    @PersistenceContext
    private EntityManager em;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        // Prepare test data for each test
        user1 = new User();
        user1.setFirstName("User1");

        user2 = new User();
        user2.setFirstName("User2");

        user3 = new User();
        user3.setFirstName("User3");

        em.persist(user1);
        em.persist(user2);
        em.persist(user3);
        em.flush();
    }

    @Test
    void createUser_shouldPersistUser() {
        Long userId = userJPARep.createUser(user1);

        // Ensure the user was persisted and has an ID
        assertThat(userId).isNotNull();
    }

    @Test
    void allUsers_shouldReturnAllUsers() {
        List<UserDTO> users = userJPARep.allUsers();

        // Ensure the method returns the correct number of users
        assertThat(users).hasSize(3);
    }

    @Test
    void findUserById_shouldReturnCorrectUser() {
        User user = userJPARep.findUserById(user1.getId());

        // Ensure the user returned matches the expected user
        assertThat(user).isNotNull();
        assertThat(user.getFirstName()).isEqualTo(user1.getFirstName());
    }

    @Test
    void findNextUser_shouldReturnNextUser() {
        UserDTO nextUser = userJPARep.findNextUser(user1.getId());

        // Ensure the next user is correct
        assertThat(nextUser).isNotNull();
        assertThat(nextUser.getFirstName()).isEqualTo(user2.getFirstName());
    }

    @Test
    void findUserWithLowestId_shouldReturnCorrectUser() {
        UserDTO lowestIdUser = userJPARep.findUserWithLowestId();

        // Ensure the user with the lowest ID is returned
        assertThat(lowestIdUser).isNotNull();
        assertThat(lowestIdUser.getFirstName()).isEqualTo(user1.getFirstName());
    }

    @Test
    void findUserWithHighestId_shouldReturnCorrectUser() {
        UserDTO highestIdUser = userJPARep.findUserWithHighestId();

        // Ensure the user with the highest ID is returned
        assertThat(highestIdUser).isNotNull();
        assertThat(highestIdUser.getFirstName()).isEqualTo(user3.getFirstName());
    }

    @Test
    void deleteUserById_shouldDeleteUser() {
        boolean deleted = userJPARep.deleteUserById(user1.getId());

        // Ensure the user was deleted
        assertThat(deleted).isTrue();

        // Ensure the user is no longer in the database
        User deletedUser = em.find(User.class, user1.getId());
        assertThat(deletedUser).isNull();
    }

    @Test
    void deleteUserById_shouldReturnFalseIfUserDoesNotExist() {
        boolean deleted = userJPARep.deleteUserById(999L);  // ID that doesn't exist

        // Ensure the deletion failed
        assertThat(deleted).isFalse();
    }
}
