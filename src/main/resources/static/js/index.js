// === 공통 유틸 ===
function getXsrfFromCookie(name = 'XSRF-TOKEN'){
    const m = document.cookie.match(new RegExp('(?:^|; )' + name + '=([^;]*)'));
    return m ? decodeURIComponent(m[1]) : null;
}
async function api(path, opts={}){
    const xsrf = getXsrfFromCookie();
    const init = {
        headers: { 'Content-Type': 'application/json', ...(xsrf? {'X-XSRF-TOKEN': xsrf} : {}) },
        credentials: 'include',
        ...opts
    };
    const res = await fetch(path, init);
    if(!res.ok) throw new Error(path + ' ' + res.status);
    return res.json();
}
function fmtTime(s){
    if(!s) return '--:--';
    const d = new Date(s);
    return d.toTimeString().slice(0,5);
}
const el = (id)=>document.getElementById(id);


// 사이드바 토글 (모바일)
window.addEventListener('DOMContentLoaded', ()=>{
    el('btnSidebar')?.addEventListener('click',()=>{
        el('sidebar')?.classList.toggle('hidden');
    });


// 프로필 메뉴 토글
    el('btnProfile')?.addEventListener('click',()=>{
        el('menuProfile')?.classList.toggle('hidden');
    });
    document.addEventListener('click', (e)=>{
        if(el('menuProfile') && !el('menuProfile').contains(e.target) && !el('btnProfile').contains(e.target)){
            el('menuProfile').classList.add('hidden');
        }
    });


// 로그아웃 (서버 구현에 맞게 수정)
    el('btnLogout')?.addEventListener('click', async ()=>{
        try { await api('/logout', { method:'POST' }); location.href='/login.html'; } catch(e){ console.error(e); }
    });


    init();
});


// === 대시보드 데이터 로딩 ===
async function loadMe(){
    try {
        const me = await api('/api/user/me');
        const nickname = me.nickname || '사용자';
        el('displayName').textContent = nickname;
        el('greeting').textContent = `안녕하세요, ${nickname}님 👋`;
        el('greetingSub').textContent = new Date().toLocaleDateString('ko-KR', { weekday:'long', month:'long', day:'numeric'}) + ' 일정이 준비됐어요.';
    } catch(e){ console.warn('me 로드 실패', e); }
}


async function loadAttendance() {
    try {
        const a = await api('/api/attendance/summary');
        el('checkIn').textContent = fmtTime(a.checkIn);
        el('weeklyHours').textContent = (a.weeklyHours ?? '--') + '시간';
        el('vacationLeft').textContent = (a.vacationLeft ?? '--') + '일';
        el('statusNow').textContent = a.status || '-';
        document.querySelectorAll('#attendanceBox .skeleton').forEach(x => x.classList.remove('skeleton'));
    } catch (e) {
    }
}