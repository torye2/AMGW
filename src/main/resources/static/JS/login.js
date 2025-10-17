document.addEventListener("DOMContentLoaded", () => {
    const togglePwd = document.querySelector("#togglePwd");
    const pwd = document.querySelector("#password");

    if (togglePwd && pwd) {
        togglePwd.addEventListener("click", () => {
            pwd.type = pwd.type === "password" ? "text" : "password";
            togglePwd.textContent = pwd.type === "password" ? "👁️" : "🙈";
        });
    }
});
