document.addEventListener("DOMContentLoaded", function() {
  const checkAll = document.getElementById("checkAll");
  const checkboxes = document.querySelectorAll('input[name="check"]');
  const deleteBtn = document.getElementById("deleteBtn");
  const editBtn = document.getElementById("editBtn");
	// ✅ 헤더 객체 별도 생성
	// ✅ CSRF 토큰 읽기
	const csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
	const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
	const csrfToken = csrfTokenMeta ? csrfTokenMeta.getAttribute("content") : null;
	const csrfHeader = csrfHeaderMeta ? csrfHeaderMeta.getAttribute("content") : null;

  // 전체 선택
  if (checkAll) {
    checkAll.addEventListener("change", function() {
      checkboxes.forEach(cb => cb.checked = checkAll.checked);
    });
  }

  // 삭제 버튼 클릭 이벤트
  if (deleteBtn) {
	deleteBtn.addEventListener("click", function(e) {
	    e.preventDefault();

	    const selectedIds = Array.from(checkboxes)
	      .filter(cb => cb.checked)
	      .map(cb => parseInt(cb.value));

	    if (selectedIds.length === 0) {
	      alert("삭제할 게시글을 선택해주세요.");
	      return;
	    }

	    if (!confirm("선택한 게시글을 삭제하시겠습니까?")) return;
	      const headers = {
			"Content-Type": "application/json"
		  };
	      if (csrfHeader && csrfToken) {
	        headers[csrfHeader] = csrfToken; // 동적으로 헤더 추가
	      }
		  
	    fetch("/Compliment_Delete", {
	      method: "POST",
		  headers: headers,
		  body: JSON.stringify(selectedIds)
	    })
	    .then(response => {
	      if (response.ok) {
	        alert("삭제되었습니다.");
	        location.reload();
	      } else {
	        alert("삭제 중 오류가 발생했습니다.");
	      }
	    })
	    .catch(() => alert("서버 오류가 발생했습니다."));
  	});
}
  //수정 버튼 클릭 이벤트발생
  if (editBtn) {
      editBtn.addEventListener("click", function(e) {
        e.preventDefault();

        const selectedIds = Array.from(checkboxes)
          .filter(cb => cb.checked)
          .map(cb => cb.value);

        if (selectedIds.length === 0) {
          alert("수정할 게시글을 선택해주세요.");
          return;
        }

        if (selectedIds.length > 1) {
          alert("한 번에 하나의 게시글만 수정할 수 있습니다.");
          return;
        }

        const id = selectedIds[0];
        // 수정 페이지로 이동 (글 ID를 전달)
        window.location.href = `/Compliment_W?compliment_id=${id}`;
      });
    }
	
	//글쓰기 버튼 클릭시 이동
	document.getElementById("writeBtn").addEventListener("click", function() {
	    window.location.href = "/Compliment_W";
	});
});
