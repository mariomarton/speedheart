package marton.speedheart.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;

public class UserDTO {

    private final Long id;
    private final String firstName;
    private final String phoneNumber;
    private final String email;
    private final String photo;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final Date birthDate;

    // Lists of likes given and received (only IDs for simplicity)
    private final List<Long> likesGiven; // IDs of likes given by the user
    private final List<Long> likesReceived; // IDs of likes received by the user

    @JsonCreator
    public UserDTO(
            @JsonProperty("id") Long id,
            @JsonProperty("firstName") String firstName,
            @JsonProperty("phoneNumber") String phoneNumber,
            @JsonProperty("email") String email,
            @JsonProperty("birthDate") Date birthDate,
            @JsonProperty("likesGiven") List<Long> likesGiven,
            @JsonProperty("likesReceived") List<Long> likesReceived,
            @JsonProperty("photo") String photo

            ) {
        this.id = id;
        this.firstName = firstName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.birthDate = birthDate;
        this.likesGiven = likesGiven;
        this.likesReceived = likesReceived;
        this.photo = photo;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public List<Long> getLikesGiven() {
        return likesGiven;
    }

    public List<Long> getLikesReceived() {
        return likesReceived;
    }

    public String getPhoto() {
        return photo;
    }
}
