package amgw.amgw.controller;

import com.google.gson.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

@Controller
@RequestMapping("/chat") // 브라우저용 페이지 + API 통합
public class ChatController {

    private static final Map<String, String> pageMap = Map.of(
            "전자 결재", "/approval.html",
            "근태 관리", "/attendance.html",
            "메신저", "/messenger.html",
            "파일 공유", "/file-share.html"
    );

    private static final String API_KEY = "AIzaSyCdkVsuY8DVF92MG2I_J2hitV7uzjEVyPA";

    // ------------------------
    // 1️⃣ 브라우저 GET 요청: chat.html 렌더링
    // ------------------------
    @GetMapping
    public String chatPage() {
        return "chat"; // templates/chat.html
    }

    // ------------------------
    // 2️⃣ JS POST 요청: AI 질문 처리
    // ------------------------
    @PostMapping("/api")
    @ResponseBody
    public Map<String, String> chat(@RequestBody Map<String, String> payload) {
        String question = payload.get("question");

        // 페이지 이동 키워드 감지
        for (String key : pageMap.keySet()) {
            if (question.contains(key)) {
                return Map.of("redirect", pageMap.get(key));
            }
        }

        // Gemini API 호출
        String answer = callGeminiApi(question);

        return Map.of("answer", answer);
    }

    // ------------------------
    // 3️⃣ Gemini API 호출
    // ------------------------
    private String callGeminiApi(String prompt) {
        try {
            URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            String jsonInput = """
                    {
                      "system_instruction": {
                        "parts": [{ "text": "당신은 친근하게 대화하는 AI 챗봇입니다." }]
                      },
                      "contents": [{
                        "role": "user",
                        "parts": [{ "text": "%s" }]
                      }]
                    }
                    """.formatted(prompt.replace("\n", " "));

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.getBytes("utf-8"));
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
            return "AI 호출 중 오류 발생";
        }
    }

    private String extractTextFromJson(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            JsonArray candidates = obj.getAsJsonArray("candidates");
            if (candidates != null && candidates.size() > 0) {
                JsonObject candidate = candidates.get(0).getAsJsonObject();
                if (candidate.has("content")) {
                    JsonObject content = candidate.getAsJsonObject("content");
                    if (content.has("parts")) {
                        JsonArray parts = content.getAsJsonArray("parts");
                        if (parts.size() > 0 && parts.get(0).getAsJsonObject().has("text")) {
                            return parts.get(0).getAsJsonObject().get("text").getAsString();
                        }
                    }
                }
            }
        } catch (Exception e) {
            return "(파싱 중 오류 발생)";
        }
        return "(응답에서 텍스트를 찾을 수 없습니다)";
    }
}
