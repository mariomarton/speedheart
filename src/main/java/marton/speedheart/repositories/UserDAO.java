package marton.speedheart.repositories;

import marton.speedheart.domain.User;
import marton.speedheart.dto.UserDTO;

import java.util.List;

public interface UserDAO {
    List<UserDTO> allUsers();
    Long createUser(User entity);
    User findUserById(Long id);

    /**
     * Finds the next user based on the given user ID.
     * @param currentUserId The ID of the current user.
     * @return The next UserDTO, or null if no next user exists.
     */
    UserDTO findNextUser(Long currentUserId);
    UserDTO findUserWithLowestId();
    UserDTO findUserWithHighestId();

    /**
     * Deletes a user by ID.
     * @param id The ID of the user to delete.
     * @return True if the deletion was successful, false otherwise.
     */
    boolean deleteUserById(Long id);

    /**
     * Updates the email of a user.
     * @param id The ID of the user whose email should be updated.
     * @param newEmail The new email to set.
     * @return True if the update was successful, false otherwise.
     */
    boolean updateUserEmail(Long id, String newEmail);
}
