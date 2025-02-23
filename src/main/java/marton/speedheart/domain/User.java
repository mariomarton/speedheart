package marton.speedheart.domain;

import marton.speedheart.dto.UserDTO;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@NamedQuery(name = "User.findAll", query = "SELECT u FROM User u")  // JPQL query for fetching all users
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-generate ID
    private Long id; // Primary key, repository-generated

    private String firstName;
    private String phoneNumber;
    private String email;
    private Date birthDate;
    private String photo;

    // Default constructor for JPA
    public User() {}

    public User(String firstName, String phoneNumber, String email, Date birthDate, String photo) {
        this.firstName = firstName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.birthDate = birthDate;
        this.photo = photo;
    }

    // One-to-many relationship for likes given by the user
    @OneToMany(mappedBy = "giver")
    private List<Like> likesGiven = new ArrayList<>();;

    // One-to-many relationship for likes received by the user
    @OneToMany(mappedBy = "receiver")
    private List<Like> likesReceived = new ArrayList<>();;

    // Getters & setters:
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Like> getLikesGiven() {
        return likesGiven;
    }

    public void setLikesGiven(List<Like> likesGiven) {
        this.likesGiven = likesGiven;
    }

    public List<Like> getLikesReceived() {
        return likesReceived;
    }

    public void setLikesReceived(List<Like> likesReceived) {
        this.likesReceived = likesReceived;
    }

    // Method to get the IDs of the likes given by the user
    public List<Long> getLikesGivenIds() {
        return likesGiven.stream()
                .map(Like::getId)  // Get the ID of each Like from the giver's likes
                .collect(Collectors.toList());
    }

    // Method to get the IDs of the likes received by the user
    public List<Long> getLikesReceivedIds() {
        return likesReceived.stream()
                .map(Like::getId)  // Get the ID of each Like from the receiver's likes
                .collect(Collectors.toList());
    }


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    // Method to convert User to UserDTO
    public UserDTO toDTO() {
        return new UserDTO(id, firstName, phoneNumber, email, birthDate, getLikesGivenIds(), getLikesReceivedIds(), getPhoto());
    }

    // Constructor from UserDTO
    public User(UserDTO dto) {
        this.id = dto.getId();
        this.firstName = dto.getFirstName();
        this.phoneNumber = dto.getPhoneNumber();
        this.email = dto.getEmail();
        this.birthDate = dto.getBirthDate();
        this.photo = dto.getPhoto();
    }

    // Override toString()
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", birthDate=" + birthDate +
                ", photo=" + photo +
                '}';
    }


}
