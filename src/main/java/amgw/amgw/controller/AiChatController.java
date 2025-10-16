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
    private JdbcTemplate jdbcTemplate;

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

        try {
            // 1️⃣ 문자열이 JSON인지 확인 후 파싱
            JsonObject obj = null;
            try {
                obj = JsonParser.parseString(aiResponse).getAsJsonObject();
            } catch(Exception e) {
                int start = aiResponse.indexOf("{");
                int end = aiResponse.lastIndexOf("}");
                if(start >= 0 && end > start) {
                    obj = JsonParser.parseString(aiResponse.substring(start, end + 1)).getAsJsonObject();
                }
            }

            if(obj != null){
                answer = obj.has("answer") ? obj.get("answer").getAsString() : null;
                redirect = obj.has("redirect") ? obj.get("redirect").getAsString() : null;
            }

            // ✅ autoFillData 출력
            if(obj.has("autoFillData")) {
                JsonObject autoFillData = obj.getAsJsonObject("autoFillData");
                Map<String, String> responseMap = Map.of(
                        "answer", answer != null ? answer : "휴가 신청 페이지로 이동합니다.",
                        "redirect", redirect != null ? redirect : "/attendance.html",
                        "type", autoFillData.get("type").getAsString(),
                        "reason", autoFillData.get("reason").getAsString(),
                        "startDate", autoFillData.get("startDate").getAsString(),
                        "endDate", autoFillData.get("endDate").getAsString()
                );
                return responseMap;
            }


            // 2️⃣ 안전하게 기본값 보장
            if(redirect != null && redirect.startsWith("/")) {
                if(answer == null) answer = "페이지로 이동하시겠습니까?";
                return Map.of("answer", answer, "redirect", redirect);
            } else {
                if(answer != null) return Map.of("answer", answer);
                else return Map.of("answer", aiResponse);
            }

        } catch(Exception e){
            e.printStackTrace();
            return Map.of("answer", aiResponse);
        }
    }

    // -------------------------------
    // DB에서 페이지 URL 조회
    // -------------------------------
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

    // -------------------------------
    // Gemini API 호출
    // -------------------------------
    private String callGeminiForAction(String userPrompt) {
        try {
            List<Map<String, Object>> pageList = jdbcTemplate.queryForList("SELECT page_name, url FROM pages");

            StringBuilder dbInfo = new StringBuilder("현재 그룹웨어 페이지 정보:\n");
            for (Map<String, Object> page : pageList) {
                dbInfo.append("- ").append(page.get("page_name"))
                        .append(": ").append(page.get("url")).append("\n");
            }

            String systemPrompt = """
                당신은 그룹웨어 시스템 AI 챗봇입니다.
                - 특정 기능 요청은 확인 후 수행
                - 페이지 이동 시 {"answer":"페이지로 이동하시겠습니까?", "redirect":"/페이지.html"} 형식
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
                       "redirect": "/attendance.html",
                       "autoFillData": {
                         "type": "연차",
                         "reason": "개인 사유",
                         "startDate": "2025-10-15",
                         "endDate": "2025-10-17"
                       }
                     }
                     JSON 구조 외부 답변: 
                     "answer": "휴가 신청 페이지로 이동합니다."
                     페이지 정보가 필요하면 dbInfo를 참고
                """ + dbInfo.toString();

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
}
