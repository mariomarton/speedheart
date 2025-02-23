package marton.speedheart;

import marton.speedheart.domain.Like;
import marton.speedheart.domain.User;
import marton.speedheart.dto.LikeDTO;
import marton.speedheart.rest_controller.LikeRestController;
import marton.speedheart.repositories.LikeDAO;
import marton.speedheart.repositories.UserDAO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class LikeRestControllerTests {

    @InjectMocks
    private LikeRestController likeRestController;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private LikeDAO likeDAO;

    @Mock
    private UserDAO userDAO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(httpServletRequest.getRequestURI()).thenReturn("/rest/like");
    }

    @Test
    void all_shouldReturnAllLikes() {
        List<LikeDTO> mockLikes = Arrays.asList(
                new LikeDTO(1L, 2L),
                new LikeDTO(3L, 4L)
        );
        when(likeDAO.allLikes()).thenReturn(mockLikes);

        List<LikeDTO> result = likeRestController.all();

        assertThat(result).hasSize(2);
        verify(likeDAO, times(1)).allLikes();
    }

    @Test
    void create_shouldPersistLike() {
        LikeDTO likeDTO = new LikeDTO(1L, 2L);
        User giver = new User();
        giver.setId(1L);
        User receiver = new User();
        receiver.setId(2L);

        when(userDAO.findUserById(1L)).thenReturn(giver);
        when(userDAO.findUserById(2L)).thenReturn(receiver);
        when(likeDAO.likeExists(1L, 2L)).thenReturn(false);
        when(likeDAO.createLike(any(Like.class))).thenReturn(1L);

        ResponseEntity<Void> response = likeRestController.create(likeDTO);

        assertThat(response.getStatusCodeValue()).isEqualTo(201); // Created
        verify(likeDAO, times(1)).createLike(any(Like.class));
    }

    @Test
    void create_shouldReturnBadRequestIfGiverNotFound() {
        LikeDTO likeDTO = new LikeDTO(1L, 2L);
        when(userDAO.findUserById(1L)).thenReturn(null);

        ResponseEntity<Void> response = likeRestController.create(likeDTO);

        assertThat(response.getStatusCodeValue()).isEqualTo(400); // Bad Request
    }

    @Test
    void create_shouldReturnConflictIfLikeExists() {
        LikeDTO likeDTO = new LikeDTO(1L, 2L);
        User giver = new User();
        giver.setId(1L);
        User receiver = new User();
        receiver.setId(2L);

        when(userDAO.findUserById(1L)).thenReturn(giver);
        when(userDAO.findUserById(2L)).thenReturn(receiver);
        when(likeDAO.likeExists(1L, 2L)).thenReturn(true);

        ResponseEntity<Void> response = likeRestController.create(likeDTO);

        assertThat(response.getStatusCodeValue()).isEqualTo(409); // Conflict
    }

    @Test
    void isMutualLike_shouldReturnTrueIfMutual() {
        when(likeDAO.isMutualLike(1L, 2L)).thenReturn(true);

        boolean result = likeRestController.isMutualLike(1L, 2L);

        assertThat(result).isTrue();
        verify(likeDAO, times(1)).isMutualLike(1L, 2L);
    }

    @Test
    void likeExists_shouldReturnTrueIfLikeExists() {
        when(likeDAO.likeExists(1L, 2L)).thenReturn(true);

        boolean result = likeRestController.likeExists(1L, 2L);

        assertThat(result).isTrue();
        verify(likeDAO, times(1)).likeExists(1L, 2L);
    }
}
