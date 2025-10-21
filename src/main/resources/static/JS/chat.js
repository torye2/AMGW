(function () {
  // ========= Utils =========
  function getMeta(name) {
    return document.querySelector(`meta[name="${name}"]`)?.content;
  }

  // 로그인 사용자 id (gw.users.id)
  const rawMe = getMeta('me-id') ?? '';
  const meId = Number(rawMe);
  if (!Number.isFinite(meId)) {
    console.warn('[chat] invalid meId meta:', rawMe);
  }

  // CSRF fetch helper
  async function api(path, opts = {}) {
    const token = getMeta('_csrf');
    const header = getMeta('_csrf_header');
    const init = {
      headers: { 'Content-Type': 'application/json', ...(token && header ? { [header]: token } : {}) },
      credentials: 'include',
      ...opts
    };
    const res = await fetch(path, init);
    const text = await res.text();
    if (!res.ok) {
      console.error('API ERROR', res.status, path, text);
      throw new Error(`${path} ${res.status}`);
    }
    try { return JSON.parse(text); } catch { return text; }
  }

  // ========= DOM refs =========
  const elRooms      = document.getElementById('rooms');
  const elMessages   = document.getElementById('messages');
  const elTitle      = document.getElementById('title');
  const elMsg        = document.getElementById('msg');
  const elSend       = document.getElementById('sendBtn');
  const elDirectForm = document.getElementById('directForm');
  const elGroupForm  = document.getElementById('createGroupForm');

  // ========= WS / STOMP =========
  let stomp = null;
  let currentRoom = null;
  let currentSubId = null;
  let isConnected = false;

  function updateInputState() {
    const disabled = !isConnected || !currentRoom;
    if (elMsg)  elMsg.disabled  = disabled;
    if (elSend) elSend.disabled = disabled;
  }

  function connectWs() {
    const sock = new SockJS('/ws');
    stomp = Stomp.over(sock);
    stomp.connect({}, () => {
      console.log('WS connected');
      isConnected = true;           // 연결 상태 업
      updateInputState();

      // ★ 첫 방 자동 오픈 (있으면)
      const first = document.querySelector('.room');
      if (first && !currentRoom) {
        const rid = first.getAttribute('data-room-id');
        openRoom(rid, first);
      }
    });
  }

  function getSenderId(m){
    if (m == null) return NaN;
    if (m.senderId != null) return Number(m.senderId);
    if (m.sender_id != null) return Number(m.sender_id);
    if (m.sender && m.sender.id != null) return Number(m.sender.id);
    return NaN;
  }

  function appendMsg(m){
    const sid  = getSenderId(m);
    const mine = Number.isFinite(meId) && Number.isFinite(sid) && sid === meId;

    // 행 컨테이너(이름 라벨 + 말풍선)
    const row  = document.createElement('div');
    row.className = 'row ' + (mine ? 'me' : 'other');

    // --- 이름 라벨 ---
    // 내 메시지는 보통 라벨을 생략하고, "상대" 메시지만 표시
    if (!mine) {
      // 백엔드가 내려주는 키: senderName (우선), 없으면 대체 키도 시도
      const name = m.senderName || (m.sender && m.sender.name) || '사용자';
      const label = document.createElement('div');
      label.className = 'name-label';
      label.textContent = name;
      row.appendChild(label);
    }

    // --- 말풍선 ---
    const bubble  = document.createElement('div');
    bubble.className = 'bubble ' + (mine ? 'me' : 'other');
    bubble.textContent = m?.content ?? '';
    row.appendChild(bubble);

    // 렌더
    elMessages.appendChild(row);
    elMessages.scrollTop = elMessages.scrollHeight;
  }

  async function openRoom(roomId, node) {
    currentRoom = roomId;
    document.querySelectorAll('.room').forEach(n => n.classList.remove('active'));
    node?.classList.add('active');
    elTitle.textContent = node?.textContent?.trim() || `Room #${roomId}`;
    elMessages.innerHTML = '';

    // 히스토리 로드
    const page = await api(`/api/chat/rooms/${roomId}/messages?page=0&size=50`);
    // console.debug('[chat] history', page); // 필요시 활성화
    const arr = Array.isArray(page.content) ? page.content.slice().reverse() : [];
    arr.forEach(appendMsg);
    if (arr.length === 0) {
      const tip = document.createElement('div');
      tip.className = 'muted';
      tip.textContent = '첫 메시지를 보내보세요.';
      elMessages.appendChild(tip);
    }

    // 구독 갱신
    if (stomp) {
      if (currentSubId) stomp.unsubscribe(currentSubId);
      const sub = stomp.subscribe(`/topic/rooms/${roomId}`, (frame) => {
        const m = JSON.parse(frame.body);
        // console.debug('[chat] recv', m); // 필요시 활성화
        appendMsg(m);
      });
      currentSubId = sub.id;
    }
    updateInputState();
  }
  // 외부에서 열 수 있게 노출(선택)
  window.openRoom = openRoom;

  // ========= Events =========
  document.addEventListener('DOMContentLoaded', () => {
    connectWs();

    // 방 클릭
    elRooms?.addEventListener('click', (e) => {
      const target = e.target.closest('.room');
      if (!target) return;
      const roomId = target.getAttribute('data-room-id');
      if (roomId) openRoom(roomId, target);
    });

    // 메시지 전송 (낙관적 렌더 제거: 중복 방지)
    elSend?.addEventListener('click', () => {
      const text = elMsg.value.trim();
      if (!text || !currentRoom || !stomp || !isConnected) return;

      stomp.send(`/app/rooms/${currentRoom}/send`, {}, JSON.stringify({
        roomId: currentRoom,
        content: text,
        contentType: 'TEXT'
      }));

      // 서버 브로드캐스트가 오면 appendMsg에서 파란/회색 판별하여 렌더
      elMsg.value = '';
      elMsg.focus();
    });

    elMsg?.addEventListener('keydown', (e) => {
      if (e.isComposing) return;
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        elSend?.click();
      }
    });

    // 1:1 방 생성
    elDirectForm?.addEventListener('submit', async (e) => {
      e.preventDefault();
      const fd = new FormData(elDirectForm);
      const userId = (fd.get('userId') || '').toString().trim();
      if (!userId) return;

      const res = await api(`/api/chat/rooms/direct?userId=${encodeURIComponent(userId)}`, { method: 'POST' });

      // 목록 추가 후 열기
      const div = document.createElement('div');
      div.className = 'room';
      div.setAttribute('data-room-id', res.roomId);
      div.textContent = `DIRECT #${res.roomId}`;
      elRooms?.prepend(div);
      openRoom(res.roomId, div);
      elDirectForm.reset();
    });

    // 그룹 방 생성 (userId들 쉼표 구분)
    elGroupForm?.addEventListener('submit', async (e) => {
      e.preventDefault();
      const fd = new FormData(elGroupForm);
      const name = (fd.get('name') || '').toString().trim();
      const userIds = (fd.get('userIds') || '').toString()
        .split(',').map(s => s.trim()).filter(Boolean).map(Number)
        .filter(n => Number.isFinite(n) && n > 0);

      if (!name) return alert('그룹명을 입력하세요');
      if (userIds.length === 0) return alert('초대할 userId를 입력하세요');

      try {
        const res = await api('/api/chat/rooms/group', {
          method: 'POST',
          body: JSON.stringify({ name, memberIds: userIds })
        });

        const roomId = res.roomId;
        const div = document.createElement('div');
        div.className = 'room';
        div.setAttribute('data-room-id', roomId);
        div.textContent = `${name} · 새 그룹`;
        elRooms?.prepend(div);
        openRoom(roomId, div);
        elGroupForm.reset();
      } catch (err) {
        console.error(err);
        alert('그룹 방 생성 중 오류가 발생했습니다.');
      }
    });
  });
})();
