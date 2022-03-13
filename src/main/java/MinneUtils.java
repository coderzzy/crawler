import com.alibaba.fastjson.JSON;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MinneUtils {
    // chrome
    // private static final String BROWSER_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.139 Safari/537.36";

    // vivo 1920
    private static final String ANDROID_USER_AGENT
            = "minne/7.35.3 (Android Mobile vivo 1920; Android OS 9)";
    private static final String CLIENT_ID = "bcffe91e02cd86130245e7fc0d0648caa5ffe20eaab304a3a647b47bdf836d27";
    private static final String CLIENT_SECRET = "716f2a0d29dd75a3888b462e8646c11a392f8fb730553a1a7b89b70e1122202f";


    public static void main(String[] args) throws InterruptedException {
        // 完整测试随机生成账号，注册 + 登录 + 收藏 + 登出
        testRandomAccount();

//        String email = "zzx123xzz@qq.com";
//        String password = "zzx2479xzz";
//        String userName = "zzx123xzz";
//        // 注册测试
//        testSignUp(email, password, userName);
//
//        // 登录 + 收藏 + 登出 测试
//        testSignInAndFavorite(email, password);
    }

    // ----------------------测试代码----------------------

    private static void testRandomAccount() throws InterruptedException {
        // 0. 生成随机账号
        String randomStr = CrawlUtils.getStringRandom(8);
        String email = randomStr + "@qq.com";
        String userName = randomStr;
        String password = "1a" + randomStr;
        System.out.println("random account, " + email);
        // 1. 注册-校验userName, 应当为true
        boolean checkResult = checkName(userName);
        System.out.println("first check name, expect = true, real = " + checkResult);
        if (!checkResult) {
            System.out.println("username duplicate, please retry !");
            return;
        }
        // 2. 注册
        Thread.sleep(1000);
        boolean signUpResult = signUp(email, password, userName);
        System.out.println("sign up success = " + signUpResult);
        // 3. 再次校验userName，应当为false
        Thread.sleep(1000);
        boolean checkResult2 = checkName(userName);
        System.out.println("first check name, expect = false, real = " + checkResult2);
        if (checkResult2) {
            System.out.println("user may sign up failed, please retry !");
            return;
        }
        // 4. 先获取 accessToken
        String accessToken = getAccessToken(email, password);
        Thread.sleep(1000);
        // 5. 登录
        signIn(accessToken);
        Thread.sleep(1000);
        // 6. 搜索
        List<String> productIds = search(accessToken, "La belleza");
        Thread.sleep(1000);
        // 7. 收藏
        for (String productId : productIds) {
            favorite(accessToken, productId);
            Thread.sleep(1000);
        }
        // 8. 登出
        Thread.sleep(1000);
        signOut(accessToken);
    }


    private static void testSignUp(String email, String password, String userName) throws InterruptedException {
        // 注册
        // 1. 注册-校验userName, 应当为true
        boolean checkResult = checkName(userName);
        System.out.println("first check name, expect = true, real = " + checkResult);

        // 2. 注册
        Thread.sleep(1000);
        boolean signUpResult = signUp(email, password, userName);
        System.out.println("sign up success = " + signUpResult);

        // 3. 再次校验userName，应当为false
        Thread.sleep(1000);
        boolean checkResult2 = checkName(userName);
        System.out.println("first check name, expect = false, real = " + checkResult2);
    }

    private static void testSignInAndFavorite(String email, String password) throws InterruptedException {
        // 登录 + 收藏 + 登出
        // 1. 先获取 accessToken
        String accessToken = getAccessToken(email, password);
        Thread.sleep(1000);
        // 2. 登录
        signIn(accessToken);
        Thread.sleep(1000);
        // 3. 搜索
        List<String> productIds = search(accessToken, "La belleza");
        Thread.sleep(1000);
        // 4. 收藏
        for (String productId : productIds) {
            favorite(accessToken, productId);
            Thread.sleep(1000);
        }
        // 5. 登出
        Thread.sleep(1000);
        signOut(accessToken);
    }

    // ----------------------功能代码----------------------

    /**
     * 注册阶段会校验userName是否重复
     * 204 表示正常
     * 422 表示异常
     *
     * @param userName
     * @return
     */
    private static boolean checkName(String userName) {
        String uri = "https://api.minne.com/v4/users/validate/name.json?user[name]=" + userName;

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, ANDROID_USER_AGENT);
        headers.set("X-Client-ID", CLIENT_ID);
        headers.set("X-Client-Secret", CLIENT_SECRET);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> result = null;
        try {
            result = HttpUtils.doGet(uri, entity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result == null) {
            return false;
        }
        System.out.println("check name result = " + result);
        return 204 == result.getStatusCode().value();
    }

    /**
     * 注册
     *
     * @param email
     * @param password
     * @param userName
     */
    private static boolean signUp(String email, String password, String userName) {
        String uri = "https://api.minne.com/v3/users.json";

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        headers.set(HttpHeaders.USER_AGENT, ANDROID_USER_AGENT);
        headers.set("X-Client-ID", CLIENT_ID);
        headers.set("X-Client-Secret", CLIENT_SECRET);

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.put("user[email]", Collections.singletonList(email));
        params.put("user[password]", Collections.singletonList(password));
        params.put("user[name]", Collections.singletonList(userName));

        HttpEntity entity = new HttpEntity<>(params, headers);
        ResponseEntity<String> result = HttpUtils.doPost(uri, entity);

        System.out.println("sign up result = " + result);
        return 200 == result.getStatusCode().value();
    }

    /**
     * 获取 access_token
     *
     * @param email
     * @param password
     * @return
     */
    private static String getAccessToken(String email, String password) {
        String uri = "https://api.minne.com/oauth/token?grant_type=password";

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        headers.set(HttpHeaders.USER_AGENT, ANDROID_USER_AGENT);
        headers.set("X-Client-ID", CLIENT_ID);
        headers.set("X-Client-Secret", CLIENT_SECRET);

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.put("username", Collections.singletonList(email));
        params.put("password", Collections.singletonList(password));
        params.put("client_id", Collections.singletonList(CLIENT_ID));
        params.put("client_secret", Collections.singletonList(CLIENT_SECRET));

        HttpEntity entity = new HttpEntity<>(params, headers);
        ResponseEntity<String> result = HttpUtils.doPost(uri, entity);

        System.out.println("getAccessToken result = " + result);
        AccessTokenResult accessTokenResult = JSON.parseObject(result.getBody(), AccessTokenResult.class);

        return accessTokenResult.getAccess_token();
    }

    /**
     * 登录
     */
    private static void signIn(String accessToken) {
        String uri = "https://api.minne.com/v4/user.json";

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, ANDROID_USER_AGENT);
        headers.set("X-Client-ID", CLIENT_ID);
        headers.set("X-Client-Secret", CLIENT_SECRET);
        headers.set("authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> result = HttpUtils.doGet(uri, entity);
        System.out.println("signIn result = " + result);
    }

    /**
     * 搜索
     */
    private static List<String> search(String accessToken, String word) {
        String uriFormat = "https://api.minne.com/v4/search/saleonly.json?" +
                "keywords=%s&per=%s&page=%s&search_type=normal&input_method=";
        int page = 1;
        int limit = 16;
        String uri = String.format(uriFormat, word, limit, page);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        headers.set(HttpHeaders.USER_AGENT, ANDROID_USER_AGENT);
        headers.set("X-Client-ID", CLIENT_ID);
        headers.set("X-Client-Secret", CLIENT_SECRET);
        headers.set("authorization", "Bearer " + accessToken);

        HttpEntity entity = new HttpEntity<>(headers);
        ResponseEntity<String> result = HttpUtils.doGet(uri, entity);
        System.out.println("search result = " + result);

        SearchResultList searchResultList = JSON.parseObject(result.getBody(), SearchResultList.class);
        List<SearchResult> searchResults = searchResultList.getData();

        return searchResults.stream().map(SearchResult::getId).collect(Collectors.toList());
    }


    /**
     * 收藏
     */
    private static void favorite(String accessToken, String productId) {
        String uri = "https://api.minne.com/v3/favorites.json";

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        headers.set(HttpHeaders.USER_AGENT, ANDROID_USER_AGENT);
        headers.set("X-Client-ID", CLIENT_ID);
        headers.set("X-Client-Secret", CLIENT_SECRET);
        headers.set("authorization", "Bearer " + accessToken);

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.put("product_id", Collections.singletonList(productId));

        HttpEntity entity = new HttpEntity<>(params, headers);
        ResponseEntity<String> result = HttpUtils.doPost(uri, entity);

        System.out.println("favorite result = " + result);
    }

    /**
     * 登出
     */
    private static void signOut(String accessToken) {
        String uri = "https://api.minne.com/oauth/revoke";

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        headers.set(HttpHeaders.USER_AGENT, ANDROID_USER_AGENT);
        headers.set("X-Client-ID", CLIENT_ID);
        headers.set("X-Client-Secret", CLIENT_SECRET);
        headers.set("authorization", "Bearer " + accessToken);

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.put("token", Collections.singletonList(accessToken));
        params.put("client_id", Collections.singletonList(CLIENT_ID));
        params.put("client_secret", Collections.singletonList(CLIENT_SECRET));

        HttpEntity entity = new HttpEntity<>(params, headers);
        ResponseEntity<String> result = HttpUtils.doPost(uri, entity);

        System.out.println("signOut result = " + result);
    }

}

class AccessTokenResult {
    private String access_token;

    public AccessTokenResult() {

    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }
}

class SearchResultList {

    private List<SearchResult> data;

    public SearchResultList() {

    }

    public List<SearchResult> getData() {
        return data;
    }

    public void setData(List<SearchResult> data) {
        this.data = data;
    }
}

class SearchResult {

    private String id;

    public SearchResult() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
