// /js/searchPage.js
(function(){
  const $ = (s, r=document)=>r.querySelector(s);
  const esc = (s)=>String(s??'').replace(/[&<>"']/g, m=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#039;'}[m]));

  document.addEventListener('DOMContentLoaded', () => {
    const params = new URLSearchParams(location.search);
    const q = (params.get('q') || '').trim();

    // 상단에 검색어 에코
    const qEcho = $('#qEcho');
    if (qEcho) qEcho.textContent = q ? `“${q}”에 대한 결과` : '검색어가 없습니다.';

    // 헤더 검색 input에도 값 반영 (있으면)
    const headerInput = $('#globalSearch');
    if (headerInput && q) headerInput.value = q;

    // 결과 로드
    load(q);
  });

  async function load(q){
    const box = $('#searchResults');
    if (!box) return;

    if (!q){
      box.innerHTML = emptyCard('검색어를 입력해주세요.');
      return;
    }

    box.innerHTML = skeleton();

    try{
      const res = await fetch('/api/search?q=' + encodeURIComponent(q), { credentials: 'same-origin' });
      if (!res.ok) throw new Error('search ' + res.status);
      const data = await res.json() || [];

      if (!data.length){
        box.innerHTML = emptyCard('검색 결과가 없습니다.');
        return;
      }

      // 섹션별 그룹핑(user/approval/chat 등)
      const groups = groupBy(data, x => x.type || 'other');
      const order  = ['approval','chat','user','notice','doc','other'];

      const cards = [];
      for (const key of order){
        if (!groups[key]) continue;
        cards.push(renderCard(key, groups[key]));
      }
      box.innerHTML = cards.join('');

    }catch(err){
      console.error('[searchPage] load failed', err);
      box.innerHTML = emptyCard('오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
    }
  }

  function groupBy(arr, fn){
    return arr.reduce((m, x) => {
      const k = fn(x);
      (m[k] ||= []).push(x);
      return m;
    }, {});
  }

  function titleByType(t){
    switch(t){
      case 'approval': return '결재';
      case 'chat':     return '채팅방';
      case 'user':     return '직원';
      case 'notice':   return '공지';
      case 'doc':      return '문서';
      default:         return '기타';
    }
  }

  function renderCard(type, items){
    return `
      <article class="rounded-xl border border-slate-200 bg-white p-4">
        <div class="flex items-center justify-between mb-3">
          <h2 class="text-base font-semibold">${esc(titleByType(type))}</h2>
        </div>
        <ul class="divide-y divide-slate-100">
          ${items.map(row).join('')}
        </ul>
      </article>
    `;
  }

  function row(it){
    const url = it.url || '#';
    return `
      <li class="py-2">
        <a class="flex items-start gap-3 group" href="${esc(url)}">
          <span class="mt-0.5">${icon(it.type)}</span>
          <span class="min-w-0">
            <div class="text-sm font-medium text-slate-900 group-hover:underline truncate">
              ${esc(it.title || '(제목 없음)')}
            </div>
            ${it.subtitle ? `<div class="text-xs text-slate-500 mt-0.5 truncate">${esc(it.subtitle)}</div>` : ''}
          </span>
        </a>
      </li>
    `;
  }

  function icon(type){
    switch(type){
      case 'approval': return svg(`<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-7 4h8M7 8h10M5 5h14a2 2 0 012 2v10a2 2 0 01-2 2H5a2 2 0 01-2-2V7a2 2 0 012-2z"/>`);
      case 'chat':     return svg(`<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l9 6 9-6M4 6h16a2 2 0 012 2v8a2 2 0 01-2 2H4a2 2 0 01-2-2V8a2 2 0 012-2z"/>`);
      case 'user':     return svg(`<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 14a4 4 0 10-8 0v2a4 4 0 008 0v-2zM12 14a4 4 0 100-8 4 4 0 000 8z"/>`);
      default:         return svg(`<circle cx="12" cy="12" r="9" stroke-width="2"/>`);
    }
  }
  function svg(path){
    return `<svg class="w-5 h-5 text-slate-500" viewBox="0 0 24 24" fill="none" stroke="currentColor">${path}</svg>`;
  }

  function skeleton(){
    return `
      <div class="col-span-full">
        <div class="rounded-xl border border-slate-200 bg-white p-4">
          <div class="animate-pulse space-y-2">
            <div class="h-4 bg-slate-200 rounded w-1/3"></div>
            <div class="h-3 bg-slate-200 rounded"></div>
            <div class="h-3 bg-slate-200 rounded w-2/3"></div>
          </div>
        </div>
      </div>
    `;
  }
  function emptyCard(msg){
    return `
      <div class="col-span-full">
        <div class="rounded-xl border border-slate-200 bg-white p-6 text-center text-slate-500">
          ${esc(msg)}
        </div>
      </div>
    `;
  }
})();
