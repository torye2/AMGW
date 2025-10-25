//---------------------------------------------------------------
// 공통 변수 선언
//---------------------------------------------------------------
const editor = document.getElementById('editor');
const titleInput = document.getElementById('Compliment_typetext');
const insertImageBtn = document.getElementById('insertImageBtn');
const imageInput = document.getElementById('imageInput');
const textColorBtn = document.getElementById('textColorBtn');
const bgColorBtn = document.getElementById('bgColorBtn');
const textColorInput = document.getElementById('textColorInput');
const bgColorInput = document.getElementById('bgColorInput');
const noticeDetailInput = document.getElementById("notice_detail_input");

// HTML에는 form이 주석 처리되어 있으므로 null 에러 방지
const ComplimentForm = document.getElementById('ComplimentForm') || null;

const fontSelect = document.getElementById('font_box');
const fontSizeSelect = document.getElementById('font-size');

let savedSelection = null;
let currentAlign = 'left';
let currentList = 'ul';

//---------------------------------------------------------------
// 에디터 스타일 적용 함수
//---------------------------------------------------------------
function applyStyleToSelection(style) {
    const selection = window.getSelection();
    if (!selection.rangeCount) return;
    const range = selection.getRangeAt(0);
    const span = document.createElement('span');
    span.setAttribute('style', style);
    if (selection.isCollapsed) {
        span.appendChild(document.createTextNode('\u200B'));
        range.insertNode(span);
        range.setStartAfter(span);
        range.setEndAfter(span);
        selection.removeAllRanges();
        selection.addRange(range);
    } else {
        range.surroundContents(span);
    }
}

function updateButtonState() {
    const btns = [
        { id: 'boldBtn', state: 'bold' },
        { id: 'italicBtn', state: 'italic' },
        { id: 'underlineBtn', state: 'underline' },
        { id: 'strikeBtn', state: 'strikeThrough' }
    ];
    btns.forEach(b => {
        const el = document.getElementById(b.id);
        if (el) el.classList.toggle('active', document.queryCommandState(b.state));
    });
}

//---------------------------------------------------------------
// 명령 실행
//---------------------------------------------------------------
function execCmd(command) {
    editor.focus();
    if (command === 'createLink') {
        const url = prompt('링크 주소(URL)을 입력하세요:');
        if (url) document.execCommand('createLink', false, url);
    } else if (command === 'insertCheckbox') {
        document.execCommand('insertHTML', false, '<input type="checkbox">');
    } else if (command === 'insertHr') {
        document.execCommand('insertHTML', false, '<hr>');
    } else if (command === 'insertTable') {
        const rows = parseInt(prompt('행 개수 입력', '2'));
        const cols = parseInt(prompt('열 개수 입력', '2'));
        if (isNaN(rows) || isNaN(cols) || rows <= 0 || cols <= 0) return;
        let tableHTML = '<table border="1" style="border-collapse: collapse;">';
        for (let i = 0; i < rows; i++) {
            tableHTML += '<tr>';
            for (let j = 0; j < cols; j++) tableHTML += '<td>&nbsp;</td>';
            tableHTML += '</tr>';
        }
        tableHTML += '</table><br>';
        document.execCommand('insertHTML', false, tableHTML);
    } else {
        document.execCommand(command, false, null);
    }
    updateButtonState();
}

//---------------------------------------------------------------
// 메뉴 관련
//---------------------------------------------------------------
function hideAllMenus(exceptId = null) {
    ['alignMenu', 'listMenu', 'lineHeightMenu'].forEach(id => {
        const el = document.getElementById(id);
        if (el && id !== exceptId) el.style.display = 'none';
    });
}

function toggleMenu(menuId) {
    const menu = document.getElementById(menuId);
    const isVisible = menu.style.display === "block";
    hideAllMenus(isVisible ? null : menuId);
    menu.style.display = isVisible ? "none" : "block";
}

// 정렬 적용
function setAlign(type) {
    document.execCommand("justify" + type, false, null);
    updateButtonState();
    document.getElementById("alignMenu").style.display = "none";
}

