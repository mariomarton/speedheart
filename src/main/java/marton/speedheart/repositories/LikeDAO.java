package marton.speedheart.repositories;

import marton.speedheart.domain.Like;
import marton.speedheart.dto.LikeDTO;

import java.util.List;

public interface LikeDAO {
    List<LikeDTO> allLikes();
    Long createLike(Like entity);
    boolean likeExists(Long giverId, Long receiverId);
    boolean isMutualLike(Long giverId, Long receiverId);
}
