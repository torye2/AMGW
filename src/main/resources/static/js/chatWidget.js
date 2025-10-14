// chatWidget.js
document.addEventListener("DOMContentLoaded", async () => {
    // 1️⃣ chatWidget.html 불러오기
    try {
        const res = await fetch("/html/chatWidget.html"); // 실제 경로에 맞게 수정 (static/chatWidget.html)
        if (!res.ok) throw new Error(`Chat widget fetch 실패: ${res.status}`);
        const html = await res.text();

        // body 끝에 삽입
        const container = document.createElement("div");
        container.innerHTML = html;
        document.body.appendChild(container);

        console.log("✅ Chat Widget HTML 로드 완료");
    } catch (err) {
        console.error("❌ Chat Widget HTML 로드 실패", err);
        return;
    }

    // 2️⃣ DOM 요소 선택
    const chatLayer = document.getElementById("chatLayer");
    const chatToggleBtn = document.getElementById("chatToggleBtn");
    const chatCloseBtn = document.getElementById("chatCloseBtn");
    const chatForm = document.getElementById("chatForm");
    const chatInput = document.getElementById("chatInput");
    const chatWindow = document.getElementById("chatWindow");

    if (!chatLayer || !chatToggleBtn) {
        console.error("❌ Chat Widget HTML이 로드되지 않았습니다.");
        return;
    }

    // 3️⃣ 토글 버튼
    chatToggleBtn.addEventListener("click", () => chatLayer.classList.toggle("hidden"));
    chatCloseBtn.addEventListener("click", () => chatLayer.classList.add("hidden"));

    // 4️⃣ CSRF 토큰 가져오기
    function getCsrfToken() {
        const metaToken = document.querySelector('meta[name="_csrf"]');
        const metaHeader = document.querySelector('meta[name="_csrf_header"]');
        return {
            csrfToken: metaToken ? metaToken.content : null,
            csrfHeader: metaHeader ? metaHeader.content : null
        };
    }

    // 5️⃣ fetch wrapper (CSRF 자동 포함)
    async function api(path, opts = {}) {
        const { csrfToken, csrfHeader } = getCsrfToken();
        console.log("✅ CSRF TOKEN:", csrfToken, "HEADER:", csrfHeader);
        const headers = {
            "Content-Type": "application/json",
            ...(csrfToken && csrfHeader ? { [csrfHeader]: csrfToken } : {})
        };
        console.log("📡 요청 헤더:", headers); // 추가
        const res = await fetch(path, { headers, credentials: "include", ...opts });
        if (!res.ok) throw new Error(`${path} ${res.status}`);
        return res.json();
    }

    // 6️⃣ 메시지 추가
    function addMessage(text, sender = "ai") {
        const div = document.createElement("div");
        div.className = sender === "ai"
            ? "p-2 bg-indigo-50 rounded-lg text-slate-800"
            : "p-2 bg-slate-100 rounded-lg text-slate-800 text-right";
        div.textContent = text;
        chatWindow.appendChild(div);
        chatWindow.scrollTop = chatWindow.scrollHeight;
    }

    // 7️⃣ 질문 전송
    async function sendQuestion(question) {
        try {
            addMessage(question, "user");
            chatInput.value = "";
            const data = await api("/chat/api", {
                method: "POST",
                body: JSON.stringify({ question })
            });

            if (data.answer) addMessage(data.answer, "ai");

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

                btnContainer.append(yesBtn, noBtn);
                chatWindow.appendChild(btnContainer);
                chatWindow.scrollTop = chatWindow.scrollHeight;
            }
        } catch (err) {
            addMessage("❌ AI 호출 중 오류 발생", "ai");
            console.error(err);
        }
    }

    // 8️⃣ Form submit
    chatForm.addEventListener("submit", e => {
        e.preventDefault();
        const question = chatInput.value.trim();
        if (question) sendQuestion(question);
    });

    // 9️⃣ ✅ 우측 하단 버튼 위치 (기존 left → right)
    const toggleBtn = document.getElementById("chatToggleBtn");
    if (toggleBtn) {
        toggleBtn.classList.remove("left-4");
        toggleBtn.classList.add("right-4");
    }

    console.log("✅ Chat Widget JS 초기화 완료");
});
