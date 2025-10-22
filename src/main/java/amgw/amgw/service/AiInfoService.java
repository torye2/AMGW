// src/main/java/amgw/amgw/service/AiInfoService.java
package amgw.amgw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AiInfoService {

    private final JdbcTemplate jdbc;

    // ========= 외부에서 호출하는 메인 빌더 =========
    /** 로그인 사용자 관점의 모든 주요 정보를 한 번에 요약(길이 제한 포함) */
    public String buildRichContext(Long userId) {
        StringBuilder sb = new StringBuilder(4096);

        // 0) 시스템/페이지 요약(선택)
        appendSection(sb, "시스템 페이지", pagesMap());

        // 1) 사용자 요약
        appendUserSummary(sb, userId);

        // 2) 알림
        appendNotifications(sb, userId, 8);

        // 3) 전자결재
        appendApprovals(sb, userId, 8);

        // 4) 근태(오늘/주간/최근 신청)
        appendAttendance(sb, userId);

        // 5) 캘린더(다가오는 일정)
        appendCalendar(sb, userId, 8);

        // 6) 문서(최근 내 문서/버전/공유)
        appendDocuments(sb, userId, 8);

        // 7) 공지(최근)
        appendNotices(sb, 8);

        // 8) 채팅(내 방/미확인/최근 메시지)
        appendChat(sb, userId, 8);

        // 9) To-Do
        appendTodos(sb, userId, 10);

        // 10) 칭찬 게시물 (있다면)
        appendCompliments(sb, userId, 5);

        // 11) (옵션) 사내 디렉터리 스냅샷
        appendDirectorySnapshot(sb, 40);

        // 길이 방어(과도한 프롬프트 방지)
        return truncate(sb.toString(), 12000);
    }

    // ========= 개별 섹션 빌더 =========
    private void appendUserSummary(StringBuilder sb, Long userId) {
        var u = findUser(userId);
        sb.append("로그인 사용자 요약:\n");
        if (u.isEmpty()) {
            sb.append("- (조회 실패)\n\n");
            return;
        }
        sb.append("- id: ").append(n(u.get("id"))).append("\n");
        sb.append("- username: ").append(n(u.get("username"))).append("\n");
        sb.append("- name: ").append(n(u.get("name"))).append("\n");
        sb.append("- email: ").append(n(u.get("email"))).append("\n");
        sb.append("- department: ").append(n(u.get("department"))).append("\n");
        sb.append("- position: ").append(n(u.get("position"))).append("\n");
        sb.append("- role: ").append(n(u.get("role"))).append("\n");
        sb.append("- status_code: ").append(n(u.get("status_code"))).append("\n");
        sb.append("- created_at: ").append(n(u.get("created_at"))).append("\n");
        sb.append("- last_login_at: ").append(n(u.get("last_login_at"))).append("\n\n");
    }

    private void appendNotifications(StringBuilder sb, Long userId, int limit) {
        int unread = unreadNotifications(userId);
        sb.append("알림 현황:\n");
        sb.append("- 읽지 않은 알림: ").append(unread).append("개\n");
        var list = recentNotifications(userId, limit);
        if (list.isEmpty()) sb.append("- 최근 알림 없음\n\n");
        else {
            for (var r : list) {
                sb.append("  • [").append(n(r.get("type"))).append("] ")
                        .append(n(r.get("summary"))).append(" / ")
                        .append(n(r.get("created_at"))).append("\n");
            }
            sb.append("\n");
        }
    }

    private void appendApprovals(StringBuilder sb, Long userId, int limit) {
        // 인박스(승인자 기준 SUBMITTED)
        var inbox = approvalInbox(userId, limit);
        sb.append("전자결재 - 결재 대기(승인자 기준):\n");
        if (inbox.isEmpty()) sb.append("- (없음)\n");
        inbox.forEach(a -> sb.append("  • #").append(n(a.get("id")))
                .append(" [").append(n(a.get("doc_type"))).append("] ")
                .append(n(a.get("title"))).append(" / 상태: ").append(n(a.get("status")))
                .append(" / 기안자: ").append(n(a.get("drafter_name")))
                .append(" / 작성: ").append(n(a.get("created_at"))).append("\n"));

        // 아웃박스(내가 올린 최근 문서)
        var outbox = myRecentDrafts(userId, limit);
        sb.append("전자결재 - 내가 기안한 최근 문서:\n");
        if (outbox.isEmpty()) sb.append("- (없음)\n");
        outbox.forEach(a -> sb.append("  • #").append(n(a.get("id")))
                .append(" [").append(n(a.get("doc_type"))).append("] ")
                .append(n(a.get("title"))).append(" / 상태: ").append(n(a.get("status")))
                .append(" / 승인자: ").append(n(a.get("approver_name")))
                .append(" / 작성: ").append(n(a.get("created_at"))).append("\n"));
        sb.append("\n");
    }

    private void appendAttendance(StringBuilder sb, Long userId) {
        sb.append("근태:\n");
        // 오늘
        var today = todayAttendance(userId);
        if (today.isEmpty()) sb.append("- 오늘 기록: 없음\n");
        else {
            var a = today.get(0);
            sb.append("- 오늘: 출근 ").append(n(a.get("check_in_at")))
                    .append(" / 퇴근 ").append(n(a.get("check_out_at")))
                    .append(" / 근무(분) ").append(n(a.get("worked_minutes"))).append("\n");
        }
        // 주간 합계(예시: 금주)
        var week = weeklySummary(userId);
        sb.append("- 이번주 근무합계(분): ").append(week).append("\n");

        // 최근 근태 신청 5건
        var reqs = recentAttendanceRequests(userId, 5);
        sb.append("- 최근 근태 신청:\n");
        if (reqs.isEmpty()) sb.append("  • (없음)\n");
        reqs.forEach(r -> sb.append("  • #").append(n(r.get("id"))).append(" ")
                .append(n(r.get("type"))).append(" ")
                .append(n(r.get("start_date"))).append("~").append(n(r.get("end_date")))
                .append(" / 상태: ").append(n(r.get("status")))
                .append(" / 사유: ").append(n(r.get("reason"))).append("\n"));
        sb.append("\n");
    }

    private void appendCalendar(StringBuilder sb, Long userId, int limit) {
        var evts = upcomingEvents(userId, limit);
        sb.append("다가오는 일정:\n");
        if (evts.isEmpty()) sb.append("- (없음)\n\n");
        else {
            evts.forEach(e -> sb.append("  • ")
                    .append(n(e.get("start_utc"))).append("~").append(n(e.get("end_utc")))
                    .append(" | ").append(n(e.get("title")))
                    .append(" @ ").append(n(e.get("location"))).append("\n"));
            sb.append("\n");
        }
    }

    private void appendDocuments(StringBuilder sb, Long userId, int limit) {
        var docs = recentDocs(userId, limit);
        sb.append("문서(내가 소유한 최근 문서):\n");
        if (docs.isEmpty()) sb.append("- (없음)\n");
        docs.forEach(d -> sb.append("  • #").append(n(d.get("doc_id")))
                .append(" ").append(n(d.get("title")))
                .append(" (").append(n(d.get("mime_type"))).append(", ")
                .append(n(d.get("size_bytes"))).append(" bytes)")
                .append(" / ver ").append(n(d.get("version")))
                .append(" / ").append(n(d.get("created_at"))).append("\n"));

        var shares = recentShares(userId, 5);
        sb.append("문서 공유(최근):\n");
        if (shares.isEmpty()) sb.append("- (없음)\n");
        shares.forEach(s -> sb.append("  • doc#").append(n(s.get("doc_id")))
                .append(" / role: ").append(n(s.get("role")))
                .append(" / 대상: ").append(n(s.get("subject_type")))
                .append(" / 생성: ").append(n(s.get("created_at"))).append("\n"));
        sb.append("\n");
    }

    private void appendNotices(StringBuilder sb, int limit) {
        var list = recentNotices(limit);
        sb.append("최근 공지:\n");
        if (list.isEmpty()) sb.append("- (없음)\n\n");
        else {
            list.forEach(n -> sb.append("  • ").append(n(n.get("notice_title")))
                    .append(" (작성자: ").append(n(n.get("author_name"))).append(")")
                    .append(" / ").append(n(n.get("registration_time"))).append("\n"));
            sb.append("\n");
        }
    }

    private void appendChat(StringBuilder sb, Long userId, int limit) {
        // 내가 속한 방
        var rooms = myRecentRooms(userId, limit);
        sb.append("채팅방(내가 속한 최근):\n");
        if (rooms.isEmpty()) sb.append("- (없음)\n");
        rooms.forEach(r -> sb.append("  • #").append(n(r.get("room_id")))
                .append(" [").append(n(r.get("type"))).append("] ")
                .append(coalesce((String) r.get("name"), "(이름 없음)"))
                .append("\n"));

        // 방별 내 미읽음 카운트(간단 합계)
        int unreadSum = totalUnreadMessages(userId);
        sb.append("- 미확인 메시지 합계: ").append(unreadSum).append("\n");

        // 최근 메시지(내 방들 기준)
        var msgs = recentMessages(userId, 10);
        sb.append("최근 채팅 메시지(10):\n");
        if (msgs.isEmpty()) sb.append("  • (없음)\n");
        msgs.forEach(m -> sb.append("  • room#").append(n(m.get("room_id")))
                .append(" / sender#").append(n(m.get("sender_id")))
                .append(" / ").append(crop(n(m.get("content")), 80))
                .append(" / ").append(n(m.get("created_at"))).append("\n"));
        sb.append("\n");
    }

    // ===== 할일 섹션 렌더링 =====
    private void appendTodos(StringBuilder sb, Long userId, int limit) {
        var rows = jdbc.queryForList("""
        SELECT id, title, done, due_date, priority, sort_order, created_at, updated_at
        FROM todo
        WHERE user_id=?
        ORDER BY done ASC, COALESCE(sort_order, 999999), created_at DESC
        LIMIT ?
    """, userId, limit);

        sb.append("할 일(상위 ").append(limit).append("):\n");
        if (rows.isEmpty()) { sb.append("- (없음)\n\n"); return; }

        rows.forEach(r -> {
            boolean done = asBool(r.get("done"));  // <-- 안전 변환
            sb.append("  • #").append(s(r.get("id")))
                    .append(" ").append(s(r.get("title")))
                    .append(" [").append(done ? "완료" : "미완료").append("]")
                    .append(" / 우선순위: ").append(s(r.get("priority")));
            if (r.get("due_date") != null) sb.append(" / 마감: ").append(s(r.get("due_date")));
            sb.append("\n");
        });
        sb.append("\n");
    }

    private void appendCompliments(StringBuilder sb, Long userId, int limit) {
        var list = recentCompliments(userId, limit);
        sb.append("칭찬 게시물(내가 작성/관련):\n");
        if (list.isEmpty()) sb.append("- (없음)\n\n");
        else {
            list.forEach(c -> sb.append("  • #").append(n(c.get("compliment_id")))
                    .append(" ").append(n(c.get("compliment_title")))
                    .append(" / 칭찬수 ").append(n(c.get("compliment_count")))
                    .append(" / ").append(n(c.get("registration_time"))).append("\n"));
            sb.append("\n");
        }
    }

    private void appendDirectorySnapshot(StringBuilder sb, int limit) {
        var list = directorySnapshot(limit);
        sb.append("사내 사용자 디렉터리(상위 ").append(limit).append("명):\n");
        if (list.isEmpty()) sb.append("- (없음)\n");
        list.forEach(u -> sb.append("  • #").append(n(u.get("id")))
                .append(" ").append(n(u.get("name"))).append(" (").append(n(u.get("username"))).append(")")
                .append(" / 부서: ").append(coalesce((String) u.get("department"), "미지정"))
                .append(" / 직위: ").append(coalesce((String) u.get("position"), "-"))
                .append(" / 권한: ").append(n(u.get("role")))
                .append(" / 상태: ").append(n(u.get("status_code")))
                .append(" / 이메일: ").append(n(u.get("email"))).append("\n"));
        sb.append("\n");
    }

    // 리스트용(필드 나열)
    private void appendSectionRows(StringBuilder sb, String title, List<Map<String,Object>> rows, String... fields) {
        sb.append(title).append(":\n");
        if (rows.isEmpty()) { sb.append("- (없음)\n\n"); return; }
        for (var r : rows) {
            sb.append("  • ");
            for (int i=0;i<fields.length;i++) {
                if (i>0) sb.append(" | ");
                sb.append(fields[i]).append(": ").append(n(r.get(fields[i])));
            }
            sb.append("\n");
        }
        sb.append("\n");
    }

    // 맵용(키:값 나열) — 이름을 다르게!
    private void appendSectionMap(StringBuilder sb, String title, Map<String,String> map) {
        sb.append(title).append(":\n");
        if (map == null || map.isEmpty()) { sb.append("- (없음)\n\n"); return; }
        map.forEach((k,v)-> sb.append("  • ").append(k).append(": ").append(v).append("\n"));
        sb.append("\n");
    }


    // ========= 조회 메서드 =========
    public Map<String,Object> findUser(Long userId) {
        String sql = """
            SELECT id, username, name, email, department, position, role, status_code, created_at, last_login_at
            FROM users WHERE id = ?
        """;
        var list = jdbc.queryForList(sql, userId);
        return list.isEmpty() ? Map.of() : list.get(0);
    }

    public int unreadNotifications(Long userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id=? AND read_flag='N'";
        Integer cnt = jdbc.queryForObject(sql, Integer.class, userId);
        return cnt == null ? 0 : cnt;
    }

    public List<Map<String,Object>> recentNotifications(Long userId, int limit) {
        String sql = """
            SELECT id, type, summary, data, read_flag, created_at
            FROM notifications
            WHERE user_id=?
            ORDER BY created_at DESC
            LIMIT ?
        """;
        return jdbc.queryForList(sql, userId, limit);
    }

    public List<Map<String,Object>> approvalInbox(Long approverId, int limit) {
        String sql = """
            SELECT d.id, d.title, d.doc_type, d.status, d.created_at, d.updated_at,
                   d.drafter_id, d.approver_id, ud.name AS drafter_name
            FROM approval_doc d
            JOIN users ud ON ud.id = d.drafter_id
            WHERE d.approver_id = ? AND d.status = 'SUBMITTED'
            ORDER BY d.created_at DESC
            LIMIT ?
        """;
        return jdbc.queryForList(sql, approverId, limit);
    }

    public List<Map<String,Object>> myRecentDrafts(Long userId, int limit) {
        String sql = """
            SELECT d.id, d.title, d.doc_type, d.status, d.created_at,
                   d.drafter_id, d.approver_id, ua.name AS approver_name
            FROM approval_doc d
            JOIN users ua ON ua.id = d.approver_id
            WHERE d.drafter_id = ?
            ORDER BY d.created_at DESC
            LIMIT ?
        """;
        return jdbc.queryForList(sql, userId, limit);
    }

    public List<Map<String,Object>> todayAttendance(Long userId) {
        String sql = """
            SELECT user_id, work_date, check_in_at, check_out_at,
                   TIMESTAMPDIFF(MINUTE, check_in_at, IFNULL(check_out_at, NOW())) AS worked_minutes
            FROM attendance_log
            WHERE user_id=? AND work_date=CURDATE()
            LIMIT 1
        """;
        return jdbc.queryForList(sql, userId);
    }

    public int weeklySummary(Long userId) {
        String sql = """
            SELECT COALESCE(SUM(
                     TIMESTAMPDIFF(MINUTE, check_in_at, IFNULL(check_out_at, NOW()))
                   ), 0) AS minutes
            FROM attendance_log
            WHERE user_id=? AND work_date >= (CURDATE() - INTERVAL (WEEKDAY(CURDATE())) DAY)
        """;
        Integer m = jdbc.queryForObject(sql, Integer.class, userId);
        return m == null ? 0 : m;
    }

    public List<Map<String,Object>> recentAttendanceRequests(Long userId, int limit) {
        String sql = """
            SELECT id, type, status, start_date, end_date, start_time, end_time, reason, approver_id, created_at
            FROM attendance_request
            WHERE user_id=?
            ORDER BY created_at DESC
            LIMIT ?
        """;
        return jdbc.queryForList(sql, userId, limit);
    }

    public List<Map<String,Object>> upcomingEvents(Long userId, int limit) {
        String sql = """
            SELECT id, title, location, start_utc, end_utc, all_day
            FROM calendar_event
            WHERE user_id=? AND start_utc >= (NOW() - INTERVAL 12 HOUR)
            ORDER BY start_utc ASC
            LIMIT ?
        """;
        return jdbc.queryForList(sql, userId, limit);
    }

    public List<Map<String,Object>> recentNotices(int limit) {
        String sql = """
            SELECT n.notice_id, n.notice_title, n.important, n.registration_time, u.name AS author_name
            FROM notice n
            JOIN users u ON u.id = n.user_id
            ORDER BY n.registration_time DESC
            LIMIT ?
        """;
        return jdbc.queryForList(sql, limit);
    }

    public List<Map<String,Object>> recentDocs(Long ownerId, int limit) {
        String sql = """
            SELECT doc_id, title, mime_type, size_bytes, version, created_at
            FROM documents
            WHERE owner_id=?
            ORDER BY created_at DESC
            LIMIT ?
        """;
        return jdbc.queryForList(sql, ownerId, limit);
    }

    public List<Map<String,Object>> recentShares(Long createdBy, int limit) {
        String sql = """
            SELECT doc_id, subject_type, subject_id, role, link_token, expires_at, created_at
            FROM document_shares
            WHERE created_by=?
            ORDER BY created_at DESC
            LIMIT ?
        """;
        return jdbc.queryForList(sql, createdBy, limit);
    }

    public List<Map<String,Object>> myRecentRooms(Long userId, int limit) {
        String sql = """
            SELECT r.id AS room_id, r.type, r.name, r.created_at
            FROM chat_room_member m
            JOIN chat_room r ON r.id = m.room_id
            WHERE m.user_id = ?
            ORDER BY r.created_at DESC
            LIMIT ?
        """;
        return jdbc.queryForList(sql, userId, limit);
    }

    public int totalUnreadMessages(Long userId) {
        // 매우 러프한 미읽음 합계: 각 방의 마지막 메시지 시각 이후 내 읽음 유무로 근사치
        String sql = """
            SELECT COUNT(*)
            FROM chat_message cm
            JOIN chat_room_member m ON m.room_id = cm.room_id AND m.user_id = ?
            LEFT JOIN chat_message_read mr ON mr.message_id = cm.id AND mr.user_id = ?
            WHERE mr.user_id IS NULL
        """;
        Integer cnt = jdbc.queryForObject(sql, Integer.class, userId, userId);
        return cnt == null ? 0 : cnt;
    }

    public List<Map<String,Object>> recentMessages(Long userId, int limit) {
        String sql = """
            SELECT cm.id, cm.room_id, cm.sender_id, cm.content, cm.content_type, cm.created_at
            FROM chat_message cm
            JOIN chat_room_member m ON m.room_id = cm.room_id AND m.user_id = ?
            ORDER BY cm.created_at DESC
            LIMIT ?
        """;
        return jdbc.queryForList(sql, userId, limit);
    }

    public List<Map<String,Object>> recentTodos(Long userId, int limit) {
        String sql = """
            SELECT id, title, done, due_date, priority, sort_order, created_at, updated_at
            FROM todo
            WHERE user_id=?
            ORDER BY done ASC, COALESCE(sort_order, 999999), created_at DESC
            LIMIT ?
        """;
        return jdbc.queryForList(sql, userId, limit);
    }

    public List<Map<String,Object>> recentCompliments(Long userId, int limit) {
        String sql = """
            SELECT compliment_id, compliment_title, compliment_count, registration_time
            FROM compliment
            WHERE user_id=?
            ORDER BY registration_time DESC
            LIMIT ?
        """;
        return jdbc.queryForList(sql, userId, limit);
    }

    public Map<String,String> pagesMap() {
        String sql = "SELECT page_name, url FROM pages ORDER BY id ASC";
        Map<String,String> m = new LinkedHashMap<>();
        jdbc.queryForList(sql).forEach(row -> m.put(
                String.valueOf(row.get("page_name")),
                String.valueOf(row.get("url"))
        ));
        return m;
    }

    public List<Map<String,Object>> directorySnapshot(int limit) {
        String sql = """
            SELECT id, username, name, email, department, position, role, status_code
            FROM users
            ORDER BY department IS NULL, department ASC, name ASC
            LIMIT ?
        """;
        return jdbc.queryForList(sql, limit);
    }

    // ========= 유틸 =========
    private String n(Object o) { return o == null ? "" : String.valueOf(o); }
    private String coalesce(String v, String def) { return (v==null || v.isBlank()) ? def : v; }

    private String crop(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max-1) + "…" : s;
    }

    private void appendSection(StringBuilder sb, String title, List<Map<String,Object>> rows, String... fields) {
        sb.append(title).append(":\n");
        if (rows.isEmpty()) { sb.append("- (없음)\n\n"); return; }
        for (var r : rows) {
            sb.append("  • ");
            for (int i=0;i<fields.length;i++) {
                if (i>0) sb.append(" | ");
                sb.append(fields[i]).append(": ").append(n(r.get(fields[i])));
            }
            sb.append("\n");
        }
        sb.append("\n");
    }

    private void appendSection(StringBuilder sb, String title, Map<String,String> map) {
        sb.append(title).append(":\n");
        if (map.isEmpty()) { sb.append("- (없음)\n\n"); return; }
        map.forEach((k,v)-> sb.append("  • ").append(k).append(": ").append(v).append("\n"));
        sb.append("\n");
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max-1) + "…" : s;
    }

    // ===== 안전 캐스팅 유틸 =====
    private boolean asBool(Object v) {
        if (v == null) return false;
        if (v instanceof Boolean) return (Boolean) v;
        if (v instanceof Number) return ((Number) v).intValue() != 0;
        return "true".equalsIgnoreCase(v.toString()) || "1".equals(v.toString());
    }
    private String s(Object v) { return v == null ? "" : String.valueOf(v); }

}
