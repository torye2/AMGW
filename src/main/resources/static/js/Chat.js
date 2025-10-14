function getCsrfToken() {
    const metaToken = document.querySelector('meta[name="_csrf"]');
    const metaHeader = document.querySelector('meta[name="_csrf_header"]');
    return {
        csrfToken: metaToken ? metaToken.content : null,
        csrfHeader: metaHeader ? metaHeader.content : null
    };
}

async function api(path, opts = {}) {
    const { csrfToken, csrfHeader } = getCsrfToken();
    const headers = {
        "Content-Type": "application/json",
        ...(csrfToken && csrfHeader ? { [csrfHeader]: csrfToken } : {})
    };

    const res = await fetch(path, {
        headers,
        credentials: "include",
        ...opts
    });

    if (!res.ok) throw new Error(`${path} ${res.status}`);
    return res.json();
}

const chatForm = document.getElementById("chatForm");
const chatInput = document.getElementById("chatInput");
const chatWindow = document.getElementById("chatWindow");

function addMessage(text, sender="ai") {
    const div = document.createElement("div");
    div.className = sender === "ai"
        ? "p-2 bg-indigo-50 rounded-lg text-slate-800"
        : "p-2 bg-slate-100 rounded-lg text-slate-800 text-right";
    div.textContent = text;
    chatWindow.appendChild(div);
    chatWindow.scrollTop = chatWindow.scrollHeight;
}

async function sendQuestion(question) {
    try {
        const data = await api("/chat/api", {
            method: "POST",
            body: JSON.stringify({ question })
        });

        if (data.redirect) {
            addMessage(`📂 페이지로 이동합니다: ${data.redirect}`, "ai");
            setTimeout(() => { window.location.href = data.redirect; }, 700);
            return;
        }

        if (data.answer) {
            addMessage(data.answer, "ai");
        } else {
            addMessage("⚠️ AI 응답을 해석할 수 없습니다.", "ai");
        }

    } catch (err) {
        addMessage("❌ AI 호출 중 오류 발생", "ai");
        console.error(err);
    }
}

chatForm.addEventListener("submit", e => {
    e.preventDefault();
    const question = chatInput.value.trim();
    if (!question) return;

    addMessage(question, "user");
    chatInput.value = "";
    sendQuestion(question);
});

window.addEventListener("DOMContentLoaded", () => {
    console.log("✅ Chat JS 초기화 완료");
});
