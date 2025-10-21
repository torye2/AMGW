// /js/notifications.js
(function () {
  // ===== CSRF 유틸 =====
  function getMetaCsrf() {
    const token = document.querySelector('meta[name="_csrf"]')?.content;
    const header = document.querySelector('meta[name="_csrf_header"]')?.content;
    return token && header ? { header, token } : null;
  }
  function getCookieXsrf(name = 'XSRF-TOKEN') {
    const m = document.cookie.match(new RegExp('(?:^|; )' + name + '=([^;]*)'));
    return m ? decodeURIComponent(m[1]) : null;
  }
  async function apiFetch(path, opts = {}) {
    const headers = { 'Content-Type': 'application/json', ...(opts.headers || {}) };

    // meta 우선 (Spring Security의 CsrfTokenRepository 기반)
    const meta = getMetaCsrf();
    if (meta) headers[meta.header] = meta.token;
    else {
      // CookieCsrfTokenRepository.withHttpOnlyFalse() 대비
      const xsrf = getCookieXsrf();
      if (xsrf) headers['X-XSRF-TOKEN'] = xsrf;
    }

    const res = await fetch(path, { credentials: 'include', ...opts, headers });
    if (!res.ok) {
      const txt = await res.text().catch(() => '');
      console.warn('[notif] fetch fail', path, res.status, txt);
      throw new Error(path + ' ' + res.status);
    }
    const ct = res.headers.get('content-type') || '';
    return ct.includes('application/json') ? res.json() : null;
  }

  // ===== DOM 준비 =====
  document.addEventListener('DOMContentLoaded', () => {
    const btn = document.getElementById('btnNotif');
    const badge = document.getElementById('notifBadge');
    if (!btn || !badge) return;

    // 드롭다운 컨테이너 생성(없으면)
    let dropdown = document.getElementById('notifDropdown');
    if (!dropdown) {
      dropdown = document.createElement('div');
      dropdown.id = 'notifDropdown';
      dropdown.className = 'absolute right-0 mt-2 w-80 bg-white border border-slate-200 rounded-xl shadow-lg hidden z-50';
      // 버튼의 부모(relative) 기준으로 닻 내릴 수 있게 wrapper에 relative 없으면 추가
      const wrap = btn.parentElement;
      if (wrap && !getComputedStyle(wrap).position.match(/relative|absolute|fixed/)) {
        wrap.style.position = 'relative';
      }
      wrap.appendChild(dropdown);
    }

    // 리스트 렌더
    function renderList(list) {
      dropdown.innerHTML = '';
      if (!list || list.length === 0) {
        dropdown.innerHTML = `
          <div class="p-3 text-sm text-slate-500">새 알림이 없습니다.</div>
        `;
        return;
      }

      const ul = document.createElement('ul');
      ul.className = 'divide-y divide-slate-100 max-h-96 overflow-auto';

      list.forEach((it) => {
        const li = document.createElement('li');
        li.className = 'hover:bg-slate-50';
        li.dataset.id = it.id;
        li.dataset.type = it.type;
        if (it.roomId) li.dataset.roomId = it.roomId;
        if (it.url) li.dataset.url = it.url;

        const title = escapeHtml(it.title || it.summary || '(알림)');
        const sub = escapeHtml(it.sub || '');
        const when = it.createdAt ? new Date(it.createdAt).toLocaleString() : '';

        li.innerHTML = `
          <a href="#" class="block px-3 py-2">
            <div class="text-sm font-medium truncate">${title}</div>
            <div class="text-xs text-slate-500 truncate">${sub}</div>
            <div class="text-[11px] text-slate-400 mt-1">${when}</div>
          </a>
        `;
        ul.appendChild(li);
      });

      dropdown.appendChild(ul);
    }

    // XSS-safe
    function escapeHtml(s) {
      return String(s ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
    }

    // 배지 숫자 갱신
    function setBadge(n) {
      const nn = Math.max(0, Number(n) || 0);
      badge.textContent = String(nn);
      badge.classList.toggle('hidden', nn === 0);
    }

    // 현재 메모리 캐시 (UI 즉시 반영용)
    let cache = [];

    async function fetchUnread() {
      // 서버: /api/notifications/unread → [{id,type,summary,data(createdAt, extra as JSON string),createdAt}, ...]
      const raw = await apiFetch('/api/notifications/unread', { method: 'GET' });
      // 파싱해서 우리가 쓰기 쉬운 모양으로 변환
      cache = (raw || []).map((n) => {
        let extra = {};
        if (n.data) {
          try { extra = JSON.parse(n.data); } catch (_) {}
        }
        const item = {
          id: n.id,
          type: n.type,
          summary: n.summary,
          createdAt: n.createdAt,
          // 기본 텍스트(title/sub/url) 구성
          title: n.summary,
          sub: '',
          url: '#'
        };

        if (n.type === 'chat') {
          const from = extra.senderName || '상대방';
          const content = (extra.content || '').toString();
          item.title = `${from}님에게 채팅알림`;
          item.sub = content;
          item.url = extra.roomId ? `/chat?roomId=${extra.roomId}` : '/chat';
          item.roomId = extra.roomId || null;
        } else if (n.type === 'approval') {
          const from = extra.requesterName || '누군가';
          const title = extra.title ? ` (${extra.title})` : '';
          item.title = `${from}님에게 결재 요청`;
          item.sub = extra.title || '';
          item.url = extra.docId ? `/approvals/${extra.docId}` : '/approvals';
        } else if (n.type === 'vacation') {
          const from = extra.requesterName || '누군가';
          const kind = extra.vacationType ? ` (${extra.vacationType})` : '';
          item.title = `${from}님이 휴가/근태 신청`;
          item.sub = extra.vacationType || '';
          item.url = extra.requestId ? `/attendance/requests/${extra.requestId}` : '/attendance.html#request';
        }
        return item;
      });

      setBadge(cache.length);
      renderList(cache);
    }

    // 최초 로드
    fetchUnread().catch(() => {});

    // 버튼 토글
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      dropdown.classList.toggle('hidden');
    });

    // 바깥 클릭 닫기
    document.addEventListener('click', (e) => {
      if (!dropdown.classList.contains('hidden')) {
        if (!dropdown.contains(e.target) && !btn.contains(e.target)) {
          dropdown.classList.add('hidden');
        }
      }
    });

    // 항목 클릭 → 서버 읽음 처리(단건/채팅방 묶음) → UI에서 제거 → 이동
    dropdown.addEventListener('click', async (e) => {
      const a = e.target.closest('a');
      if (!a) return;
      e.preventDefault();

      const li = e.target.closest('li');
      if (!li) return;

      const id = li.dataset.id;
      const type = li.dataset.type;
      const roomId = li.dataset.roomId;
      const url = li.dataset.url || '#';

      // UI 선반영(낙관적 업데이트)
      removeFromCacheAndDom(id, roomId, type);

      try {
        if (type === 'chat' && roomId) {
          await apiFetch(`/api/notifications/read-by-room?roomId=${encodeURIComponent(roomId)}`, {
            method: 'POST'
          });
        } else {
          await apiFetch(`/api/notifications/read-one?id=${encodeURIComponent(id)}`, {
            method: 'POST'
          });
        }
      } catch (err) {
        console.warn('[notif] mark-read failed; restoring...', err);
        // 실패 시 목록을 전체 재조회하여 복구
        await fetchUnread().catch(() => {});
        return;
      }

      // 이동
      window.location.href = url;
    });

    function removeFromCacheAndDom(id, roomId, type) {
      if (type === 'chat' && roomId) {
        // 같은 roomId 전부 제거
        cache = cache.filter((x) => String(x.roomId || '') !== String(roomId));
        [...dropdown.querySelectorAll(`li[data-room-id="${CSS.escape(String(roomId))}"]`)].forEach((el) => el.remove());
      } else {
        cache = cache.filter((x) => String(x.id) !== String(id));
        dropdown.querySelector(`li[data-id="${CSS.escape(String(id))}"]`)?.remove();
      }
      setBadge(cache.length);
      if (cache.length === 0) renderList(cache);
    }

    // ===== WebSocket 구독(실시간 수신) =====
    if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {
      console.error('[notif] SockJS/Stomp not loaded');
      return;
    }
    const socket = new SockJS('/ws');
    const stomp = Stomp.over(socket);
    stomp.debug = null;

    stomp.connect({}, () => {
      stomp.subscribe('/user/queue/notifications', (msg) => {
        // 서버 payload 예: { id, type, summary, ...extraData }
        let d = {};
        try { d = JSON.parse(msg.body || '{}'); } catch (_) {}

        // 토스트 메시지
        const text = buildToastText(d);
        showToast(text);

        // 리스트/배지 실시간 반영
        const added = convertPushToItem(d);
        cache.unshift(added);
        setBadge(cache.length);
        // 맨 위에 추가
        prependOneToDom(added);
      });
    });

    function buildToastText(d) {
      const s = (v) => (v == null ? '' : String(v));
      if (s(d.summary)) return d.summary;

      switch (d.type) {
        case 'chat': {
          const from = s(d.senderName) || '상대방';
          const content = s(d.content).trim();
          return content ? `${from}님에게 채팅알림: ${content}` : `${from}님이 메시지를 보냈습니다.`;
        }
        case 'approval': {
          const from = s(d.requesterName) || '누군가';
          const title = s(d.title);
          return `${from}님에게 결재 요청이 왔습니다${title ? ` (${title})` : ''}`;
        }
        case 'vacation': {
          const from = s(d.requesterName) || '누군가';
          const kind = s(d.vacationType);
          return `${from}님이 휴가/근태를 신청했습니다${kind ? ` (${kind})` : ''}`;
        }
        default:
          return '새 알림이 도착했습니다.';
      }
    }

    function convertPushToItem(d) {
      const s = (v) => (v == null ? '' : String(v));
      const item = {
        id: d.id,
        type: d.type,
        createdAt: new Date().toISOString(),
        title: d.summary || '(알림)',
        sub: '',
        url: '#'
      };

      if (d.type === 'chat') {
        const from = s(d.senderName) || '상대방';
        item.title = `${from}님에게 채팅알림`;
        item.sub = s(d.content);
        item.url = d.roomId ? `/chat?roomId=${d.roomId}` : '/chat';
        item.roomId = d.roomId || null;
      } else if (d.type === 'approval') {
        const from = s(d.requesterName) || '누군가';
        const title = s(d.title);
        item.title = `${from}님에게 결재 요청`;
        item.sub = title;
        item.url = d.docId ? `/approvals/${d.docId}` : '/approvals';
      } else if (d.type === 'vacation') {
        const from = s(d.requesterName) || '누군가';
        const kind = s(d.vacationType);
        item.title = `${from}님이 휴가/근태 신청`;
        item.sub = kind;
        item.url = d.requestId ? `/attendance/requests/${d.requestId}` : '/attendance.html#request';
      }
      return item;
    }

    function prependOneToDom(it) {
      // 비어있었다면 전체 다시 렌더
      if (!dropdown.querySelector('ul')) {
        renderList([it, ...cache.filter((x) => x.id !== it.id)]);
        return;
      }
      const ul = dropdown.querySelector('ul');
      const li = document.createElement('li');
      li.className = 'hover:bg-slate-50';
      li.dataset.id = it.id;
      li.dataset.type = it.type;
      if (it.roomId) li.dataset.roomId = it.roomId;
      if (it.url) li.dataset.url = it.url;

      const title = escapeHtml(it.title || it.summary || '(알림)');
      const sub = escapeHtml(it.sub || '');
      const when = it.createdAt ? new Date(it.createdAt).toLocaleString() : '';
      li.innerHTML = `
        <a href="#" class="block px-3 py-2">
          <div class="text-sm font-medium truncate">${title}</div>
          <div class="text-xs text-slate-500 truncate">${sub}</div>
          <div class="text-[11px] text-slate-400 mt-1">${when}</div>
        </a>
      `;
      ul.prepend(li);
    }

    // 간단 토스트
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
      toast.textContent = (message && String(message).trim()) ? message : '(알림)';

      const existing = document.querySelectorAll('.__toast_item__').length;
      toast.style.transform = `translateY(-${existing * 8}px)`;

      document.body.appendChild(toast);
      requestAnimationFrame(() => { toast.style.opacity = '1'; });
      setTimeout(() => { toast.style.opacity = '0'; }, 3000);
      setTimeout(() => { toast.remove(); }, 3600);
    }
  });
})();
