package amgw.amgw.controller;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
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
    private JdbcTemplate jdbcTemplate; // DB 조회용

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

        try {
            if (!aiResponse.trim().startsWith("{")) {
                return Map.of("answer", aiResponse);
            }

            JsonReader reader = new JsonReader(new StringReader(aiResponse));
            reader.setLenient(true);
            JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();

            // AI가 특정 페이지 요청으로 판별한 경우
            if (obj.has("page_name")) {
                String pageName = obj.get("page_name").getAsString();
                String pageUrl = getPageUrl(pageName);

                if (pageUrl != null) {
                    return Map.of("answer", pageName + " 페이지로 이동하시겠습니까?", "redirect", pageUrl);
                } else {
                    return Map.of("answer", pageName + " 페이지 정보를 찾을 수 없습니다.");
                }
            }

            String answer = obj.has("answer") ? obj.get("answer").getAsString() : null;
            String redirect = obj.has("redirect") ? obj.get("redirect").getAsString() : null;

            if (answer != null && redirect != null) return Map.of("answer", answer, "redirect", redirect);
            else if (answer != null) return Map.of("answer", answer);
            else if (redirect != null) return Map.of("answer", "페이지로 이동하시겠습니까?", "redirect", redirect);

            return Map.of("answer", aiResponse);

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("answer", aiResponse);
        }
    }

    // -------------------------------
    // DB에서 페이지 URL 조회
    // -------------------------------
    private String getPageUrl(String pageName) {
        try {
            System.out.println("DB 조회 시도: " + pageName);
            String sql = "SELECT url FROM pages WHERE page_name = ?";
            List<String> urls = jdbcTemplate.queryForList(sql, String.class, pageName);
            System.out.println("DB 조회 결과: " + urls);
            System.out.println("[DB 조회] pageName=" + pageName + ", url=" + (urls.isEmpty() ? "없음" : urls.get(0)));
            return urls.isEmpty() ? null : urls.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // -------------------------------
    // Gemini API 호출
    // -------------------------------
    private String callGeminiForAction(String userPrompt) {
        try {
            // 1️⃣ DB에서 페이지 목록 조회
            String sql = "SELECT page_name, url FROM pages";
            List<Map<String, Object>> pageList = jdbcTemplate.queryForList(sql);

            // 2️⃣ DB 정보 문자열로 만들기
            StringBuilder dbInfo = new StringBuilder();
            dbInfo.append("현재 그룹웨어 페이지 정보:\n");
            for (Map<String, Object> page : pageList) {
                dbInfo.append("- ").append(page.get("page_name"))
                        .append(": ").append(page.get("url")).append("\n");
            }

            // 3️⃣ systemPrompt 작성 (DB 정보 포함)
            String systemPrompt = """
                당신은 그룹웨어 시스템 AI 챗봇입니다.
                - 항상 특정 기능 요청을 바로 수행하지 마세요. 필요 시 확인 질문 후 기능을 실행합니다.
                - 사용자가 특정 기능 페이지로 가길 원하면 다음 형식으로 반환:
                  {"answer": "페이지로 이동하시겠습니까?", "redirect": "/페이지.html"}
                - 일반 대화는 {"answer": "대화 내용"} 형식으로 반환
                - 업무 요약 요청 시 {"answer": "업무 요약 내용"} 형식으로 반환
                - 정보 안내 요청 시 {"answer": "안내 메시지"} 형식으로 반환
                - JSON 외 다른 형태로는 절대 응답하지 마세요.

                예시:
                - "전자결재" → {"answer": "전자결재에 대해 어떤 기능이 필요하신가요? 결재를 올리는 건가요, 아니면 결재 문서를 확인하는 건가요?"}
                - "오늘 회의 내용 요약해줘" → {"answer": "오늘 회의에서는 일정 검토, 프로젝트 진행 상황 점검, 신규 업무 배정이 있었습니다."}
                
                """ + dbInfo.toString();

            // 4️⃣ Gemini API 호출 구조는 그대로
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

            URL url = new URL(
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY
            );
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
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }
                return extractTextFromJson(response.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"answer\": \"AI 호출 중 오류가 발생했습니다.\"}";
        }
    }


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
}
