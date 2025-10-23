// /js/globalSearch.js
(function(){
  const esc = (s)=>String(s ?? '').replace(/[&<>"']/g, m => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#039;'}[m]));
  const $  = (sel,root=document)=>root.querySelector(sel);

  let input, dropdown, listWrap, moreBtn;
  let items = [];     // [{type,title,subtitle,url,icon?}, ...]
  let active = -1;    // -1 = 하이라이트 없음, 0..len-1 = 항목, len = more 버튼
  let usedArrows = false;   // 방향키로 이동했는지 여부
  let debTimer = null;

  document.addEventListener('DOMContentLoaded', () => {
    input    = $('#globalSearch');
    dropdown = $('#searchDropdown');
    if (!input || !dropdown) return;

    // 접근성 역할
    dropdown.setAttribute('role', 'listbox');
    input.setAttribute('role', 'combobox');
    input.setAttribute('aria-expanded', 'false');
    input.setAttribute('aria-autocomplete', 'list');

    input.addEventListener('input', onInput);
    input.addEventListener('keydown', onKeyDown);
    document.addEventListener('click', onDocClick);
  });

  function onInput(e){
    const q = (e.target.value || '').trim();
    clearTimeout(debTimer);
    usedArrows = false;       // 새 입력 → 방향키 초기화
    active = -1;              // 기본적으로 하이라이트 없음(Enter=전체보기)
    if (!q){
      hide();
      return;
    }
    debTimer = setTimeout(() => run(q), 180);
  }

  function onKeyDown(e){
    if (e.key === 'Enter'){
      const q = input.value.trim();
      if (!q) return;

      // 방향키로 선택한 상태면 → 그 항목으로 이동
      if (usedArrows && active >= 0 && active < items.length){
        e.preventDefault();
        go(items[active].url);
        return;
      }
      // "전체 결과 보기" 활성 상태거나, 선택 안했으면 → 전체 결과 페이지
      e.preventDefault();
      go(`/search?q=${encodeURIComponent(q)}`);
      return;
    }

    if (dropdown.classList.contains('hidden')) return;

    const len = items.length;
    if (e.key === 'ArrowDown'){
      e.preventDefault();
      if (len === 0){
        // 항목이 없어도 moreBtn 활성화 가능
        active = 0; usedArrows = true;
        draw();
        return;
      }
      usedArrows = true;
      // active: -1(없음) → 0(첫 항목) → ... → len(전체보기) → 0 ...
      active = (active + 1 + (len + 1)) % (len + 1);
      draw();
    } else if (e.key === 'ArrowUp'){
      e.preventDefault();
      if (len === 0){
        active = -1; usedArrows = true; draw(); return;
      }
      usedArrows = true;
      active = (active - 1 + (len + 1)) % (len + 1);
      draw();
    } else if (e.key === 'Escape'){
      hide();
    }
  }

  function onDocClick(e){
    if (!dropdown.classList.contains('hidden')){
      const wrap = $('#globalSearchWrap');
      if (!wrap.contains(e.target)){
        hide();
      }
    }
  }

  async function run(q){
    try{
      const res = await fetch('/api/search?q=' + encodeURIComponent(q), { credentials:'same-origin' });
      if(!res.ok) throw new Error('search ' + res.status);
      items = await res.json() || [];
      // 기본은 선택 없음(Enter==전체보기)
      active = -1;
      draw();
    }catch(err){
      console.warn('[search] failed', err);
      items = [];
      active = -1;
      draw();
    }
  }

  function draw(){
    dropdown.innerHTML = `
      <div class="gs-head">검색 결과</div>
      <div class="gs-list" id="gsList"></div>
      <div class="gs-more" id="gsMore" role="option" aria-selected="false">전체 결과 보기</div>
    `;
    listWrap = $('#gsList', dropdown);
    moreBtn  = $('#gsMore', dropdown);

    // 리스트 채우기
    if (!items.length){
      listWrap.innerHTML = `<div class="gs-empty">검색 결과가 없습니다.</div>`;
    } else {
      listWrap.innerHTML = items.map((it, idx) => row(it, idx === active)).join('');
      // 클릭 위임
      listWrap.querySelectorAll('.row').forEach((el, i) => {
        el.addEventListener('click', () => go(items[i].url));
        el.addEventListener('mouseenter', () => { active = i; usedArrows = true; highlight(); });
      });
    }

    // more 버튼 이벤트
    moreBtn.addEventListener('click', () => {
      const q = input.value.trim();
      if (q) go(`/search?q=${encodeURIComponent(q)}`);
    });
    moreBtn.addEventListener('mouseenter', () => { active = items.length; usedArrows = true; highlight(); });

    // active 반영
    highlight();
    show();
  }

  function row(it, isActive){
    return `
      <div class="row ${isActive ? 'active' : ''}" role="option" aria-selected="${isActive ? 'true' : 'false'}">
        <div class="icon">${icon(it.type)}</div>
        <div class="min-w-0">
          <div class="title truncate">${esc(it.title || '(제목 없음)')}</div>
          ${it.subtitle ? `<div class="sub truncate">${esc(it.subtitle)}</div>` : ''}
        </div>
      </div>
    `;
  }

  function highlight(){
    const rows = listWrap.querySelectorAll('.row');
    rows.forEach((el, i) => {
      const on = (i === active);
      el.classList.toggle('active', on);
      el.setAttribute('aria-selected', on ? 'true' : 'false');
    });
    const onMore = (active === items.length);
    moreBtn.classList.toggle('active', onMore);
    moreBtn.setAttribute('aria-selected', onMore ? 'true' : 'false');

    // 보이도록 스크롤
    if (active >= 0 && active < items.length){
      rows[active]?.scrollIntoView({ block: 'nearest' });
    } else if (onMore){
      moreBtn.scrollIntoView({ block: 'nearest' });
    }
  }

  function show(){
    dropdown.classList.remove('hidden');
    input.setAttribute('aria-expanded', 'true');
  }
  function hide(){
    dropdown.classList.add('hidden');
    input.setAttribute('aria-expanded', 'false');
  }
  function go(url){
    if (!url) return;
    window.location.href = url;
  }

  function icon(type){
    switch(type){
      case 'user': return `
        <svg class="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M16 14a4 4 0 10-8 0v2a4 4 0 008 0v-2zM12 14a4 4 0 100-8 4 4 0 000 8z"/>
        </svg>`;
      case 'approval': return `
        <svg class="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M9 12h6m-7 4h8M7 8h10M5 5h14a2 2 0 012 2v10a2 2 0 01-2 2H5a2 2 0 01-2-2V7a2 2 0 012-2z"/>
        </svg>`;
      case 'chat': return `
        <svg class="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M3 8l9 6 9-6M4 6h16a2 2 0 012 2v8a2 2 0 01-2 2H4a2 2 0 01-2-2V8a2 2 0 012-2z"/>
        </svg>`;
      default: return `
        <svg class="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor">
          <circle cx="12" cy="12" r="9" stroke-width="2"/>
        </svg>`;
    }
  }
})();
