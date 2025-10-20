document.addEventListener("DOMContentLoaded", () => {
    const csrf = {
        token: document.querySelector("meta[name='_csrf']").content,
        header: document.querySelector("meta[name='_csrf_header']").content
    };

    document.querySelectorAll(".btn-approve, .btn-reject").forEach((btn) => {
        btn.addEventListener("click", async () => {
            const row = btn.closest("tr");
            const id = row.dataset.id;
            const action = btn.classList.contains("btn-approve") ? "approve" : "reject";

            const res = await fetch(`/admin/users/${id}/${action}`, {
                method: "POST",
                headers: {
                    [csrf.header]: csrf.token
                }
            });

            if (res.ok) {
                row.style.opacity = 0.4;
                alert(res.message + " action: " + action);
                row.querySelectorAll("button").forEach(b => b.disabled = true);
                btn.textContent = action === "approve" ? "승인 완료" : "거절됨";
                btn.style.background = action === "approve" ? "#22c55e" : "#ef4444";
            } else {
                alert("처리 중 오류가 발생했습니다.");
            }
        });
    });
});