// 라인 높이
function setLineHeightForSelection(lineHeight) {
    const sel = window.getSelection();
    if (!sel || sel.rangeCount === 0) {
        editor.style.lineHeight = lineHeight;
        return;
    }
    const range = sel.getRangeAt(0);
    let node = sel.anchorNode;
    if (node && node.nodeType !== 1) node = node.parentNode;
    while (node && node.id !== 'editor' && window.getComputedStyle(node).display === 'inline') node = node.parentNode;
    if (node && node !== editor) node.style.lineHeight = lineHeight;
    else {
        const wrapper = document.createElement('div');
        wrapper.style.lineHeight = lineHeight;
        wrapper.appendChild(range.extractContents());
        range.insertNode(wrapper);
        sel.removeAllRanges();
        const newRange = document.createRange();
        newRange.selectNodeContents(wrapper);
        sel.addRange(newRange);
    }
    updateButtonState();
    updateListAndAlignIcons();
}
//---------------------------------------------------------------
// 글꼴 / 글자 크기
//---------------------------------------------------------------
if (fontSelect) {
    fontSelect.addEventListener('change', e => {
        const val = e.target.value;
        try { document.execCommand('fontName', false, val); } catch {}
        applyStyleToSelection(`font-family: ${val};`);
    });
}

if (fontSizeSelect) {
    fontSizeSelect.addEventListener('change', e => {
        const val = e.target.value;
        applyStyleToSelection(`font-size: ${val};`);
    });
}

//---------------------------------------------------------------
// 에디터 이벤트
//---------------------------------------------------------------
editor.addEventListener('click', updateButtonState);
editor.addEventListener('keyup', updateButtonState);

//---------------------------------------------------------------
// 메뉴 버튼 이벤트 연결
//---------------------------------------------------------------
document.getElementById('alignBtn').addEventListener('click', e => { e.stopPropagation(); toggleMenu('alignMenu'); });
document.getElementById('listBtn').addEventListener('click', e => { e.stopPropagation(); toggleMenu('listMenu'); });
document.getElementById('lineHeightBtn').addEventListener('click', e => { e.stopPropagation(); toggleMenu('lineHeightMenu'); });

// 정렬 메뉴
document.querySelectorAll('#alignMenu button').forEach((btn, index) => {
    const types = ["Left", "Center", "Right", "Full"];
    const icons = ['fa-align-left', 'fa-align-center', 'fa-align-right', 'fa-align-justify'];
    btn.addEventListener('click', () => {
        setAlign(types[index]);
        const icon = document.getElementById('alignBtn').querySelector('i');
        icon.className = 'fa-solid ' + icons[index];
        hideAllMenus();
    });
});

// 리스트 메뉴
document.querySelectorAll('#listMenu button').forEach((btn, index) => {
    const icons = ['fa-list-ul', 'fa-list-ol', 'fa-list-check'];
    btn.addEventListener('click', () => {
        if (index === 0) execCmd('insertUnorderedList');
        else if (index === 1) execCmd('insertOrderedList');
        else if (index === 2) execCmd('insertCheckbox');
        const icon = document.getElementById('listBtn').querySelector('i');
        icon.className = 'fa-solid ' + icons[index];
        hideAllMenus();
    });
});

// 라인 높이 메뉴
document.querySelectorAll('#lineHeightMenu button').forEach(btn => {
    btn.addEventListener('click', function () {
        const lineHeight = this.getAttribute('data-line');
        setLineHeightForSelection(lineHeight);
        hideAllMenus();
    });
});

//---------------------------------------------------------------
// 이미지 삽입
//---------------------------------------------------------------
insertImageBtn.addEventListener('click', e => { e.preventDefault(); imageInput.click(); });

imageInput.addEventListener('change', e => {
    const file = e.target.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = evt => {
        const img = document.createElement('img');
        img.src = evt.target.result;
        img.style.maxWidth = '100%';
        img.style.display = 'block';
        img.style.margin = '5px 0';
        editor.focus();
        document.execCommand('insertHTML', false, img.outerHTML);
    };
    reader.readAsDataURL(file);
});

//---------------------------------------------------------------
// 텍스트 색상 / 배경색
//---------------------------------------------------------------
textColorBtn.addEventListener('click', () => textColorInput.click());
bgColorBtn.addEventListener('click', () => bgColorInput.click());

textColorInput.addEventListener('input', e => {
    editor.focus();
    document.execCommand('foreColor', false, e.target.value);
});

bgColorInput.addEventListener('input', e => {
    editor.focus();
    document.execCommand('hiliteColor', false, e.target.value);
});

//---------------------------------------------------------------
// Form submit 시 에디터 내용을 hidden input으로 전달
//---------------------------------------------------------------
const complimentForm = document.getElementById('ComplimentForm'); // HTML id와 정확히 맞춤
const complimentDetailInput = document.getElementById('Compliment_detail_input'); // HTML id와 정확히 맞춤

if (complimentForm) {
    complimentForm.addEventListener('submit', function(e) {
        if (editor && complimentDetailInput) {
            // contenteditable div 내용을 hidden input에 넣어서 서버로 전송
            complimentDetailInput.value = editor.innerHTML;
        }
    });
}