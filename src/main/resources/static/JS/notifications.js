document.addEventListener('DOMContentLoaded', () => {
  const notifBadge = document.getElementById('notifBadge');
  if (!notifBadge) return;

  if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {
    console.error('[notif] SockJS/Stomp 미로딩');
    return;
  }

  const socket = new SockJS('/ws');
  const stompClient = Stomp.over(socket);
  stompClient.debug = null;

  // 최초 배지 카운트
  updateBadge();

  stompClient.connect({}, () => {
    stompClient.subscribe('/user/queue/notifications', (msg) => {
      console.log('[notif] raw:', msg.body);
      let d = {};
      try { d = JSON.parse(msg.body || '{}'); } catch(e) {}

      const s = (v) => (v == null ? '' : String(v));
      let text = s(d.summary);

      if (!text) {
        switch (d.type) {
          case 'chat': {
            const from = s(d.senderName) || '상대방';
            const content = s(d.content).trim();
            text = content ? `${from}님에게 채팅알림: ${content}` : `${from}님이 메시지를 보냈습니다.`;
            break;
          }
          case 'approval': {
            const from = s(d.requesterName) || '누군가';
            const title = s(d.title);
            text = `${from}님에게 결재 요청이 왔습니다${title ? ` (${title})` : ''}`;
            break;
          }
          case 'vacation': {
            const from = s(d.requesterName) || '누군가';
            const kind = s(d.vacationType);
            text = `${from}님이 휴가/근태를 신청했습니다${kind ? ` (${kind})` : ''}`;
            break;
          }
          default:
            text = '새 알림이 도착했습니다.';
        }
      }

      showToast(text);
      notifBadge.classList.remove('hidden');
      updateBadge(); // ✅ 수신 후 배지 카운트 갱신
    });
  });

  async function updateBadge() {
    try {
      // 컨트롤러가 "세션에서 현재 사용자"를 읽는 버전으로 구현되어 있어야 합니다.
      const res = await fetch('/api/notifications/unread', { credentials: 'same-origin' });
      if (!res.ok) return;
      const list = await res.json();
      const n = Array.isArray(list) ? list.length : 0;
      notifBadge.textContent = String(n);
      notifBadge.classList.toggle('hidden', n === 0);
    } catch {}
  }

  function showToast(message) {
    const toast = document.createElement('div');
    toast.style.position = 'fixed';
    toast.style.left = '24px';
    toast.style.bottom = '24px';
    toast.style.maxWidth = '360px';
    toast.style.padding = '12px 16px';
    toast.style.borderRadius = '12px';
    toast.style.background = '#0f172a';
    toast.style.color = '#fff';
    toast.style.boxShadow = '0 10px 20px rgba(0,0,0,.25)';
    toast.style.whiteSpace = 'pre-wrap';
    toast.style.lineHeight = '1.35';
    toast.style.fontSize = '14px';
    toast.style.opacity = '0';
    toast.style.transition = 'opacity .4s ease';
    toast.style.zIndex = '99999';
    toast.className = '__toast_item__';

    const existing = document.querySelectorAll('.__toast_item__').length;
    toast.style.transform = `translateY(-${existing * 8}px)`;
    toast.textContent = (message && message.trim()) ? message : '(알림)';
    document.body.appendChild(toast);
    requestAnimationFrame(() => { toast.style.opacity = '1'; });
    setTimeout(() => { toast.style.opacity = '0'; }, 3000);
    setTimeout(() => { toast.remove(); }, 3600);
  }

  document.getElementById('btnNotif')?.addEventListener('click', async () => {
    try {
      await fetch('/api/notifications/read-all', { method: 'POST', credentials: 'same-origin' });
      notifBadge.classList.add('hidden');
      notifBadge.textContent = '0';
    } catch (e) {
      console.error('알림 읽음 처리 실패:', e);
    }
  });
});
