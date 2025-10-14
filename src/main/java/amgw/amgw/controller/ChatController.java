package amgw.amgw.controller;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

@Controller
@RequestMapping("/chat")
public class ChatController {

    private static final Map<String, String> pageMap = Map.of(
            "전자결재", "/approval.html",
            "근태관리", "/attendance.html",
            "메신저", "/messenger.html",
            "파일공유", "/file-share.html"
    );

    private static final String API_KEY = "AIzaSyCdkVsuY8DVF92MG2I_J2hitV7uzjEVyPA";

    @GetMapping
    public String chatPage() {
        return "chat";
    }

    @PostMapping("/api")
    @ResponseBody
    public Map<String, String> chat(@RequestBody Map<String, String> payload) {
        String question = payload.get("question");
        String aiResponse = callGeminiForAction(question);

        // 🔹 안전 파싱
        try {
            if (!aiResponse.trim().startsWith("{")) {
                // JSON 아닌 경우 바로 answer로 전달
                return Map.of("answer", aiResponse);
            }

            JsonReader reader = new JsonReader(new StringReader(aiResponse));
            reader.setLenient(true);
            JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();

            if (obj.has("redirect") && !obj.get("redirect").getAsString().isEmpty()) {
                return Map.of("redirect", obj.get("redirect").getAsString());
            }

            if (obj.has("answer")) {
                return Map.of("answer", obj.get("answer").getAsString());
            }

            return Map.of("answer", aiResponse);

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
            URL url = new URL(
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY
            );
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            // 🔹 AI에게 반드시 JSON으로 응답하도록 지시
            String systemPrompt = """
                    당신은 그룹웨어 시스템 AI 챗봇입니다.
                    사용자의 질문에 반드시 JSON 형식으로 답변하세요.
                    - 특정 기능 요청 → {"redirect": "/페이지.html"}
                    - 일반 대화 → {"answer": "대화 내용"}
                    JSON 외 다른 형태로는 절대 응답하지 마세요.
                    """;

            JsonObject jsonRoot = new JsonObject();

            // system_instruction
            JsonObject systemInstruction = new JsonObject();
            JsonArray systemParts = new JsonArray();
            JsonObject sysPart = new JsonObject();
            sysPart.addProperty("text", systemPrompt);
            systemParts.add(sysPart);
            systemInstruction.add("parts", systemParts);
            jsonRoot.add("system_instruction", systemInstruction);

            // contents
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

            // 전송
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
    // Gemini 응답에서 text 추출
    // -------------------------------
    private String extractTextFromJson(String json) {
        try {
            JsonReader reader = new JsonReader(new StringReader(json));
            reader.setLenient(true);
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            JsonArray candidates = null;
            if (root.has("response")) {
                candidates = root.getAsJsonObject("response").getAsJsonArray("candidates");
            } else if (root.has("candidates")) {
                candidates = root.getAsJsonArray("candidates");
            }

            if (candidates != null && !candidates.isEmpty()) {
                JsonObject candidate = candidates.get(0).getAsJsonObject();

                if (candidate.has("content")) {
                    JsonArray parts = candidate.getAsJsonObject("content").getAsJsonArray("parts");
                    if (parts != null && !parts.isEmpty()) {
                        return parts.get(0).getAsJsonObject().get("text").getAsString();
                    }
                }

                if (candidate.has("outputText")) {
                    return candidate.get("outputText").getAsString();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "(AI 응답 파싱 실패)";
    }
}
