document.addEventListener("DOMContentLoaded", async () => {
  // 1️⃣ Chat Widget HTML 불러오기
  try {
    const res = await fetch("/html/chatWidget.html");
    if (!res.ok) throw new Error(`Chat widget fetch 실패: ${res.status}`);
    const html = await res.text();
    const container = document.createElement("div");
    container.innerHTML = html;
    document.body.appendChild(container);
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
  const todayCheckIn = document.getElementById("todayCheckIn");
  const todayCheckOut = document.getElementById("todayCheckOut");

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
  function addMessage(text, type = "ai") {
    const div = document.createElement("div");

    switch(type) {
      case "ai": // 일반 AI
        div.className = "p-2 bg-indigo-50 rounded-lg text-slate-800";
        break;
      case "user":
        div.className = "p-2 bg-slate-100 rounded-lg text-slate-800 text-right";
        break;
      case "checkIn": // 출근
        div.className = "p-2 bg-green-100 rounded-lg text-green-800 font-bold";
        break;
      case "checkOut": // 퇴근
        div.className = "p-2 bg-yellow-100 rounded-lg text-yellow-800 font-bold";
        break;
      case "warning":
        div.className = "p-2 bg-red-100 rounded-lg text-red-800 font-bold";
        break;
    }

    div.textContent = text;
    chatWindow.appendChild(div);
    chatWindow.scrollTop = chatWindow.scrollHeight;
  }

  // 6️⃣ 오늘 출퇴근 조회
  async function loadTodayAttendance() {
    try {
      const data = await api("/api/attendance/today");
      todayCheckIn.textContent = data?.checkInAt ? new Date(data.checkInAt).toLocaleTimeString() : "-";
      todayCheckOut.textContent = data?.checkOutAt ? new Date(data.checkOutAt).toLocaleTimeString() : "-";
    } catch (err) {
      console.error("오늘 출퇴근 조회 실패", err);
    }
  }

  await loadTodayAttendance();

  // 7️⃣ 질문 전송
  async function sendQuestion(question) {
    try {
      // 사용자 메시지
      addMessage(question, "user");
      chatInput.value = "";

      const data = await api("/aiChat/aiApi", {
        method: "POST",
        body: JSON.stringify({ question })
      });

      // 출퇴근 처리
      if (data.action === "checkIn" || data.action === "checkOut") {
        const type = data.action === "checkIn" ? "checkIn" : "checkOut";
        if(data.status === "ok") {
          addMessage(`✅ ${data.action === "checkIn" ? "출근" : "퇴근"} 시간이 기록되었습니다.`, type);
        } else if(data.status === "already") {
          addMessage(`⚠️ 이미 오늘 ${data.action === "checkIn" ? "출근" : "퇴근"} 기록이 있습니다.`, "warning");
        }
        await loadTodayAttendance();
      } else {
        // 일반 AI 답변
        if(data.answer) addMessage(data.answer, "ai");
      }

      // 페이지 이동 처리
      if (data.redirect) {
        const btnContainer = document.createElement("div");
        btnContainer.className = "mt-1 flex gap-2";

        const yesBtn = document.createElement("button");
        yesBtn.textContent = "네";
        yesBtn.className = "p-1 bg-blue-500 text-white rounded";
        yesBtn.onclick = () => {
          sessionStorage.setItem("autoFillData", JSON.stringify(data));
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

  // 8️⃣ 폼 이벤트
  chatForm.addEventListener("submit", (e) => {
    e.preventDefault();
    const q = chatInput.value.trim();
    if (q) sendQuestion(q);
  });
});
