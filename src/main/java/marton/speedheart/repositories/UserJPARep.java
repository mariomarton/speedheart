package marton.speedheart.repositories;

import marton.speedheart.domain.User;
import marton.speedheart.dto.UserDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import jakarta.persistence.TypedQuery;
import java.util.List;

@Service  // Marks this class as a Spring-managed service
public class UserJPARep implements UserDAO {

    @PersistenceContext
    private EntityManager em;  // Injecting the JPA EntityManager to interact with the database

    @Override
    @Transactional
    public List<UserDTO> allUsers() {
        TypedQuery<User> q = em.createNamedQuery("User.findAll", User.class);
        List<User> users = q.getResultList();

        // Convert User entities to UserDTOs
        return users.stream()
                .map(User::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public Long createUser(User entity) {
        em.persist(entity);
        em.flush();

        return entity.getId();  // Return the generated ID
    }

    @Override
    @Transactional
    public User findUserById(Long id) {
        return em.find(User.class, id);  // JPA's built-in method for finding entities by primary key
    }

    @Transactional
    public UserDTO findNextUser(Long currentUserId) {
        TypedQuery<User> q = em.createQuery(
                "SELECT u FROM User u WHERE u.id > :currentUserId ORDER BY u.id ASC", User.class
        );
        q.setParameter("currentUserId", currentUserId);
        q.setMaxResults(1);

        List<User> result = q.getResultList();

        // Return the next user if found, otherwise return null
        return result.isEmpty() ? null : result.get(0).toDTO();
    }

    @Override
    @Transactional
    public UserDTO findUserWithLowestId() {
        // Query to get the user with the lowest ID (first registered)
        TypedQuery<User> query = em.createQuery("SELECT u FROM User u ORDER BY u.id ASC", User.class);
        query.setMaxResults(1);  // Get only the first result (the user with the lowest ID)
        User user = query.getSingleResult();
        return user != null ? user.toDTO() : null;  // Convert User to UserDTO and return
    }

    @Override
    @Transactional
    public UserDTO findUserWithHighestId() {
        // Query to get the user with the highest ID
        TypedQuery<User> query = em.createQuery("SELECT u FROM User u ORDER BY u.id DESC", User.class);
        query.setMaxResults(1);  // Get only the first result (the user with the highest ID)
        User user = query.getSingleResult();
        return user != null ? user.toDTO() : null;  // Convert User to UserDTO and return
    }

    @Override
    @Transactional
    public boolean deleteUserById(Long id) {
        User user = em.find(User.class, id);
        if (user != null) {
            em.remove(user);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean updateUserEmail(Long id, String newEmail) {
        // Delete associated likes
        em.createQuery("DELETE FROM Like l WHERE l.giver.id = :userId OR l.receiver.id = :userId")
                .setParameter("userId", id)
                .executeUpdate();

        // Now delete the user
        User user = em.find(User.class, id);
        if (user != null) {
            em.remove(user);
            return true;
        }
        return false;
    }
}
