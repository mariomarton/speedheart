package marton.speedheart.rest_controller;

import marton.speedheart.domain.User;
import marton.speedheart.dto.UserDTO;
import marton.speedheart.repositories.UserDAO;
import marton.speedheart.validation.UserValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("rest/user")
public class UserRestController {

    // Logger instance
    private static final Logger logger = LoggerFactory.getLogger(UserRestController.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    UserDAO userDAO;

    @Autowired
    HttpServletRequest httpServletRequest;

    @GetMapping("/all")
    public List<UserDTO> all() {
        logger.debug("Fetching all users");
        List<UserDTO> users = userDAO.allUsers();
        logger.debug("Returning {} users", users.size());
        return users;
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> create(
            @RequestPart("user") @Valid UserValidation userValidation, // For the JSON user data
            @RequestPart("photo") MultipartFile photo) throws IOException { // For the photo file

        logger.debug("Creating user with validated data");

        // Check if photo is not null and save the file
        String photoPath = null;
        if (photo != null && !photo.isEmpty()) {
            photoPath = saveFile(photo);
        }

        String fileName = null;
        if (photoPath != null) {
            fileName = Paths.get(photoPath).getFileName().toString();
        }

        // Create and save the user
        User user = new User(
                userValidation.getFirstName(),
                userValidation.getPhoneNumber(),
                userValidation.getEmail(),
                userValidation.getBirthDate(),
                fileName // of the photo
        );

        Long id = userDAO.createUser(user);

        URI location = URI.create(httpServletRequest.getRequestURI() + "/" + id);
        logger.debug("User created with ID: {}", id);

        // Return a response with the location of the created user
        return ResponseEntity.created(location).build();
    }


    // Get a user by ID
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        User user = userDAO.findUserById(id);
        if (user != null) {
            return ResponseEntity.ok(user.toDTO());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Get the next user (logic can be customized)
    @GetMapping("/next/{id}")
    public ResponseEntity<UserDTO> getNextUser(@PathVariable Long id) {
        UserDTO nextUser = userDAO.findNextUser(id);
        if (nextUser != null) {
            return ResponseEntity.ok(nextUser);
        } else {
            return ResponseEntity.noContent().build(); // return 204 if no more users
        }
    }

    @GetMapping("/lowest")
    public ResponseEntity<UserDTO> getUserWithLowestId() {
        UserDTO user = userDAO.findUserWithLowestId();
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/highest")
    public ResponseEntity<UserDTO> getUserWithHighestId() {
        UserDTO user = userDAO.findUserWithHighestId();
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
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

        logger.debug("Saving file to: " + destinationFile.getAbsolutePath());

        try {
            file.transferTo(destinationFile);
            logger.debug("File saved successfully: " + destinationFile.getAbsolutePath());
        } catch (Exception e) {
            logger.error("Error saving file", e);
        }

        return uploadPath.toString(); // Return the file path for storage in the database
    }

    // Delete user by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        boolean deleted = userDAO.deleteUserById(id);
        if (deleted) {
            logger.debug("User with ID {} deleted successfully", id);
            return ResponseEntity.noContent().build();
        } else {
            logger.debug("User with ID {} not found for deletion", id);
            return ResponseEntity.notFound().build();
        }
    }

    // Update user's email by ID via PATCH
    @PatchMapping("/{id}/email")
    public ResponseEntity<Void> updateUserEmail(@PathVariable Long id, @RequestParam String newEmail) {
        boolean updated = userDAO.updateUserEmail(id, newEmail);
        if (updated) {
            logger.debug("User with ID {} updated with new email: {}", id, newEmail);
            return ResponseEntity.ok().build();
        } else {
            logger.debug("User with ID {} not found for email update", id);
            return ResponseEntity.notFound().build();
        }
    }



}