(function(){
  const $ = (s, d=document)=>d.querySelector(s);
  const msg = $("#attMsg");

  function csrf(){
    const t = $('meta[name="_csrf"]')?.content;
    const h = $('meta[name="_csrf_header"]')?.content;
    return {t,h};
  }

  async function api(path, {method="GET", body=null}={}){
    const {t,h} = csrf();
    const headers = {};
    if(h && t) headers[h] = t;

    let init = { method, headers, credentials: 'include' };
    if(body instanceof FormData){
      // 그대로
    } else if(body && typeof body === 'object'){
      headers['Content-Type'] = 'application/x-www-form-urlencoded;charset=UTF-8';
      const p = new URLSearchParams(body);
      body = p.toString();
    }
    if(body) init.body = body;

    const res = await fetch(path, init);
    if(!res.ok) throw new Error(await res.text());
    const ct = res.headers.get('content-type')||'';
    return ct.includes('application/json') ? res.json() : res.text();
  }

  async function loadMy(){
    const tbody = $("#myReqTbody");
    tbody.innerHTML = `<tr><td colspan="7" class="py-6 text-center text-slate-400">불러오는 중…</td></tr>`;
    try{
      const rows = await api('/api/attendance/requests/my');
      if(!rows.length){
        tbody.innerHTML = `<tr><td colspan="7" class="py-6 text-center text-slate-400">내 신청이 없습니다.</td></tr>`;
        return;
      }
      tbody.innerHTML = rows.map(r=>{
        const period = `${r.startDate} ~ ${r.endDate}`;
        const time = (r.startTime && r.endTime) ? `${r.startTime}~${r.endTime}` : '-';
        const created = r.createdAt ? new Date(r.createdAt).toLocaleString() : '-';
        return `<tr>
          <td class="py-2 pr-3">${r.id}</td>
          <td class="py-2 pr-3">${r.type}</td>
          <td class="py-2 pr-3">${period}</td>
          <td class="py-2 pr-3">${time}</td>
          <td class="py-2 pr-3">${r.reason??''}</td>
          <td class="py-2 pr-3">${r.status}</td>
          <td class="py-2 pr-3">${created}</td>
        </tr>`;
      }).join('');
    }catch(e){
      tbody.innerHTML = `<tr><td colspan="7" class="py-6 text-center text-rose-500">목록 로드 실패</td></tr>`;
      console.error(e);
    }
  }

  $("#attForm")?.addEventListener('submit', async (e)=>{
    e.preventDefault();
    const fd = new FormData(e.currentTarget);

    // 필수값 보정
    if(!fd.get('startDate') || !fd.get('endDate')) {
      msg.textContent = '시작/종료일을 입력하세요.'; return;
    }

    try{
      await api('/api/attendance/requests', {
        method:'POST',
        body: fd // application/x-www-form-urlencoded 로 자동 변환 안 함 (위에서 그대로 보냄)
      });
      msg.textContent = '신청이 등록되었습니다.';
      e.currentTarget.reset();
      loadMy();
    }catch(err){
      console.error(err);
      msg.textContent = '신청 중 오류가 발생했습니다.';
    }
  });

  $("#btnReload")?.addEventListener('click', loadMy);

  // 초기 로드
  loadMy();
})();
