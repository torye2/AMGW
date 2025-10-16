// -----------------------------
// 에디터 관련 기본 변수
// -----------------------------
const editor = document.getElementById('editor');
const titleInput = document.getElementById('Notice_typetext');
const insertImageBtn = document.getElementById('insertImageBtn');
const imageInput = document.getElementById('imageInput');
const textColorBtn = document.getElementById('textColorBtn');
const bgColorBtn = document.getElementById('bgColorBtn');
const textColorInput = document.getElementById('textColorInput');
const bgColorInput = document.getElementById('bgColorInput');
let savedSelection = null;
let currentAlign = 'left';
let currentList = 'ul';

// -----------------------------
// 글꼴, 글자크기
// -----------------------------
const fontSelect = document.getElementById('font_box');
if (fontSelect) {
    fontSelect.addEventListener('change', e => {
        const val = e.target.value;
        try { document.execCommand('fontName', false, val); } catch {}
        applyStyleToSelection(`font-family: ${val};`);
    });
}

const fontSizeSelect = document.getElementById('font-size');
if (fontSizeSelect) {
    fontSizeSelect.addEventListener('change', e => {
        const val = e.target.value;
        applyStyleToSelection(`font-size: ${val};`);
    });
}

// -----------------------------
// 메뉴 토글 관련
// -----------------------------
function hideAllMenus(exceptId = null) {
    const menus = ['alignMenu', 'listMenu', 'lineHeightMenu'];
    menus.forEach(id => {
        if (id !== exceptId) {
            const el = document.getElementById(id);
            if (el) el.style.display = 'none';
        }
    });
}

function toggleMenu(menuId) {
    const menu = document.getElementById(menuId);
    const isVisible = menu.style.display === "block";
    hideAllMenus(isVisible ? null : menuId);
    menu.style.display = isVisible ? "none" : "block";
}

// -----------------------------
// 정렬
// -----------------------------
function setAlign(type) {
    document.execCommand("justify" + type, false, null);
    updateButtonState();
    document.getElementById("alignMenu").style.display = "none";
}

// -----------------------------
// 선택 영역 스타일 적용
// -----------------------------
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

function applyHighlightToSelection(color) {
    applyStyleToSelection(`background-color: ${color};`);
}

// -----------------------------
// 선택 영역 저장/복원
// -----------------------------
function saveSelection() {
    const sel = window.getSelection();
    if (sel.rangeCount > 0) savedSelection = sel.getRangeAt(0).cloneRange();
}

function restoreSelection() {
    const sel = window.getSelection();
    editor.focus();
    sel.removeAllRanges();
    if (savedSelection) sel.addRange(savedSelection);
}

// -----------------------------
// 버튼 상태 업데이트
// -----------------------------
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

// -----------------------------
// 에디터 이벤트
// -----------------------------
editor.addEventListener('click', updateButtonState);
editor.addEventListener('keyup', updateButtonState);
editor.addEventListener('input', updateListAndAlignIcons);

// -----------------------------
// 메뉴 버튼 이벤트
// -----------------------------
document.getElementById('alignBtn').addEventListener('click', e => { e.stopPropagation(); toggleMenu('alignMenu'); });
document.getElementById('listBtn').addEventListener('click', e => { e.stopPropagation(); toggleMenu('listMenu'); });
document.getElementById('lineHeightBtn').addEventListener('click', e => { e.stopPropagation(); toggleMenu('lineHeightMenu'); });

document.querySelectorAll('#alignMenu, #listMenu, #lineHeightMenu').forEach(menu => 
    menu.addEventListener('click', e => e.stopPropagation())
);

// -----------------------------
// 정렬 메뉴 버튼
// -----------------------------
document.querySelectorAll('#alignMenu button').forEach((btn, index) => {
    btn.addEventListener('click', () => {
        const types = ["Left", "Center", "Right", "Full"];
        const icons = ['fa-align-left', 'fa-align-center', 'fa-align-right', 'fa-align-justify'];
        setAlign(types[index]);
        currentAlign = types[index].toLowerCase();
        const icon = document.getElementById('alignBtn').querySelector('i');
        icon.className = 'fa-solid ' + icons[index];
    });
});

