const chatForm = document.getElementById("chatForm");
const chatInput = document.getElementById("chatInput");
const chatWindow = document.getElementById("chatWindow");

// CSRF 가져오기
function getCsrfToken() {
    const metaToken = document.querySelector('meta[name="_csrf"]');
    const metaHeader = document.querySelector('meta[name="_csrf_header"]');
    return {
        csrfToken: metaToken ? metaToken.content : null,
        csrfHeader: metaHeader ? metaHeader.content : null
    };
}

// fetch wrapper
async function api(path, opts = {}) {
    const { csrfToken, csrfHeader } = getCsrfToken();
    const headers = { "Content-Type": "application/json", ...(csrfToken && csrfHeader ? { [csrfHeader]: csrfToken } : {}) };
    const res = await fetch(path, { headers, credentials: "include", ...opts });
    if (!res.ok) throw new Error(`${path} ${res.status}`);
    return res.json();
}

// 메시지 추가
function addMessage(text, sender = "ai") {
    const div = document.createElement("div");
    div.className = sender === "ai"
        ? "p-2 bg-indigo-50 rounded-lg text-slate-800"
        : "p-2 bg-slate-100 rounded-lg text-slate-800 text-right";
    div.textContent = text;
    chatWindow.appendChild(div);
    chatWindow.scrollTop = chatWindow.scrollHeight;
}

// 질문 전송
async function sendQuestion(question) {
    try {
        addMessage(question, "user");
        chatInput.value = "";
        const data = await api("/chat/api", { method: "POST", body: JSON.stringify({ question }) });

        // 답변 표시
        if (data.answer) addMessage(data.answer, "ai");

        // redirect 버튼 생성
        if (data.redirect) {
            const btnContainer = document.createElement("div");
            btnContainer.className = "mt-1 flex gap-2";

            const yesBtn = document.createElement("button");
            yesBtn.textContent = "네";
            yesBtn.className = "p-1 bg-blue-500 text-white rounded";
            yesBtn.onclick = () => window.location.href = data.redirect;

            const noBtn = document.createElement("button");
            noBtn.textContent = "아니요";
            noBtn.className = "p-1 bg-gray-300 text-black rounded";
            noBtn.onclick = () => {
                addMessage("이동이 취소되었습니다.", "ai");
                btnContainer.remove();
            };

            btnContainer.appendChild(yesBtn);
            btnContainer.appendChild(noBtn);
            chatWindow.appendChild(btnContainer);
            chatWindow.scrollTop = chatWindow.scrollHeight;
        }

    } catch (err) {
        addMessage("❌ AI 호출 중 오류 발생", "ai");
        console.error(err);
    }
}

chatForm.addEventListener("submit", e => {
    e.preventDefault();
    const question = chatInput.value.trim();
    if (question) sendQuestion(question);
});

window.addEventListener("DOMContentLoaded", () => console.log("✅ Chat JS 초기화 완료"));
