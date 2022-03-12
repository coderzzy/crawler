import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class HttpUtils {

    public static ResponseEntity<String> doGet(String uri, HttpEntity entity) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
    }

    public static ResponseEntity<String> doPost(String uri, HttpEntity entity) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
    }
}
