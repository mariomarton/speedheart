package marton.speedheart;

import marton.speedheart.domain.User;
import marton.speedheart.dto.UserDTO;
import marton.speedheart.rest_controller.UserRestController;
import marton.speedheart.repositories.UserDAO;
import marton.speedheart.validation.UserValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserRestControllerTests {

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private UserRestController userRestController;

    @Mock
    private HttpServletRequest httpServletRequest;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock HttpServletRequest behavior
        when(httpServletRequest.getRequestURI()).thenReturn("/rest/user/create");

        user1 = new User();
        user1.setId(1L);
        user1.setFirstName("User1");

        user2 = new User();
        user2.setId(2L);
        user2.setFirstName("User2");
    }

    @Test
    void all_shouldReturnAllUsers() {
        List<UserDTO> mockUsers = Arrays.asList(user1.toDTO(), user2.toDTO());
        when(userDAO.allUsers()).thenReturn(mockUsers);

        List<UserDTO> result = userRestController.all();

        assertThat(result).hasSize(2);
        verify(userDAO, times(1)).allUsers();
    }

    @Test
    void create_shouldPersistUser() throws Exception {
        UserValidation userValidation = new UserValidation();
        userValidation.setFirstName("User3");
        MultipartFile photo = mock(MultipartFile.class);
        when(photo.isEmpty()).thenReturn(true);

        when(userDAO.createUser(any(User.class))).thenReturn(3L);

        ResponseEntity<Void> response = userRestController.create(userValidation, photo);

        assertThat(response.getStatusCodeValue()).isEqualTo(201);
        verify(userDAO, times(1)).createUser(any(User.class));
    }

    @Test
    void getUserById_shouldReturnCorrectUser() {
        when(userDAO.findUserById(1L)).thenReturn(user1);

        ResponseEntity<UserDTO> response = userRestController.getUserById(1L);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().getFirstName()).isEqualTo("User1");
        verify(userDAO, times(1)).findUserById(1L);
    }

    @Test
    void getUserById_shouldReturnNotFoundIfUserDoesNotExist() {
        // Simulate userDAO.findUserById returning null for ID 999
        when(userDAO.findUserById(999L)).thenReturn(null);

        // Call the method
        ResponseEntity<UserDTO> response = userRestController.getUserById(999L);

        // Assert the response is 404 NOT FOUND
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Verify that the DAO method was called exactly once
        verify(userDAO, times(1)).findUserById(999L);
    }



    @Test
    void getNextUser_shouldReturnNextUser() {
        when(userDAO.findNextUser(1L)).thenReturn(user2.toDTO());

        ResponseEntity<UserDTO> response = userRestController.getNextUser(1L);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().getFirstName()).isEqualTo("User2");
        verify(userDAO, times(1)).findNextUser(1L);
    }

    @Test
    void getNextUser_shouldReturnNoContentIfNoNextUserExists() {
        when(userDAO.findNextUser(1L)).thenReturn(null);

        ResponseEntity<UserDTO> response = userRestController.getNextUser(1L);

        assertThat(response.getStatusCodeValue()).isEqualTo(204);
        verify(userDAO, times(1)).findNextUser(1L);
    }

    @Test
    void getUserWithLowestId_shouldReturnCorrectUser() {
        when(userDAO.findUserWithLowestId()).thenReturn(user1.toDTO());

        ResponseEntity<UserDTO> response = userRestController.getUserWithLowestId();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().getFirstName()).isEqualTo("User1");
        verify(userDAO, times(1)).findUserWithLowestId();
    }

    @Test
    void getUserWithHighestId_shouldReturnCorrectUser() {
        when(userDAO.findUserWithHighestId()).thenReturn(user2.toDTO());

        ResponseEntity<UserDTO> response = userRestController.getUserWithHighestId();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().getFirstName()).isEqualTo("User2");
        verify(userDAO, times(1)).findUserWithHighestId();
    }

    @Test
    void deleteUser_shouldDeleteUser() {
        when(userDAO.deleteUserById(1L)).thenReturn(true);

        ResponseEntity<Void> response = userRestController.deleteUser(1L);

        assertThat(response.getStatusCodeValue()).isEqualTo(204);
        verify(userDAO, times(1)).deleteUserById(1L);
    }

    @Test
    void deleteUser_shouldReturnNotFoundIfUserDoesNotExist() {
        when(userDAO.deleteUserById(999L)).thenReturn(false);

        ResponseEntity<Void> response = userRestController.deleteUser(999L);

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
        verify(userDAO, times(1)).deleteUserById(999L);
    }

    @Test
    void updateUserEmail_shouldUpdateEmail() {
        when(userDAO.updateUserEmail(1L, "new.email@example.com")).thenReturn(true);

        ResponseEntity<Void> response = userRestController.updateUserEmail(1L, "new.email@example.com");

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        verify(userDAO, times(1)).updateUserEmail(1L, "new.email@example.com");
    }

    @Test
    void updateUserEmail_shouldReturnNotFoundIfUserDoesNotExist() {
        when(userDAO.updateUserEmail(999L, "new.email@example.com")).thenReturn(false);

        ResponseEntity<Void> response = userRestController.updateUserEmail(999L, "new.email@example.com");

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
        verify(userDAO, times(1)).updateUserEmail(999L, "new.email@example.com");
    }
}
