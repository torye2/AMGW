package amgw.amgw.controller;

import amgw.amgw.config.CustomUserDetails;
import amgw.amgw.service.AiInfoService;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/aiChat")
public class AiChatController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AiInfoService aiInfoService;

    private static final String API_KEY = "AIzaSyCdkVsuY8DVF92MG2I_J2hitV7uzjEVyPA";

    @GetMapping
    public String chatPage() {
        return "aiChat";
    }

    @PostMapping("/aiApi")
    @ResponseBody
    public Map<String, String> chat(@RequestBody Map<String, String> payload) {
        String question = payload.get("question");
        String aiResponse = callGeminiForAction(question);

        String answer = null;
        String redirect = null;
        String action = null;
        String status = null;

        try {
            // 1) 문자열을 JSON으로 안전하게 파싱
            JsonObject obj = null;
            try {
                obj = JsonParser.parseString(aiResponse).getAsJsonObject();
            } catch (Exception e) {
                int start = aiResponse.indexOf("{");
                int end = aiResponse.lastIndexOf("}");
                if (start >= 0 && end > start) {
                    obj = JsonParser.parseString(aiResponse.substring(start, end + 1)).getAsJsonObject();
                }
            }

            if (obj != null) {
                if (obj.has("answer") && !obj.get("answer").isJsonNull()) {
                    answer = obj.get("answer").getAsString();
                }
                if (obj.has("redirect") && !obj.get("redirect").isJsonNull()) {
                    redirect = obj.get("redirect").getAsString();
                }
                if (obj.has("action") && !obj.get("action").isJsonNull()) {
                    action = obj.get("action").getAsString();
                }
            }

            // 2) autoFillData (근태 자동입력)
            if (obj != null && obj.has("autoFillData") && obj.get("autoFillData").isJsonObject()) {
                JsonObject autoFillData = obj.getAsJsonObject("autoFillData");
                return Map.of(
                        "answer",  (answer != null ? answer : "휴가 신청 페이지로 이동합니다."),
                        "redirect", (redirect != null ? redirect : "/attendance"),
                        "type",     autoFillData.has("type")      ? autoFillData.get("type").getAsString()      : "",
                        "reason",   autoFillData.has("reason")    ? autoFillData.get("reason").getAsString()    : "",
                        "startDate",autoFillData.has("startDate") ? autoFillData.get("startDate").getAsString() : "",
                        "endDate",  autoFillData.has("endDate")   ? autoFillData.get("endDate").getAsString()   : ""
                );
            }

            // 3) 출퇴근 처리
            if ("checkIn".equals(action) || "checkOut".equals(action)) {
                status = markAttendance(action); // DB 업데이트 + status 반환
                if (answer == null) {
                    answer = status.equals("ok")
                            ? "출퇴근 처리되었습니다."
                            : "이미 오늘 " + ("checkIn".equals(action) ? "출근" : "퇴근") + " 기록이 있습니다.";
                }
                return Map.of(
                        "answer", answer,
                        "action", action,
                        "status", status
                );
            }

            // 4) 페이지 이동 or 일반 응답
            if (redirect != null && redirect.startsWith("/")) {
                if (answer == null) answer = "페이지로 이동하시겠습니까?";
                return Map.of("answer", answer, "redirect", redirect);
            } else {
                return Map.of("answer", (answer != null ? answer : aiResponse));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("answer", aiResponse);
        }
    }

    // -------------------------------
    // Gemini API 호출
    // -------------------------------
    private String callGeminiForAction(String userPrompt) {
        try {
            // 페이지 목록 요약
            List<Map<String, Object>> pageList = jdbcTemplate.queryForList("SELECT page_name, url FROM pages");
            StringBuilder dbInfo = new StringBuilder("현재 그룹웨어 페이지 정보:\n");
            for (Map<String, Object> page : pageList) {
                dbInfo.append("- ").append(page.get("page_name")).append(": ").append(page.get("url")).append("\n");
            }

            // 로그인 사용자 컨텍스트
            Long userId = ((CustomUserDetails) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal()).getUserId();

            // 나머지 컨텍스트
            String context = aiInfoService.buildRichContext(userId);



            String systemPrompt = """
                    당신은 그룹웨어 시스템 AI 챗봇입니다. 사용할 각 기능을 상황에 맞춰서 적절히 사용하세요.
                                    - 특정 기능 요청은 확인 후 수행
                                    - 결재 내용이나 회의 내용, 문서의 파일 내용 분석등 장문의 글을 보낼시에 내용을 요약하는 기능도 수행해야합니다.
                                    - 일반 대화시에 db 내용을 조회해서 대답해야하는 상황이 있습니다. db 내용은 dbInfo와 context를 참고하세요. db 내용을 참고할땐 절대로 내용에 오류가 있어서는 안됩니다. 항상 참이 되는 진실만을 답변하세요. notifications 테이블 type에 approval과 chat가 있는데 approval은 결재 내용이고 chat은 일반 유저끼리의 채팅입니다.
                                    - 페이지 이동 시 {"answer":"페이지로 이동하시겠습니까?", "redirect":"/페이지"} 형식
                                    - 일반 대화는 {"answer":"대화 내용"} 형식
                                    - JSON 외 응답 금지
                                    - 사용자가 '휴가 신청', '근태 등록', '외근 신청' 등 '근태 관리' 기능을 요청하면 반드시 autoFillData 필드를 JSON 형식으로 작성하세요. 추가로 일반 대화도 작성하세요. 일반 대화는 autoFillData 필드 외부에 작성 됩니다.
                                    - autoFillData 필드 의미:
                                            - type: 휴가 종류 (연차, 병가 등)
                                            - reason: 휴가 사유
                                            - startDate: 시작 날짜 (YYYY-MM-DD)
                                            - endDate: 종료 날짜 (YYYY-MM-DD)
                                         JSON 구조 예시:
                                         {
                                           "redirect": "/attendance",
                                           "autoFillData": {
                                             "type": "연차",
                                             "reason": "개인 사유",
                                             "startDate": "2025-10-15",
                                             "endDate": "2025-10-17"
                                           }
                                         }
                                         JSON 구조 외부 답변:\s
                                         "answer": "휴가 신청 페이지로 이동합니다."
                                         - 사용자가 '출근', '퇴근'을 입력하면 JSON 형식으로 {"action": "checkIn"} 또는 {"action": "checkOut"} 반환
                                         - 출퇴근 요청 시 {"answer":"출퇴근 처리되었습니다.","action":"checkIn" 또는 "checkOut"} 형식
                    
                                         페이지 정보가 필요하면 dbInfo를 참고

                                        [페이지 정보]
                                        """ + dbInfo + """
                                
                                        [업무 컨텍스트]
                                        """ + context + """
                                
                                        [로그인 사용자 컨텍스트]
                                        """ + userId;

            // ===== Gemini 요청 JSON 구성 =====
            JsonObject jsonRoot = new JsonObject();

            JsonObject systemInstruction = new JsonObject();
            JsonArray systemParts = new JsonArray();
            JsonObject sysPart = new JsonObject();
            sysPart.addProperty("text", systemPrompt);
            systemParts.add(sysPart);
            systemInstruction.add("parts", systemParts);
            jsonRoot.add("system_instruction", systemInstruction);

            JsonArray contents = new JsonArray();
            JsonObject contentObj = new JsonObject();
            contentObj.addProperty("role", "user");
            JsonArray userParts = new JsonArray();
            JsonObject userPart = new JsonObject();
            userPart.addProperty("text", userPrompt);
            userParts.add(userPart);
            contentObj.add("parts", userParts);
            contents.add(contentObj);
            jsonRoot.add("contents", contents);

            URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonRoot.toString().getBytes("utf-8"));
            }

            InputStream is = (conn.getResponseCode() == 200) ? conn.getInputStream() : conn.getErrorStream();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) response.append(line.trim());
                return extractTextFromJson(response.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"answer\": \"AI 호출 중 오류가 발생했습니다.\"}";
        }
    }

    // -------------------------------
    // AI 응답에서 text 추출
    // -------------------------------
    private String extractTextFromJson(String json) {
        try {
            JsonReader reader = new JsonReader(new StringReader(json));
            reader.setLenient(true);
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            JsonArray candidates = null;
            if (root.has("response")) candidates = root.getAsJsonObject("response").getAsJsonArray("candidates");
            else if (root.has("candidates")) candidates = root.getAsJsonArray("candidates");

            if (candidates != null && !candidates.isEmpty()) {
                JsonObject candidate = candidates.get(0).getAsJsonObject();
                if (candidate.has("content")) {
                    JsonArray parts = candidate.getAsJsonObject("content").getAsJsonArray("parts");
                    if (parts != null && !parts.isEmpty())
                        return parts.get(0).getAsJsonObject().get("text").getAsString();
                }
                if (candidate.has("outputText")) return candidate.get("outputText").getAsString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "(AI 응답 파싱 실패)";
    }

    // -------------------------------
    // 출퇴근 기록 처리
    // -------------------------------
    private String markAttendance(String action) {
        Long userId = ((CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getUserId();

        String checkSql = "SELECT id, check_in_at, check_out_at FROM attendance_log WHERE user_id = ? AND work_date = CURDATE()";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(checkSql, userId);

        if (list.isEmpty()) {
            String insertIn  = "INSERT INTO attendance_log(user_id, work_date, check_in_at, created_at, updated_at) VALUES (?, CURDATE(), ?, NOW(), NOW())";
            String insertOut = "INSERT INTO attendance_log(user_id, work_date, check_out_at, created_at, updated_at) VALUES (?, CURDATE(), ?, NOW(), NOW())";
            if ("checkIn".equals(action))
                jdbcTemplate.update(insertIn, userId, new java.sql.Timestamp(System.currentTimeMillis()));
            else if ("checkOut".equals(action))
                jdbcTemplate.update(insertOut, userId, new java.sql.Timestamp(System.currentTimeMillis()));
            return "ok";
        } else {
            Map<String, Object> record = list.get(0);
            if ("checkIn".equals(action)) {
                if (record.get("check_in_at") == null) {
                    jdbcTemplate.update("UPDATE attendance_log SET check_in_at = ?, updated_at = NOW() WHERE id = ?",
                            new java.sql.Timestamp(System.currentTimeMillis()), record.get("id"));
                    return "ok";
                } else return "already";
            } else if ("checkOut".equals(action)) {
                if (record.get("check_out_at") == null) {
                    jdbcTemplate.update("UPDATE attendance_log SET check_out_at = ?, updated_at = NOW() WHERE id = ?",
                            new java.sql.Timestamp(System.currentTimeMillis()), record.get("id"));
                    return "ok";
                } else return "already";
            }
        }
        return "already";
    }


    private String getPageUrl(String pageName) {
        try {
            String sql = "SELECT url FROM pages WHERE page_name = ?";
            List<String> urls = jdbcTemplate.queryForList(sql, String.class, pageName);
            return urls.isEmpty() ? null : urls.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
