document.addEventListener('DOMContentLoaded', () => {
  const notifBadge = document.getElementById('notifBadge');
  if (!notifBadge) return;

  // WebSocket 연결
  const socket = new SockJS('/ws');
  const stompClient = Stomp.over(socket);
  stompClient.debug = null;

  stompClient.connect({}, () => {
    stompClient.subscribe('/user/queue/notifications', (msg) => {
      const data = JSON.parse(msg.body);
      showToast(data.summary || '새 알림이 도착했습니다.');
      activateBadge();
    });
  });

  function activateBadge() {
    notifBadge.classList.remove('hidden');
  }

  function showToast(message) {
    const toast = document.createElement('div');
    toast.className =
      'fixed bottom-6 left-6 bg-slate-800 text-white px-4 py-3 rounded-xl shadow-lg opacity-0 transition-opacity duration-500';
    toast.innerHTML = `<span>${message}</span>`;
    document.body.appendChild(toast);

    setTimeout(() => (toast.style.opacity = '1'), 50);
    setTimeout(() => (toast.style.opacity = '0'), 2800);
    setTimeout(() => toast.remove(), 3300);
  }

  // 전체 읽음 처리
  document.getElementById('btnNotif')?.addEventListener('click', async () => {
    try {
      await fetch('/api/notifications/read-all', { method: 'POST', credentials: 'same-origin' });
      notifBadge.classList.add('hidden');
    } catch (e) {
      console.error('알림 읽음 처리 실패:', e);
    }
  });
});
