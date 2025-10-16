document.addEventListener("DOMContentLoaded", function() {
	const checkAll = document.getElementById("checkAll");
	if (checkAll) {
		checkAll.addEventListener("change", function(e)	{
			const checked = e.target.checked;
			document.querySelectorAll("input[name='noticeIds']").forEach(cb => cb.checked = checked);
		});
	}
	
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
});

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
