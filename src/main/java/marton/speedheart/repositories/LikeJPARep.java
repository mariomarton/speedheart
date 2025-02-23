package marton.speedheart.repositories;

import marton.speedheart.domain.Like;
import marton.speedheart.dto.LikeDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import jakarta.persistence.TypedQuery;
import java.util.List;

@Service  // Marks this class as a Spring-managed service
public class LikeJPARep implements LikeDAO {

    @PersistenceContext
    private EntityManager em;  // Injecting the JPA EntityManager to interact with the database

    @Override
    @Transactional
    public List<LikeDTO> allLikes() {
        TypedQuery<Like> q = em.createNamedQuery("Like.findAll", Like.class);
        List<Like> Likes = q.getResultList();

        // Convert Like entities to LikeDTOs
        return Likes.stream()
                .map(Like::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public Long createLike(Like entity) {
        em.persist(entity);
        em.flush();

        return entity.getId();
    }

    @Override
    @Transactional
    public boolean likeExists(Long giverId, Long receiverId) {
        // Check if there's a like from giverId to receiverId
        return em.createQuery(
                        "SELECT COUNT(l) FROM Like l WHERE l.giver.id = :giverId AND l.receiver.id = :receiverId", Long.class)
                .setParameter("receiverId", receiverId)
                .setParameter("giverId", giverId)
                .getSingleResult() > 0;
    }

    @Override
    @Transactional
    public boolean isMutualLike(Long giverId, Long receiverId) {
        // Check if there's a like from receiverId to giverId and vice versa
        return likeExists(giverId, receiverId) && likeExists(receiverId, giverId);
    }
}
