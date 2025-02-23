package marton.speedheart.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class LikeDTO {

    private final Long id;
    private final Date timestamp;
    private final Long giverId;  // The ID of the user who gave the like
    private final Long receiverId;  // The ID of the user who received the like

    @JsonCreator
    public LikeDTO(
            @JsonProperty("id") Long id,
            @JsonProperty("timestamp") Date timestamp,
            @JsonProperty("giverId") Long giverId,
            @JsonProperty("receiverId") Long receiverId
    ) {
        this.id = id;
        this.timestamp = timestamp;
        this.giverId = giverId;
        this.receiverId = receiverId;
    }

    // Overloaded constructor for test cases
    public LikeDTO(Long giverId, Long receiverId) {
        this.id = null;
        this.timestamp = null;
        this.giverId = giverId;
        this.receiverId = receiverId;
    }

    public Long getId() {
        return id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Long getGiverId() {
        return giverId;
    }

    public Long getReceiverId() {
        return receiverId;
    }
}
