(function () {
  function safeSet(id, val) {
    var el = document.getElementById(id);
    if (el) el.textContent = val == null ? "" : String(val);
  }

  async function fetchSummary() {
    try {
      const res = await fetch("/api/attendance/summary", {
        credentials: "include",
        headers: { Accept: "application/json" }
      });

      // 비로그인/리다이렉트/HTML 응답 등은 무시하고 조용히 종료
      if (!res.ok) return;
      const ct = res.headers.get("content-type") || "";
      if (!ct.includes("application/json")) return;

      const data = await res.json();
      safeSet("checkIn", data.todayCheckIn || "--:--");
      safeSet("weeklyHours", data.weeklyHoursText || "0h 0m");
    } catch (_) {
      // 절대 에러 던지지 않음 (다른 스크립트 방해 X)
    }
  }

  // DOM 다 올라온 뒤에만 실행 (다른 버튼 바인딩 끝난 후)
  window.addEventListener("load", fetchSummary);

  // 출퇴근 성공 후 수동 갱신용 훅
  window.refreshAttendanceSummary = fetchSummary;
})();
