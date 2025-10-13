// === CSRF 토큰 가져오기 ===
function getCsrfToken() {
    const metaToken = document.querySelector('meta[name="_csrf"]');
    const metaHeader = document.querySelector('meta[name="_csrf_header"]');
    const csrfToken = metaToken ? metaToken.content : null;
    const csrfHeader = metaHeader ? metaHeader.content : null;

    if (!csrfToken || !csrfHeader) {
        console.warn("CSRF 토큰 또는 헤더가 없습니다. fetch 호출 시 403 발생 가능");
    }
    return { csrfToken, csrfHeader };
}

// === 공통 AJAX 호출 ===
async function api(path, opts = {}) {
    const { csrfToken, csrfHeader } = getCsrfToken();
    const headers = {
        "Content-Type": "application/json",
        ...(csrfToken && csrfHeader ? { [csrfHeader]: csrfToken } : {})
    };

    const res = await fetch(path, {
        headers,
        credentials: 'include',
        ...opts
    });

    if (!res.ok) throw new Error(`${path} ${res.status}`);
    return res.json();
}

// === DOM 요소 ===
const chatForm = document.getElementById("chatForm");
const chatInput = document.getElementById("chatInput");
const chatWindow = document.getElementById("chatWindow");

// === 메시지 추가 ===
function addMessage(text, sender = "ai") {
    const div = document.createElement("div");
    div.className = sender === "ai"
        ? "p-2 bg-indigo-50 rounded-lg text-slate-800"
        : "p-2 bg-slate-100 rounded-lg text-slate-800 text-right";
    div.textContent = text;
    chatWindow.appendChild(div);
    chatWindow.scrollTop = chatWindow.scrollHeight;
}

// === 서버에 질문 전송 ===
async function sendQuestion(question) {
    try {
        const data = await api("/chat/api", {
            method: "POST",
            body: JSON.stringify({ question })
        });

        if (data.redirect) {
            addMessage(`이 페이지로 이동합니다: ${data.redirect}`, "ai");
            setTimeout(() => window.location.href = data.redirect, 500);
            return;
        }

        addMessage(data.answer, "ai");

    } catch (err) {
        addMessage("❌ AI 호출 중 오류 발생", "ai");
        console.error(err);
    }
}

// === 폼 제출 이벤트 ===
chatForm.addEventListener("submit", e => {
    e.preventDefault();
    const question = chatInput.value.trim();
    if (!question) return;

    addMessage(question, "user");
    chatInput.value = "";
    sendQuestion(question);
});

window.addEventListener('DOMContentLoaded', () => {
    console.log("Chat JS 초기화 완료");
});
