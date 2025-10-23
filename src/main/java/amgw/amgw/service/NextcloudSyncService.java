package amgw.amgw.service;

import amgw.amgw.config.NextcloudClient;
import amgw.amgw.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NextcloudSyncService {

    private final UserRepository userRepository;
    private final NextcloudClient nc;

    /**
     * DB 사용자 전체를 Nextcloud에 동기화:
     * - 사용자 생성/업데이트
     * - 부서명 → 그룹 보장 및 멤버십 부여
     */
    public void syncAllUsersFromDb() {
        var users = userRepository.findAllForNextcloud(); // projection

        for (var u : users) {
            String userId = decideUserId(u); // username 우선, 없으면 email/name 가공

            // 1) 사용자 보장
            nc.ensureUser(userId, u.getName(), u.getEmail(), null, u.getDepartment());

            // 2) 표시명/메일 최신화(있으면)
            nc.setUserDisplayName(userId, u.getName());
            nc.setUserEmail(userId, u.getEmail());

            // 3) 부서 → 그룹 매핑(있으면)
            if (u.getDepartment() != null && !u.getDepartment().isBlank()) {
                String group = normalizeGroup(u.getDepartment());
                nc.ensureGroup(group);
                nc.addUserToGroup(userId, group);
            }
        }
    }

    private String decideUserId(UserRepository.NcUserView u) {
        if (u.getUsername() != null && !u.getUsername().isBlank()) {
            return u.getUsername();
        }
        if (u.getEmail() != null && !u.getEmail().isBlank()) {
            return u.getEmail().toLowerCase();
        }
        // 최후의 수단: 이름 기반 슬러그 + id
        return slug(u.getName()) + "-" + u.getId();
    }

    private String normalizeGroup(String department) {
        // Nextcloud 그룹명은 공백/특수문자 피하는 것이 안전
        return department.trim().toLowerCase().replaceAll("[^a-z0-9_-]", "-");
    }

    private String slug(String s) {
        if (s == null) return "user";
        return s.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
