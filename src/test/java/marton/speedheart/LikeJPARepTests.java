package marton.speedheart;

import marton.speedheart.domain.Like;
import marton.speedheart.domain.User;
import marton.speedheart.dto.LikeDTO;
import marton.speedheart.repositories.LikeJPARep;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
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
class LikeJPARepTests {

    @Autowired
    private LikeJPARep likeJPARep;

    @PersistenceContext
    private EntityManager em;

    private User giver;
    private User receiver1;
    private User receiver2;
    private Like like1;
    private Like like2;
    private Like like3;

    @BeforeEach
    void setUp() {
        // Prepare test data for each test
        giver = new User();
        giver.setFirstName("giver");

        receiver1 = new User();
        receiver1.setFirstName("receiver1");

        receiver2 = new User();
        receiver2.setFirstName("receiver2");

        em.persist(giver);
        em.persist(receiver1);
        em.persist(receiver2);
        em.flush();

        like1 = new Like();
        like1.setGiver(giver);
        like1.setReceiver(receiver1);

        like2 = new Like();
        like2.setGiver(receiver1);
        like2.setReceiver(receiver2);

        like3 = new Like();
        like3.setGiver(receiver2);
        like3.setReceiver(receiver1);
    }

    @Test
    void createLike_shouldPersistLike() {
        Long likeId = likeJPARep.createLike(like1);

        // Ensure the Like was persisted and has an ID
        assertThat(likeId).isNotNull();
    }

    @Test
    void allLikes_shouldReturnAllLikes() {
        likeJPARep.createLike(like1);
        likeJPARep.createLike(like2);
        likeJPARep.createLike(like3);

        List<LikeDTO> likes = likeJPARep.allLikes();

        // Ensure the method returns the correct number of likes
        assertThat(likes).hasSize(3);
    }

    @Test
    void likeExists_shouldReturnTrueIfLikeExists() {
        likeJPARep.createLike(like1);

        boolean exists = likeJPARep.likeExists(giver.getId(), receiver1.getId());

        // Ensure that the like exists
        assertThat(exists).isTrue();
    }

    @Test
    void likeExists_shouldReturnFalseIfLikeDoesNotExist() {
        boolean exists = likeJPARep.likeExists(giver.getId(), receiver2.getId());

        // Ensure that the like does not exist
        assertThat(exists).isFalse();
    }

    @Test
    void isMutualLike_shouldReturnTrueIfMutualLikeExists() {
        likeJPARep.createLike(like2);
        likeJPARep.createLike(like3);

        boolean mutualLike = likeJPARep.isMutualLike(receiver1.getId(), receiver2.getId());

        // Ensure that the method detects mutual likes
        assertThat(mutualLike).isTrue();
    }

    @Test
    void isMutualLike_shouldReturnFalseIfNoMutualLike() {
        likeJPARep.createLike(like1);

        boolean mutualLike = likeJPARep.isMutualLike(giver.getId(), receiver2.getId());

        // Ensure that the method detects no mutual like
        assertThat(mutualLike).isFalse();
    }
}
