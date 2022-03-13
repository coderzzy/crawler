import com.alibaba.fastjson.JSON;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * https://www.creema.jp/item/13539950/detail
 * <p>
 * 人工操作：
 * 测试账号: zzx@gmail.com zzx12xzz
 * CSRF-TOKEN: 8aWzROqVYxds8HVqVZmfiiqe17UIWAlsiu1aADNb
 * <p>
 * 主要流程
 * 1、访问页面，set-cookie 和 csrfToken
 * 2、注册 -> 跳转后自动登录
 * 3、搜索 La belleza
 * 4、点赞
 * 5、登出
 */
public class CreemaWebUtils {

    private static final boolean DEBUG_FLAG = false;

    private static void doDebug(String content) {
        if (!DEBUG_FLAG) return;
        System.out.println(content);
    }

    private static final Pattern CSRF_TOKEN_PATTERN = Pattern.compile("(?<=csrfToken: ').*(?=')");
    private static final String DUPLICATE_SIGN_UP = "既に登録済みのメールアドレスです";
    private static final String SEARCH_WORD = "La belleza";

    public static void main(String[] args) throws InterruptedException {
        Env env = getEnv();
        System.out.println("env = " + JSON.toJSONString(env));
        String csrfToken = env.getCsrfToken();
        String cookies = env.getCookies();

        Thread.sleep(1000);

        String email = "zzx@gmail.com";
        String password = "zzx12xzz";
        String userName = "zzx";
        Env newEnv = signUp(email, password, userName, csrfToken, cookies);
        System.out.println("after signUp, newEnv = " + JSON.toJSONString(newEnv));
        if (null == newEnv) {
            System.out.println("user may sign up failed, please retry !");
            return;
        }
        csrfToken = newEnv.getCsrfToken();
        cookies = newEnv.getCookies();

        Thread.sleep(1000);

        String result = favorite("8748188", csrfToken, cookies);
        System.out.println("after favorite, result = " + result);

        Thread.sleep(1000);

        signOut(cookies);
        System.out.println("after signOut");
    }

    // ----------------------功能代码----------------------

    /**
     * 获取环境信息
     * 1、cookies
     * 2、csrfToken
     *
     * @return
     */
    private static Env getEnv() {
        String uri = "https://www.creema.jp/";

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, HttpUtils.BROWSER_USER_AGENT);

        HttpEntity entity = new HttpEntity<>(headers);
        ResponseEntity<String> result = HttpUtils.doGet(uri, entity);

        doDebug("getCsrfToken result = " + result);

        String cookies = result.getHeaders().get(HttpHeaders.SET_COOKIE)
                .stream().collect(Collectors.joining(";"));
        Matcher m = CSRF_TOKEN_PATTERN.matcher(result.getBody());
        String csrfToken = m.find() ? m.group(0) : "";

        return new Env(cookies, csrfToken);
    }

    /**
     * 注册
     *
     * @param email
     * @param password
     * @param userName
     * @param csrfToken
     * @param cookies
     * @return
     */
    private static Env signUp(String email, String password,
                              String userName, String csrfToken, String cookies) {
        String uri = "https://www.creema.jp/user/registration/input";

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        headers.set(HttpHeaders.USER_AGENT, HttpUtils.BROWSER_USER_AGENT);
        headers.set(HttpHeaders.COOKIE, cookies);

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.put("_token", Collections.singletonList(csrfToken));
        params.put("from", Collections.singletonList("registration"));
        params.put("redirect_to", Collections.singletonList("/"));
        params.put("email", Collections.singletonList(email));
        params.put("password", Collections.singletonList(password));
        params.put("nickname", Collections.singletonList(userName));

        HttpEntity entity = new HttpEntity<>(params, headers);
        ResponseEntity<String> result = HttpUtils.doPostJump302(uri, entity);

        doDebug("sign up result = " + result);
        if (result.getBody().contains(DUPLICATE_SIGN_UP)) {
            return null;
        }
        // 成功注册后，需要更新 env
        String newCookies = result.getHeaders().get(HttpHeaders.SET_COOKIE)
                .stream().collect(Collectors.joining(";"));
        Matcher m = CSRF_TOKEN_PATTERN.matcher(result.getBody());
        String newCsrfToken = m.find() ? m.group(0) : "";

        return new Env(newCookies, newCsrfToken);
    }

    /**
     * 收藏
     */
    private static String favorite(String itemId, String csrfToken, String cookies) {
        String uri = String.format("https://www.creema.jp/item/%s/favorite/add", itemId);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        headers.set(HttpHeaders.USER_AGENT, HttpUtils.BROWSER_USER_AGENT);
        headers.set(HttpHeaders.COOKIE, cookies);

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.put("language", Collections.singletonList("ja"));
        params.put("_token", Collections.singletonList(csrfToken));

        HttpEntity entity = new HttpEntity<>(params, headers);
        ResponseEntity<String> result = HttpUtils.doPost(uri, entity);

        doDebug("favorite result = " + result);
        return result.getBody();
    }

    /**
     * 登出
     */
    private static void signOut(String cookies) {
        String uri = "https://www.creema.jp/user/logout";

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, HttpUtils.BROWSER_USER_AGENT);
        headers.set(HttpHeaders.COOKIE, cookies);

        HttpEntity entity = new HttpEntity<>(headers);
        ResponseEntity<String> result = HttpUtils.doGet(uri, entity);

        doDebug("signOut result = " + result);
    }

    // ------ 内部类

    private static class Env {
        private String cookies;
        private String csrfToken;

        public Env(String cookies, String csrfToken) {
            this.cookies = cookies;
            this.csrfToken = csrfToken;
        }

        public String getCookies() {
            return cookies;
        }

        public String getCsrfToken() {
            return csrfToken;
        }
    }

}


