document.addEventListener("DOMContentLoaded", function() {
	// ✅ 전체 선택 체크박스
	const checkAll = document.getElementById("checkAll");
	if (checkAll) {
		checkAll.addEventListener("change", function(e) {
			const checked = e.target.checked;
			document.querySelectorAll("input[name='noticeIds']").forEach(cb => {
				cb.checked = checked;
			});
		});
	}

	// ✅ 삭제 버튼 클릭 시 체크 여부 확인
	const form = document.querySelector("form");
	if (form) {
		form.addEventListener("submit", function(e) {
			const checked = document.querySelectorAll("input[name='noticeIds']:checked");
			if (checked.length === 0) {
				e.preventDefault();
				alert("삭제할 공지사항을 선택해주세요.");
			}
		});
	}

	// ✅ 검색창 입력 확인
	const searchForm = document.getElementById("searchForm");
	if (searchForm) {
		searchForm.addEventListener("submit", function(e) {
			const input = searchForm.querySelector("input[name='keyword']");
			if (!input.value.trim()) {
				e.preventDefault();
				alert("검색어를 입력해주세요.");
				input.focus();
			}
		});
	}

	// ✅ 수정 버튼 클릭 (관리자만 존재함)
	const fixButton = document.getElementById("fix");
	if (fixButton) {
		fixButton.addEventListener("click", function() {
			const checked = document.querySelector("input[name='noticeIds']:checked");
			if (!checked) {
				alert("수정할 공지사항을 선택해주세요.");
				return;
			}
			const noticeId = checked.value;
			location.href = `/Notice_W?notice_id=${noticeId}`;
		});
	}
});
