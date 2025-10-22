// 문서함: 목록/업로드/이름변경
(() => {
    const elTitle = document.querySelector('#title');
    const elFile = document.querySelector('#file');
    const elBtnUpload = document.querySelector('#btnUpload');
    const elDrop = document.querySelector('#dropzone');
    const elTbody = document.querySelector('#docTbody');
    const elPageInfo = document.querySelector('#pageInfo');
    const elTotalInfo = document.querySelector('#totalInfo');
    const elPrev = document.querySelector('#prevPage');
    const elNext = document.querySelector('#nextPage');
    const elRefresh = document.querySelector('#btnRefresh');

    // 페이지네이션 상태
    let page = 0;        // 0-base
    let size = 10;       // 페이지 크기
    let lastPage = false;
    let total = 0;

    function getCookie(name) {
        const m = document.cookie.match('(^|;)\\s*' + name + '\\s*=\\s*([^;]+)');
        return m ? decodeURIComponent(m.pop()) : '';
    }
    function xsrf() {
        // CookieCsrfTokenRepository 기본명: XSRF-TOKEN
        return getCookie('XSRF-TOKEN') || getCookie('XSRF') || '';
    }

    function humanSize(bytes) {
        if (bytes == null) return '-';
        const units = ['B','KB','MB','GB','TB'];
        let i = 0, n = Number(bytes);
        while (n >= 1024 && i < units.length-1) { n /= 1024; i++; }
        return `${n.toFixed(n < 10 && i > 0 ? 1 : 0)} ${units[i]}`;
    }
    function fmt(dtStr) {
        // 서버가 ISO 로 내려준다고 가정
        if (!dtStr) return '-';
        const d = new Date(dtStr);
        if (Number.isNaN(d.getTime())) return dtStr;
        const p2 = (v)=> String(v).padStart(2,'0');
        return `${d.getFullYear()}-${p2(d.getMonth()+1)}-${p2(d.getDate())} ${p2(d.getHours())}:${p2(d.getMinutes())}`;
    }

    async function listDocs() {
        elTbody.innerHTML = `<tr><td colspan="7" class="text-center py-4 text-muted">불러오는 중…</td></tr>`;
        try {
            const res = await fetch(`/api/docs?page=${page}&size=${size}`, {
                credentials: 'include',
                headers: { 'Accept': 'application/json' }
            });
            if (!res.ok) throw new Error('목록 조회 실패');
            const data = await res.json();

            // Spring Page 기본 형태 방어적으로 처리
            const content = Array.isArray(data.content) ? data.content : (Array.isArray(data) ? data : []);
            total = data.totalElements ?? content.length;
            lastPage = data.last ?? (page * size + content.length >= total);

            renderTable(content);
            elPageInfo.textContent = `Page ${page+1}`;
            elTotalInfo.textContent = `총 ${total.toLocaleString()}건`;
            elPrev.parentElement.classList.toggle('disabled', page <= 0);
            elNext.parentElement.classList.toggle('disabled', !!lastPage);
        } catch (e) {
            console.error(e);
            elTbody.innerHTML = `<tr><td colspan="7" class="text-center py-4 text-danger">목록을 불러오지 못했습니다.</td></tr>`;
        }
    }

    function renderTable(list) {
        if (!list || list.length === 0) {
            elTbody.innerHTML = `<tr><td colspan="7" class="text-center py-4 text-muted">문서가 없습니다. 상단에서 업로드하세요.</td></tr>`;
            return;
        }

        elTbody.innerHTML = list.map(row => {
            const id = row.docId ?? row.id ?? 0;
            const title = row.title ?? '(제목 없음)';
            const mime = row.mimeType ?? '-';
            const size = humanSize(row.sizeBytes);
            const ver = row.version ?? 1;
            const updatedAt = fmt(row.updatedAt);

            return `
        <tr data-id="${id}">
          <td class="text-muted">${id}</td>
          <td>
            <div class="d-flex align-items-center gap-2">
              <span class="truncate" title="${title}">${title}</span>
              <button class="btn btn-sm btn-outline-secondary btn-rename">이름변경</button>
            </div>
          </td>
          <td><span class="badge text-bg-light">${mime}</span></td>
          <td>${size}</td>
          <td><span class="badge text-bg-primary">v${ver}</span></td>
          <td class="text-muted">${updatedAt}</td>
          <td>
            <div class="d-flex gap-2">
              <a class="btn btn-sm btn-primary" href="/documents/edit.html?id=${id}">편집</a>
              <button class="btn btn-sm btn-outline-dark btn-download" disabled title="추후 제공">다운로드</button>
            </div>
          </td>
        </tr>
      `;
        }).join('');
    }

    async function upload(metaTitle, file) {
        if (!metaTitle || !file) {
            alert('제목과 파일을 선택하세요.');
            return;
        }
        const fd = new FormData();
        fd.append('meta', new Blob([JSON.stringify({ title: metaTitle })], { type: 'application/json' }));
        fd.append('file', file);

        elBtnUpload.disabled = true;
        elBtnUpload.textContent = '업로드 중…';
        try {
            const res = await fetch('/api/docs', {
                method: 'POST',
                body: fd,
                credentials: 'include',
                headers: { 'X-XSRF-TOKEN': xsrf() }
            });
            if (!res.ok) throw new Error('업로드 실패');
            elTitle.value = '';
            elFile.value = '';
            await listDocs();
        } catch (e) {
            console.error(e);
            alert('업로드에 실패했습니다.');
        } finally {
            elBtnUpload.disabled = false;
            elBtnUpload.textContent = '업로드';
        }
    }

    async function rename(docId, newTitle) {
        try {
            const res = await fetch(`/api/docs/${docId}`, {
                method: 'PATCH',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'X-XSRF-TOKEN': xsrf()
                },
                body: JSON.stringify({ title: newTitle })
            });
            if (!res.ok) throw new Error('이름 변경 실패');
            await listDocs();
        } catch (e) {
            console.error(e);
            alert('이름 변경에 실패했습니다.');
        }
    }

    // 이벤트 바인딩
    elBtnUpload?.addEventListener('click', () => upload(elTitle.value.trim(), elFile.files[0]));
    elRefresh?.addEventListener('click', () => listDocs());

    // 드래그 앤 드롭
    if (elDrop) {
        ['dragenter','dragover'].forEach(ev =>
            elDrop.addEventListener(ev, e => { e.preventDefault(); e.stopPropagation(); elDrop.classList.add('dragover'); }));
        ['dragleave','drop'].forEach(ev =>
            elDrop.addEventListener(ev, e => { e.preventDefault(); e.stopPropagation(); elDrop.classList.remove('dragover'); }));

        elDrop.addEventListener('drop', (e) => {
            const files = e.dataTransfer.files;
            if (files && files.length > 0) {
                if (!elTitle.value.trim()) elTitle.value = files[0].name.replace(/\.[^.]+$/, '');
                upload(elTitle.value.trim(), files[0]);
            }
        });
    }

    // 테이블 내부 위임(이름변경)
    elTbody.addEventListener('click', (e) => {
        const btn = e.target.closest('.btn-rename');
        if (btn) {
            const tr = btn.closest('tr');
            const id = tr?.dataset?.id;
            const current = tr?.querySelector('.truncate')?.textContent?.trim() || '';
            const next = prompt('새 제목을 입력하세요', current);
            if (next && next.trim() && next !== current) rename(id, next.trim());
        }
    });

    // 페이지 이동
    elPrev.addEventListener('click', (e) => {
        e.preventDefault();
        if (page > 0) { page -= 1; listDocs(); }
    });
    elNext.addEventListener('click', (e) => {
        e.preventDefault();
        if (!lastPage) { page += 1; listDocs(); }
    });

    // 초기 로드
    listDocs();
})();
