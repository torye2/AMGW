package amgw.amgw.config;

import amgw.amgw.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NextcloudClient {
    private final RestTemplate rest;
    @Value("${nextcloud.base-url}") String baseUrl;
    @Value("${nextcloud.admin-user}") String adminUser;
    @Value("${nextcloud.admin-pass}") String adminPass;

    private HttpHeaders ocsHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setBasicAuth(adminUser, adminPass);
        h.add("OCS-APIRequest", "true");
        h.setAccept(MediaType.parseMediaTypes("application/json"));
        h.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Basic Auth
        String token = Base64.getEncoder()
                .encodeToString((adminUser + ":" + adminPass).getBytes(StandardCharsets.UTF_8));
        h.set("Authorization", "Basic " + token);
        return h;
    }

    public boolean userExists(String userId) {
        try {
            var url = baseUrl + "/ocs/v1.php/cloud/users/" + UriUtils.encodePath(userId, StandardCharsets.UTF_8) + "?format=json";
            var entity = new HttpEntity<>(ocsHeaders());
            rest.exchange(url, HttpMethod.GET, entity, String.class);
            log.info("user exists run");
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            log.info("user exists run fail");
            return false;
        }
    }

    public void ensureUser(String userId, String displayName, String email, String initialPassword, String department) {
        if (userExists(userId)) return;

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("userid", userId);
        form.add("password", (initialPassword != null && !initialPassword.isBlank()) ? initialPassword : randomPassword());
        if (displayName != null && !displayName.isBlank()) form.add("displayName", displayName);
        if (email != null && !email.isBlank()) form.add("email", email);
        if (department != null && !department.isBlank()) form.add("department", department);

        var url = baseUrl + "/ocs/v1.php/cloud/users";
        log.info("➡ Nextcloud 사용자 생성 시도: {}", userId);
        try {
            rest.postForEntity(url, new HttpEntity<>(form, ocsHeaders()), String.class);
            log.info("✅ Nextcloud 사용자 생성 완료: {}", userId);
        } catch (Exception e) {
            log.error("❌ Nextcloud 사용자 생성 실패: {} - {}", userId, e.getMessage());
            throw e;
        }
    }

    public void setUserDisplayName(String userId, String displayName) {
        if (displayName == null || displayName.isBlank()) return;
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("key", "displayname");
        form.add("value", displayName);
        var url = baseUrl + "/ocs/v1.php/cloud/users/" + enc(userId);
        rest.exchange(url, HttpMethod.PUT, new HttpEntity<>(form, ocsHeaders()), String.class);
    }

    public void setUserEmail(String userId, String email) {
        if (email == null || email.isBlank()) return;
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("key", "email");
        form.add("value", email);
        var url = baseUrl + "/ocs/v1.php/cloud/users/" + enc(userId);
        rest.exchange(url, HttpMethod.PUT, new HttpEntity<>(form, ocsHeaders()), String.class);
    }

    // ===== Groups =====
    public boolean groupExists(String groupId) {
        try {
            var url = baseUrl + "/ocs/v1.php/cloud/groups/" + enc(groupId);
            rest.exchange(url, HttpMethod.GET, new HttpEntity<>(ocsHeaders()), String.class);
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }

    public void ensureGroup(String groupId) {
        if (groupExists(groupId)) return;
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("groupid", groupId);
        var url = baseUrl + "/ocs/v1.php/cloud/groups";
        rest.postForEntity(url, new HttpEntity<>(form, ocsHeaders()), String.class);
    }

    public void addUserToGroup(String userId, String groupId) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("groupid", groupId);
        var url = baseUrl + "/ocs/v1.php/cloud/users/" + enc(userId) + "/groups";
        rest.postForEntity(url, new HttpEntity<>(form, ocsHeaders()), String.class);
    }

    private static String enc(String s) {
        return UriUtils.encodePath(s, StandardCharsets.UTF_8);
    }

    private String randomPassword() {
        return UUID.randomUUID().toString();
    }
}

