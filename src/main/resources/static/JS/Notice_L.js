document.addEventListener("DOMContentLoaded", function() {
	// 전체 선택 체크박스
	const checkAll = document.getElementById("checkAll");
	if (checkAll) {
		checkAll.addEventListener("change", function(e)	{
			const checked = e.target.checked;
			document.querySelectorAll("input[name='noticeIds']").forEach(cb => cb.checked = checked);
		});
	}
	// 삭제버튼 클릭 시 체크 확인
	const form = document.querySelector("form");
	if(form) {
	   form.addEventListener("submit", function(e) {
			const checked = document.querySelectorAll("input[name='noticeIds']:checked");
			if(checked.length === 0) {
				e.preventDefault();
				alert("삭제할 공지사항을 선택해주세요.");
			}
	   });
	}
	
	//검색창 입력 확인
	const searchForm = document.getElementById("searchForm");
	if (searchForm) {
		searchForm.addEventListener("submit", function(e) {
			const input = searchForm.querySelector("input[name='keyword']");
			if (!input.value.trim()) {
				e.preventDefault();
				alert("검색어를 입력해주세요");
				input.focus();
			}
		})
	}
});

//수정 버튼
document.getElementById("fix").addEventListener("click", function() {
	const checked = document.querySelector("input[name='noticeIds']:checked");
	if (!checked) {
		alert("수정할 공지사항을 선택해주세요");
		return;
	}
	
	const noticeId = checked.value;
	// 수정 페이지로 이동, notice_id를 URL 파라미터로 전달
	location.href = `/Notice_W?notice_id=${noticeId}`;
});