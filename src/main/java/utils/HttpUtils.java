package utils;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class HttpUtils {

    public static final String BROWSER_USER_AGENT
            = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.139 Safari/537.36";


    public static ResponseEntity<String> doGet(String uri, HttpEntity entity) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
    }

    public static ResponseEntity<String> doPost(String uri, HttpEntity entity) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
    }

    /**
     * POST, 默认不跟进 302 跳转，因此重设跳转规则跟进 302
     *
     * @param uri
     * @param entity
     * @return
     */
    public static ResponseEntity<String> doPostJump302(String uri, HttpEntity entity) {
        RestTemplate restTemplate = new RestTemplate();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        HttpClient httpClient = HttpClientBuilder.create()
                .setRedirectStrategy(new LaxRedirectStrategy())
                .build();
        factory.setHttpClient(httpClient);
        restTemplate.setRequestFactory(factory);
        return restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
    }
}
