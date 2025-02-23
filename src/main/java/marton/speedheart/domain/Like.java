package marton.speedheart.domain;

import marton.speedheart.dto.LikeDTO;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "likes")
@NamedQuery(name = "Like.findAll", query = "SELECT l FROM Like l")  // JPQL query for fetching all likes
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-generate ID
    private Long id; // Primary key, repository-generated

    private Date timestamp;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "giver_id", referencedColumnName = "id")
    private User giver;  // The user who gave the like

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "receiver_id", referencedColumnName = "id")
    private User receiver;  // The user who received the like

    // Default constructor for JPA
    public Like() {}

    // Constructor with timestamp
    public Like(Date timestamp) {
        this.timestamp = timestamp;
    }

    // Getters & setters:
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getGiver() {
        return giver;
    }

    public void setGiver(User giver) {
        this.giver = giver;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }


    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    // Constructor from LikeDTO
    public Like(LikeDTO dto, User giver, User receiver) {
        this.id = dto.getId();
        this.timestamp = dto.getTimestamp();
        this.giver = giver;
        this.receiver = receiver;
    }

    // Method to convert Like to LikeDTO
    public LikeDTO toDTO() {
        return new LikeDTO(id, timestamp, giver.getId(), receiver.getId());
    }

    @Override
    public String toString() {
        return "Like{" +
                "id=" + id +
                ", giver=" + giver +
                ", receiver=" + receiver +
                ", timestamp=" + timestamp +
                '}';
    }

}
