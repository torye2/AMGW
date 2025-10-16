document.addEventListener("DOMContentLoaded", async () => {
  // 1️⃣ Chat Widget HTML 불러오기
  try {
    const res = await fetch("/html/chatWidget.html");
    if (!res.ok) throw new Error(`Chat widget fetch 실패: ${res.status}`);
    const html = await res.text();
    const container = document.createElement("div");
    container.innerHTML = html;
    document.body.appendChild(container);
    console.log("✅ Chat Widget HTML 로드 완료");
  } catch (err) {
    console.error("❌ Chat Widget HTML 로드 실패", err);
    return;
  }

  // 2️⃣ DOM 선택
  const chatLayer = document.getElementById("chatLayer");
  const chatToggleBtn = document.getElementById("chatToggleBtn");
  const chatCloseBtn = document.getElementById("chatCloseBtn");
  const chatForm = document.getElementById("chatForm");
  const chatInput = document.getElementById("chatInput");
  const chatWindow = document.getElementById("chatWindow");

  // 3️⃣ 토글
  chatToggleBtn?.addEventListener("click", () => chatLayer.classList.toggle("hidden"));
  chatCloseBtn?.addEventListener("click", () => chatLayer.classList.add("hidden"));

  // 4️⃣ fetch wrapper
  function getCsrf() {
    const t = document.querySelector('meta[name="_csrf"]')?.content;
    const h = document.querySelector('meta[name="_csrf_header"]')?.content;
    return { t, h };
  }
  async function api(path, opts = {}) {
    const { t, h } = getCsrf();
    const headers = { "Content-Type": "application/json" };
    if (t && h) headers[h] = t;
    const res = await fetch(path, { headers, credentials: "include", ...opts });
    if (!res.ok) throw new Error(`${path} ${res.status}`);
    return res.json();
  }

  // 5️⃣ 메시지 추가
  function addMessage(text, sender = "ai") {
    const div = document.createElement("div");
    div.className =
      sender === "ai"
        ? "p-2 bg-indigo-50 rounded-lg text-slate-800"
        : "p-2 bg-slate-100 rounded-lg text-slate-800 text-right";
    div.textContent = text;
    chatWindow.appendChild(div);
    chatWindow.scrollTop = chatWindow.scrollHeight;
  }

  // 6️⃣ 질문 전송
  async function sendQuestion(question) {
    try {
      addMessage(question, "user");
      chatInput.value = "";

      const data = await api("/aiChat/aiApi", {
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
        yesBtn.onclick = () => {
            // data.autoFill 대신 data 전체를 저장
            sessionStorage.setItem("autoFillData", JSON.stringify(data));
            console.log("✅ autoFill 저장됨:", data);

            setTimeout(() => (window.location.href = data.redirect), 200);
        };

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
      console.error(err);
      addMessage("❌ AI 호출 중 오류 발생", "ai");
    }
  }

  // 7️⃣ 폼 이벤트
  chatForm.addEventListener("submit", (e) => {
    e.preventDefault();
    const q = chatInput.value.trim();
    if (q) sendQuestion(q);
  });

  console.log("✅ Chat Widget JS 초기화 완료");
});
