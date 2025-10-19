// verify.js
// - 클릭 중복 방지, 간단한 토스트 알림 등 (필요 시 확장)

(function () {
    function disableWhileSubmitting(form) {
        form.addEventListener('submit', function (e) {
            const btn = form.querySelector('button[type="submit"]');
            if (btn) {
                btn.disabled = true;
                btn.dataset.originalText = btn.textContent;
                btn.textContent = '처리 중...';
                setTimeout(() => { // 안전장치 (실패 시 5초 뒤 복구)
                    btn.disabled = false;
                    btn.textContent = btn.dataset.originalText || '확인';
                }, 5000);
            }
        });
    }

    document.querySelectorAll('form[data-guard="submit"]').forEach(disableWhileSubmitting);

    // 아주 단순한 토스트
    window.toast = function (msg) {
        const el = document.createElement('div');
        el.textContent = msg;
        el.style.position = 'fixed';
        el.style.left = '50%';
        el.style.transform = 'translateX(-50%)';
        el.style.bottom = '24px';
        el.style.background = 'rgba(17,24,39,.95)';
        el.style.color = '#fff';
        el.style.padding = '10px 14px';
        el.style.borderRadius = '10px';
        el.style.zIndex = 9999;
        document.body.appendChild(el);
        setTimeout(() => el.remove(), 2200);
    };
})();
