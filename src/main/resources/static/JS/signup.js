document.addEventListener("DOMContentLoaded", () => {
    const pw1 = document.querySelector("#pw1");
    const pw2 = document.querySelector("#pw2");
    const msg = document.querySelector("#pwMatchMsg");

    function checkPw() {
        if (!pw1.value || !pw2.value) {
            msg.textContent = "";
            return;
        }

        if (pw1.value === pw2.value) {
            msg.textContent = "비밀번호가 일치합니다 ✅";
            msg.style.color = "green";
        } else {
            msg.textContent = "비밀번호가 일치하지 않습니다 ❌";
            msg.style.color = "red";
        }
    }

    pw1.addEventListener("input", checkPw);
    pw2.addEventListener("input", checkPw);
});
