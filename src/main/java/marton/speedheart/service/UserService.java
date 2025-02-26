package marton.speedheart.service;

import marton.speedheart.dto.UserDTO;
import static marton.speedheart.Constants.SERVER_BASE;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private static final String USER_URL = SERVER_BASE + "/user";

    private final RestTemplate restTemplate;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public UserService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Create a new user by sending a POST request to the backend with user data and photo.
     *
     * @param userDTO The user information to be sent to the backend.
     * @param photo   The profile picture file to be uploaded.
     * @return The URI of the newly created user.
     */
    public URI createUser(UserDTO userDTO, MultipartFile photo) {
        try {
            // Check if photo is not null and save the file
            String photoPath = null;
            if (photo != null && !photo.isEmpty()) {
                photoPath = saveFile(photo);
            }
            else throw new RuntimeException("Failed to create user: Photo is empty or null!");

            FileSystemResource photoResource = new FileSystemResource(Paths.get(photoPath).toFile());

            // Prepare the multipart request
            MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
            map.add("user", userDTO);  // Add the userDTO as a JSON part
            map.add("photo", photoResource);  // Add the photo as a file part

            // Prepare headers and entity
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(map, headers);

            // Send the multipart request to the server
            ResponseEntity<Void> response = restTemplate.exchange(
                    USER_URL + "/create",
                    HttpMethod.POST,
                    entity,
                    Void.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return URI.create(USER_URL + "/" + userDTO.getId());  // Return the URI of the created user
            } else {
                throw new RuntimeException("Failed to create user");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error processing file upload", e);
        }
    }

    /**
     * Fetch a user by their ID.
     *
     * @param id The ID of the user to retrieve.
     * @return The UserDTO corresponding to the user.
     */
    public UserDTO getUserById(Long id) {
        return restTemplate.getForObject(USER_URL + "/" + id, UserDTO.class);
    }

    public UserDTO findNextUser(Long currentUserId) {
        // Get the response as ResponseEntity to check for status codes
        ResponseEntity<UserDTO> response = restTemplate.exchange(
                USER_URL + "/next/" + currentUserId,
                HttpMethod.GET,
                null,
                UserDTO.class
        );

        // Check if the response is 204 (No Content)
        if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
            return null; // No next user
        }

        // If status is OK, return the UserDTO
        return response.getBody();
    }

    /**
     * Fetch the user with the lowest ID (earliest registered).
     *
     * @return The UserDTO corresponding to the user with the lowest ID.
     */
    public UserDTO findFirstUser() {
        return restTemplate.getForObject(USER_URL + "/lowest", UserDTO.class);
    }

    public UserDTO findLastUser() {
        return restTemplate.getForObject(USER_URL + "/highest", UserDTO.class);
    }

    /**
     * Fetch all users.
     *
     * @return A list of all users.
     */
    public List<UserDTO> getAllUsers() {
        // Send GET request to retrieve the list of all users
        ResponseEntity<List<UserDTO>> response = restTemplate.exchange(
                USER_URL + "/all",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<UserDTO>>() {}
        );

        // Return the body containing the list of users
        return response.getBody();
    }

    public boolean deleteUser(Long id) {
        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    USER_URL + "/" + id,
                    HttpMethod.DELETE,
                    null,
                    Void.class
            );

            // Return true if the response status is 204 (No Content)
            return response.getStatusCode() == HttpStatus.NO_CONTENT;
        } catch (HttpClientErrorException.NotFound e) {
            // Handle 404 (User not found)
            System.err.println("User not found: " + e.getMessage());
            return false;
        } catch (Exception e) {
            // Handle other exceptions (e.g., server error, connectivity issues)
            System.err.println("Error occurred while deleting user: " + e.getMessage());
            return false;
        }
    }


    private String saveFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Generate a unique file name to avoid overwriting
        String originalFilename = file.getOriginalFilename();
        String uniqueFilename = UUID.randomUUID() + "_" + originalFilename;

        Path uploadPath = Paths.get(uploadDir, uniqueFilename).toAbsolutePath();
        File destinationFile = uploadPath.toFile();

        file.transferTo(destinationFile);

        return uploadPath.toString(); // Return the file path
    }
}
