package marton.speedheart.web_controller;

import marton.speedheart.dto.LikeDTO;
import marton.speedheart.dto.UserDTO;
import marton.speedheart.service.LikeService;
import marton.speedheart.service.UserService;
import static marton.speedheart.Constants.SERVER_UPLOADS_BASE;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.text.SimpleDateFormat;

@Controller
@SessionAttributes("signedInUser")  // Declare that 'signedInUser' should be kept in session
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService; // Use UserService to interact with the server API

    @Autowired
    private LikeService likeService;

    // Add this method to always add the current user to the model
    @ModelAttribute
    public void addSignedInUserToModel(Model model) {
        // Check if the current user is stored in the session
        UserDTO signedInUser = (UserDTO) model.getAttribute("signedInUser");
        if (signedInUser != null) {
            model.addAttribute("signedInUser", signedInUser); // Add current user to model
        }
    }

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("user", new UserDTO());  // Add empty UserDTO to model
        return "signup";  // This will return the "signup.html" template
    }

    @PostMapping("/signup")
    public String signupUser(
            @RequestParam("firstName") String firstName,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("email") String email,
            @RequestParam("birthDate") String birthDateString,
            @RequestParam("photo") MultipartFile photo,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            // Manually convert birthDateString to Date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date birthDate = sdf.parse(birthDateString);

            // Create a UserDTO to send to the server
            UserDTO userDTO = new UserDTO();
            userDTO.setFirstName(firstName);
            userDTO.setPhoneNumber(phoneNumber);
            userDTO.setEmail(email);
            userDTO.setBirthDate(birthDate);

            // Use the service to send the request with the file
            URI createdUserUri = userService.createUser(userDTO, photo);
            if (createdUserUri != null) {
                logger.debug("User created successfully.");
                redirectAttributes.addFlashAttribute("successful_signup_msg", true);
                return "redirect:/signin";
            }

            model.addAttribute("error", "User creation failed. Please try again.");
        } catch (Exception e) {
            model.addAttribute("error", "An unexpected error occurred.");
            logger.error("Error during sign-up process", e);
        }

        return "signup";
    }

    @GetMapping("/user")
    public String showUser(@RequestParam Long id, Model model) {
        // Retrieve the currently signed-in user from session
        UserDTO signedInUser = (UserDTO) model.getAttribute("signedInUser");

        if (signedInUser != null) {
            UserDTO user = userService.getUserById(id);
            model.addAttribute("user", user);
            model.addAttribute("serverUploadsBase", SERVER_UPLOADS_BASE);
            return "user_detail";
        } else {
            // If no user is signed in, redirect to sign-in page
            return "redirect:/signin";
        }
    }

    @GetMapping("/user/next")
    public String showNextUser(@RequestParam Long currentlyShownUserId, Model model, SessionStatus sessionStatus, RedirectAttributes redirectAttributes) {
        // Retrieve the currently signed-in user from session
        UserDTO signedInUser = (UserDTO) model.getAttribute("signedInUser");

        if (signedInUser != null) {
            UserDTO nextUser = userService.findNextUser(currentlyShownUserId);
            if (nextUser != null) {
                // if next user == signed-in user, skip the next user
                if (Objects.equals(signedInUser.getId(), nextUser.getId())) {
                    // if next user == signed-in user == last user, show the last user message
                    if (Objects.equals(signedInUser.getId(), userService.findLastUser().getId())) {
                        checkAndAddLikeStatus(signedInUser.getId(), currentlyShownUserId, redirectAttributes); // Mutual like check
                        redirectAttributes.addFlashAttribute("no_more_users", true);
                        return "redirect:/user?id=" + currentlyShownUserId;
                    }
                    return showNextUser(nextUser.getId(), model, sessionStatus, redirectAttributes);
                }
                // Update the session with the next user
                model.addAttribute("currentlyShownUser", nextUser);
                checkAndAddLikeStatus(signedInUser.getId(), nextUser.getId(), redirectAttributes); // Mutual like check
                return "redirect:/user?id=" + nextUser.getId();
            } else {
                checkAndAddLikeStatus(signedInUser.getId(), currentlyShownUserId, redirectAttributes); // Mutual like check
                redirectAttributes.addFlashAttribute("no_more_users", true);
                return "redirect:/user?id=" + currentlyShownUserId;
            }
        } else {
            // If no user is signed in, redirect to sign-in page
            return "redirect:/signin";
        }
    }

    @GetMapping("")
    public String showHomePage(Model model) {
        return showSignInPage(model);
    }

    @GetMapping("/signin")
    public String showSignInPage(Model model) {
        // Fetch all users to display in the sign-in page
        List<UserDTO> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "signin"; // Return the sign-in page template
    }

    @PostMapping("/signin")
    public String signIn(@RequestParam Long userId, Model model, RedirectAttributes redirectAttributes) {
        // Fetch the selected user by ID
        UserDTO user = userService.getUserById(userId);

        // Check if the user exists
        if (user != null) {
            // Store the signed-in user in session
            model.addAttribute("signedInUser", user);

            UserDTO lowestUser = userService.findFirstUser();
            if(Objects.equals(user.getId(), lowestUser.getId())){
                UserDTO nextLowestUser = userService.findNextUser(userId);
                checkAndAddLikeStatus(userId, nextLowestUser.getId(), redirectAttributes); // Mutual like check
                return "redirect:/user?id=" + nextLowestUser.getId();
            }

            checkAndAddLikeStatus(userId, lowestUser.getId(), redirectAttributes); // Mutual like check

            return "redirect:/user?id=" + lowestUser.getId();  // Redirect to a user's detail page
        } else {
            model.addAttribute("error", "User not found. Please try again.");
            return "signin"; // Return to the sign-in page in case of an error
        }
    }

    @PostMapping("/user/like")
    public String likeUser(@RequestParam Long likedUserId, @RequestParam(required = false) Boolean noMoreUsers, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        // Retrieve the currently signed-in user from session
        UserDTO signedInUser = (UserDTO) model.getAttribute("signedInUser");

        if (signedInUser != null) {
            // Retrieve the user who is being liked
            UserDTO likedUser = userService.getUserById(likedUserId);

            if (Boolean.TRUE.equals(noMoreUsers)) {
                redirectAttributes.addFlashAttribute("no_more_users", true);
            }

            if (likedUser != null) {
                // Create a new LikeDTO
                LikeDTO likeDTO = new LikeDTO(
                        null,  // ID will be auto-generated by the backend
                        new Date(),  // Timestamp for the like
                        signedInUser.getId(),  // The ID of the user who is giving the like
                        likedUser.getId()  // The ID of the user receiving the like
                );

                try {
                    URI likeUri = likeService.createLike(likeDTO);
                    // Add a success message to the FlashAttributes
                    redirectAttributes.addFlashAttribute("message", "You liked " + likedUser.getFirstName() + "!");
                    checkAndAddLikeStatus(signedInUser.getId(), likedUser.getId(), redirectAttributes);
                } catch (HttpClientErrorException.Conflict e) {
                    // Handle 409 Conflict (like already exists)
                    redirectAttributes.addFlashAttribute("error", "You have already liked this user.");
                    checkAndAddLikeStatus(signedInUser.getId(), likedUser.getId(), redirectAttributes);
                } catch (HttpClientErrorException.MethodNotAllowed e) {
                    // Handle 405 Conflict (giver equals receiver)
                    redirectAttributes.addFlashAttribute("error", "Users cannot like themselves.");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "User not found.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "No user signed in.");
        }

        // Stay on the same page with a FlashAttribute message
        return "redirect:/user?id=" + likedUserId;  // Redirect to the same page
    }

    @PostMapping("/user/delete")
    public String deleteUser(Model model,
                             RedirectAttributes redirectAttributes,
                             HttpServletRequest request) {
        UserDTO signedInUser = (UserDTO) model.getAttribute("signedInUser");
        if (signedInUser == null) {
            redirectAttributes.addFlashAttribute("error", "No signed-in user found.");
            return "redirect:/signin";
        }
        boolean isDeleted = userService.deleteUser(signedInUser.getId());
        if (isDeleted) {
            redirectAttributes.addFlashAttribute("successful_delete", true);
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to delete user. Please try again.");
        }
        return "redirect:/signin";
    }


    @GetMapping("/error")
    public String showErrorPage(Model model) {
        return "error";
    }

    private void checkAndAddLikeStatus(Long signedInUserId, Long likedUserId, RedirectAttributes redirectAttributes) {
        if (likeService.isMutualLike(signedInUserId, likedUserId)) {
            redirectAttributes.addFlashAttribute("likeStatus", "YOU BOTH LIKE EACH OTHER!<br>You can now see this user's email and number! 💞");
            redirectAttributes.addFlashAttribute("showContact", true);
        }
        else if (likeService.likeExists(likedUserId, signedInUserId)) {
            redirectAttributes.addFlashAttribute("likeStatus", "This user likes you! Maybe you can like them back? 😏");
        }
        else if (likeService.likeExists(signedInUserId, likedUserId)) {
            redirectAttributes.addFlashAttribute("likeStatus", "You've liked this user, but they haven't liked you back, yet 💔");
        }
        else {
            redirectAttributes.addFlashAttribute("likeStatus", "You haven't yet liked this user and they haven't yet liked you 😿");
        }
    }

}
