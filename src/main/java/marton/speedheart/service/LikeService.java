package marton.speedheart.service;

import marton.speedheart.dto.LikeDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static marton.speedheart.Constants.SERVER_BASE;

@Service
public class LikeService {
    private static final String LIKE_URL = SERVER_BASE + "/like";

    private final RestTemplate restTemplate;

    public LikeService() {
        this.restTemplate = new RestTemplate();
    }

    public URI createLike(LikeDTO likeDTO) {
        return restTemplate.postForLocation(LIKE_URL, likeDTO);
    }

    public boolean likeExists(Long giverId, Long receiverId) {
        String url = LIKE_URL + "/exists/" + giverId + "/" + receiverId;
        return restTemplate.getForObject(url, Boolean.class);
    }

    public boolean isMutualLike(Long giverId, Long receiverId) {
        String url = LIKE_URL + "/mutual/" + giverId + "/" + receiverId;
        return restTemplate.getForObject(url, Boolean.class);
    }

}
