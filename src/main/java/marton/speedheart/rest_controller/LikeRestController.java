package marton.speedheart.rest_controller;

import marton.speedheart.domain.Like;
import marton.speedheart.domain.User;
import marton.speedheart.dto.LikeDTO;
import marton.speedheart.repositories.LikeDAO;
import marton.speedheart.repositories.UserDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("rest/like")
public class LikeRestController {

    // Logger instance
    private static final Logger logger = LoggerFactory.getLogger(LikeRestController.class);

    @Autowired
    private
    LikeDAO likeDAO;

    @Autowired
    private
    UserDAO userDAO;

    @Autowired
    HttpServletRequest httpServletRequest;

    @GetMapping
    public List<LikeDTO> all() {
        logger.debug("Fetching all likes");
        List<LikeDTO> likes = getLikeDAO().allLikes();
        logger.debug("Returning {} likes", likes.size());
        return likes;
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody LikeDTO likeDTO) {
        logger.debug("Creating like with details: {}", likeDTO);

        // Resolve the giver and receiver User objects from their IDs
        User giver = getUserDAO().findUserById(likeDTO.getGiverId());
        User receiver = getUserDAO().findUserById(likeDTO.getReceiverId());

        if (giver == null || receiver == null) {
            logger.error("Giver or receiver not found: giverId={}, receiverId={}", likeDTO.getGiverId(), likeDTO.getReceiverId());
            return ResponseEntity.badRequest().build();
        }

        // Check if giver equals receiver
        if (Objects.equals(giver.getId(), receiver.getId())) {
            logger.warn("Users cannot like themselves (user id = {})", giver.getId());
            return ResponseEntity.status(405).build(); //405 for Method Not Allowed http status
        }

        // Check if the like already exists
        if (getLikeDAO().likeExists(likeDTO.getGiverId(), likeDTO.getReceiverId())) {
            logger.warn("Like already exists from giverId={} to receiverId={}", likeDTO.getGiverId(), likeDTO.getReceiverId());
            return ResponseEntity.status(409).build(); //409 for Conflict http status
        }

        // Create and save the Like
        Like like = new Like(likeDTO, giver, receiver);
        Long id = getLikeDAO().createLike(like);

        URI location = URI.create(httpServletRequest.getRequestURI() + "/" + id);
        logger.debug("Like created with ID: {}", id);  // Log the ID of the created like
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/mutual/{giverId}/{receiverId}")
    public boolean isMutualLike(@PathVariable Long giverId, @PathVariable Long receiverId) {
        logger.debug("Checking mutual like between giverId: {} and receiverId: {}", giverId, receiverId);
        return getLikeDAO().isMutualLike(giverId, receiverId);
    }

    @GetMapping("/exists/{giverId}/{receiverId}")
    public boolean likeExists(@PathVariable Long giverId, @PathVariable Long receiverId) {
        logger.debug("Checking if like exists between giverId: {} and receiverId: {}", giverId, receiverId);
        return getLikeDAO().likeExists(giverId, receiverId);
    }

    public LikeDAO getLikeDAO() {
        return likeDAO;
    }

    public void setLikeDAO(LikeDAO likeDAO) {
        this.likeDAO = likeDAO;
    }

    public UserDAO getUserDAO() {
        return userDAO;
    }

    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
}
