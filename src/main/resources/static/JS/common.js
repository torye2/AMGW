(function () {
    document.addEventListener('click', (e) => {
        const btn = e.target.closest('#btnProfile');
        const menu = document.getElementById('menuProfile');

        // 프로필 버튼 클릭 → 토글
        if (btn && menu) {
            e.preventDefault();
            menu.classList.toggle('show');
            return;
        }

        // 바깥 클릭 → 닫기
        if (menu && !e.target.closest('#menuProfile')) {
            menu.classList.remove('show');
        }
    });

    // ESC로 닫기
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            document.getElementById('menuProfile')?.classList.remove('show');
        }
    });
})();