// -----------------------------
// 리스트 메뉴 버튼
// -----------------------------
document.querySelectorAll('#listMenu button').forEach((btn, index) => {
    btn.addEventListener('click', () => {
        const icons = ['fa-list-ul', 'fa-list-ol', 'fa-list-check'];
        if (index === 0) { execCmd('insertUnorderedList'); currentList = 'ul'; }
        else if (index === 1) { execCmd('insertOrderedList'); currentList = 'ol'; }
        else if (index === 2) { execCmd('insertCheckbox'); currentList = 'check'; }
        const icon = document.getElementById('listBtn').querySelector('i');
        icon.className = 'fa-solid ' + icons[index];
        hideAllMenus();
    });
});

// -----------------------------
// 라인 높이
// -----------------------------
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

document.querySelectorAll('#lineHeightMenu button').forEach(btn => {
    btn.addEventListener('click', function () {
        const lineHeight = this.getAttribute('data-line');
        setLineHeightForSelection(lineHeight);
        hideAllMenus();
    });
});

// -----------------------------
// 에디터 외부 클릭 시 메뉴 닫기
// -----------------------------
document.addEventListener('click', event => {
    const toolbar = document.querySelector('.UI_editor');
    if (!toolbar.contains(event.target) && !editor.contains(event.target) && event.target !== titleInput) {
        hideAllMenus();
        const sel = window.getSelection();
        sel.removeAllRanges();
        savedSelection = null;
    }
});

// -----------------------------
// 이미지 삽입
// -----------------------------
insertImageBtn.addEventListener('click', e => { e.preventDefault(); imageInput.click(); });

imageInput.addEventListener('change', e => {
    const file = e.target.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = evt => {
        editor.focus();
        const img = document.createElement('img');
        img.src = evt.target.result;
        img.style.maxWidth = '100%';
        img.style.display = 'block';
        img.style.margin = '5px 0';
        const sel = window.getSelection();
        if (sel.rangeCount > 0) {
            const range = sel.getRangeAt(0);
            range.insertNode(img);
            const zwsp = document.createTextNode('\u200B');
            img.after(zwsp);
            range.setStartAfter(zwsp);
            range.setEndAfter(zwsp);
            sel.removeAllRanges();
            sel.addRange(range);
        } else {
            editor.appendChild(img);
        }
    };
    reader.readAsDataURL(file);
    e.target.value = '';
});

// -----------------------------
// 텍스트 색상 & 배경색
// -----------------------------
textColorBtn.addEventListener('click', () => { saveSelection(); setTimeout(() => textColorInput.click(), 0); });
bgColorBtn.addEventListener('click', () => { saveSelection(); setTimeout(() => bgColorInput.click(), 0); });

textColorInput.addEventListener('input', e => {
    const color = e.target.value;
    setTimeout(() => { restoreSelection(); applyStyleToSelection(`color: ${color};`); }, 10);
});

bgColorInput.addEventListener('input', e => {
    const color = e.target.value;
    setTimeout(() => { restoreSelection(); applyHighlightToSelection(color); }, 10);
});

// -----------------------------
// 제목 클릭 포커스
// -----------------------------
if (titleInput) {
    titleInput.addEventListener('click', () => titleInput.focus());
}

// -----------------------------
// 버튼 스타일 토글
// -----------------------------
document.addEventListener('DOMContentLoaded', () => {
    function toggleStyle(command, btnId) {
        const btn = document.getElementById(btnId);
        restoreSelection();
        document.execCommand(command);
        setTimeout(() => { btn.classList.toggle('active', document.queryCommandState(command)); }, 10);
    }

    ['boldBtn','italicBtn','underlineBtn','strikeBtn'].forEach(id => {
        const command = id === 'strikeBtn' ? 'strikeThrough' : id.replace('Btn','');
        document.getElementById(id).addEventListener('click', () => toggleStyle(command, id));
    });
});

// -----------------------------
// UI 에디터 버튼 클릭시 메뉴 닫기
// -----------------------------
document.querySelectorAll('.UI_editor button').forEach(btn => {
    btn.addEventListener('click', function () {
        const exceptionIds = ['alignBtn','listBtn','lineHeightBtn','textColorBtn','bgColorBtn'];
        if (!exceptionIds.includes(this.id)) hideAllMenus();
    });
});

// -----------------------------
// execCmd 함수
// -----------------------------
function execCmd(command) {
    editor.focus();
    console.log('execCmd 포커싱');
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
    hideAllMenus();
}
