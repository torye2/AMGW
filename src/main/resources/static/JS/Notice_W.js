//---------------------------------------------------------------
// -----------------------------
// 공통 변수 한 번만 선언
// -----------------------------
const editor = document.getElementById('editor');
const titleInput = document.getElementById('Notice_typetext');
const insertImageBtn = document.getElementById('insertImageBtn');
const imageInput = document.getElementById('imageInput');
const textColorBtn = document.getElementById('textColorBtn');
const bgColorBtn = document.getElementById('bgColorBtn');
const textColorInput = document.getElementById('textColorInput');
const bgColorInput = document.getElementById('bgColorInput');
const noticeForm = document.getElementById("noticeForm");
const noticeDetailInput = document.getElementById("notice_detail_input");

const uploadBtn = document.getElementById('My_PC');
const uploadArea = document.getElementById('upload_area');
const fileListTable = document.getElementById('fileList');
const capacityMsg = document.getElementById('FileCapacity_msg');

const fontSelect = document.getElementById('font_box');
const fontSizeSelect = document.getElementById('font-size');

let uploadedFiles = [];
let savedSelection = null;
let currentAlign = 'left';
let currentList = 'ul';

// 실제 파일 선택을 위한 숨겨진 input 요소 생성
const fileInput = document.createElement('input');
fileInput.type = 'file';
fileInput.multiple = true;
fileInput.style.display = 'none';
document.body.appendChild(fileInput);

//---------------------------------------------------------------
// -----------------------------
// 에디터 기본 기능
// -----------------------------
function applyStyleToSelection(style) {
  const selection = window.getSelection();
  if (!selection.rangeCount) return;
  const range = selection.getRangeAt(0);

  // 선택된 텍스트가 없는 경우
  if (selection.isCollapsed) {
    const span = document.createElement('span');
    span.setAttribute('style', style);
    span.appendChild(document.createTextNode('\u200B')); // 빈문자
    range.insertNode(span);
    range.setStartAfter(span);
    range.setEndAfter(span);
    selection.removeAllRanges();
    selection.addRange(range);
    return;
  }

  // ✅ 선택 영역이 있을 경우 - 안전한 방식으로 처리
  document.execCommand('fontSize', false, '7'); // 임시 적용
  const fontElements = editor.querySelectorAll('font[size="7"]');
  fontElements.forEach(el => {
    el.removeAttribute('size');
    el.style.cssText += style; // 실제 스타일 덮어쓰기
  });
}


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

// execCmd 함수
function execCmd(command) {
    editor.focus();

    // 🔹 기본 명령 처리
    const basicCommands = [
        'bold', 'italic', 'underline', 'strikeThrough',
        'insertUnorderedList', 'insertOrderedList',
        'indent', 'outdent', 'justifyLeft', 'justifyCenter', 'justifyRight', 'justifyFull'
    ];

    if (basicCommands.includes(command)) {
        document.execCommand(command, false, null);
        return;
    }

    // 🔹 링크 삽입
    if (command === 'createLink') {
        const url = prompt('링크 주소(URL)을 입력하세요:');
        if (url) document.execCommand('createLink', false, url);
        return;
    }

    // 🔹 체크박스 삽입
    if (command === 'insertCheckbox') {
        document.execCommand('insertHTML', false, '<input type="checkbox">');
        return;
    }

    // 🔹 구분선 삽입
    if (command === 'insertHr') {
        document.execCommand('insertHTML', false, '<hr>');
        return;
    }

    // 🔹 표 삽입
    if (command === 'insertTable') {
        const rows = parseInt(prompt('행 개수 입력', '2'));
        const cols = parseInt(prompt('열 개수 입력', '2'));
        if (isNaN(rows) || isNaN(cols) || rows <= 0 || cols <= 0) return;

        let tableHTML = '<table border="1" style="border-collapse: collapse; border: 1px solid #000;">';
        for (let i = 0; i < rows; i++) {
            tableHTML += '<tr>';
            for (let j = 0; j < cols; j++) {
                tableHTML += '<td style="padding:5px; border: 1px solid #000;">&nbsp;</td>';
            }
            tableHTML += '</tr>';
        }
        tableHTML += '</table><br>';

        const selection = window.getSelection();
        if (selection.rangeCount > 0) {
            const range = selection.getRangeAt(0);
            const tempDiv = document.createElement('div');
            tempDiv.innerHTML = tableHTML;
            const fragment = document.createDocumentFragment();
            let node;
            while ((node = tempDiv.firstChild)) {
                fragment.appendChild(node);
            }
            range.insertNode(fragment);
        } else {
            editor.innerHTML += tableHTML;
        }
        return;
    }
}


//---------------------------------------------------------------
// -----------------------------
// 메뉴 토글
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

// 정렬
function setAlign(type) {
    document.execCommand("justify" + type, false, null);
    updateButtonState();
    document.getElementById("alignMenu").style.display = "none";
}

// 리스트
function updateListAndAlignIcons() {
    // 여기서는 필요하면 추후 코드 추가 가능
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
// -----------------------------
// 글꼴 / 글자 크기
// -----------------------------
if (fontSelect) {
  fontSelect.addEventListener('change', e => {
    const val = e.target.value;
    document.execCommand('fontName', false, val);
  });
}

if (fontSizeSelect) {
    fontSizeSelect.addEventListener('change', e => {
        const val = e.target.value;
        applyStyleToSelection(`font-size: ${val};`);
    });
}

//---------------------------------------------------------------
// -----------------------------
// 에디터 이벤트
// -----------------------------
editor.addEventListener('click', updateButtonState);
editor.addEventListener('keyup', updateButtonState);
editor.addEventListener('input', updateListAndAlignIcons);

//---------------------------------------------------------------
// -----------------------------
// 메뉴 버튼 이벤트
// -----------------------------
document.getElementById('alignBtn').addEventListener('click', e => { e.stopPropagation(); toggleMenu('alignMenu'); });
document.getElementById('listBtn').addEventListener('click', e => { e.stopPropagation(); toggleMenu('listMenu'); });
document.getElementById('lineHeightBtn').addEventListener('click', e => { e.stopPropagation(); toggleMenu('lineHeightMenu'); });

document.querySelectorAll('#alignMenu, #listMenu, #lineHeightMenu').forEach(menu => 
    menu.addEventListener('click', e => e.stopPropagation())
);

// 정렬 메뉴
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

// 리스트 메뉴
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

// 라인 높이 메뉴
document.querySelectorAll('#lineHeightMenu button').forEach(btn => {
    btn.addEventListener('click', function () {
        const lineHeight = this.getAttribute('data-line');
        setLineHeightForSelection(lineHeight);
        hideAllMenus();
    });
});

//---------------------------------------------------------------
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

//---------------------------------------------------------------
// -----------------------------
// 텍스트 색상 / 배경색
// -----------------------------
// 색상 버튼 클릭 시 color input 열기
textColorBtn.addEventListener('click', () => textColorInput.click());
bgColorBtn.addEventListener('click', () => bgColorInput.click());

// 텍스트 색상 적용
textColorInput.addEventListener('input', e => {
    editor.focus(); // 반드시 에디터에 포커스
    document.execCommand('foreColor', false, e.target.value);
});

// 배경색 적용
bgColorInput.addEventListener('input', e => {
    editor.focus(); // 반드시 에디터에 포커스
    document.execCommand('hiliteColor', false, e.target.value);
});

// 선택 영역에 안전하게 스타일 적용
function applyColorToSelection(cssProp, value) {
    const sel = window.getSelection();
    if (!sel.rangeCount) return;
    const range = sel.getRangeAt(0);

    if (range.collapsed) {
        // 커서 위치만 있는 경우, 스팬 삽입
        const span = document.createElement('span');
        span.style[cssProp] = value;
        span.appendChild(document.createTextNode('\u200B'));
        range.insertNode(span);
        range.setStartAfter(span);
        range.setEndAfter(span);
        sel.removeAllRanges();
        sel.addRange(range);
        return;
    }

    // 선택 영역이 있을 경우
    const contents = range.extractContents();
    const wrapper = document.createElement('span');
    wrapper.style[cssProp] = value;
    wrapper.appendChild(contents);
    range.insertNode(wrapper);

    // 선택 영역 다시 선택
    sel.removeAllRanges();
    const newRange = document.createRange();
    newRange.selectNodeContents(wrapper);
    sel.addRange(newRange);
}


//---------------------------------------------------------------
// -----------------------------
// 제목 클릭 포커스
// -----------------------------
if (titleInput) titleInput.addEventListener('click', () => titleInput.focus());

//---------------------------------------------------------------
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

//---------------------------------------------------------------
// -----------------------------
// UI 에디터 클릭 시 메뉴 닫기
// -----------------------------
document.querySelectorAll('.UI_editor button').forEach(btn => {
    btn.addEventListener('click', function () {
        const exceptionIds = ['alignBtn','listBtn','lineHeightBtn','textColorBtn','bgColorBtn'];
        if (!exceptionIds.includes(this.id)) hideAllMenus();
    });
});

document.addEventListener('click', event => {
    const toolbar = document.querySelector('.UI_editor');
    if (!toolbar.contains(event.target) && !editor.contains(event.target) && event.target !== titleInput) {
        hideAllMenus();
        const sel = window.getSelection();
        sel.removeAllRanges();
        savedSelection = null;
    }
});

//---------------------------------------------------------------
// -----------------------------
// 파일 업로드 처리
// -----------------------------
uploadBtn.addEventListener('click', () => fileInput.click());
fileInput.addEventListener('change', (e) => { handleFiles(e.target.files); fileInput.value = ""; });

if (uploadArea) {
    uploadArea.addEventListener('dragover', (e) => { e.preventDefault(); uploadArea.classList.add('dragover'); });
    uploadArea.addEventListener('dragleave', () => uploadArea.classList.remove('dragover'));
    uploadArea.addEventListener('drop', (e) => { e.preventDefault(); uploadArea.classList.remove('dragover'); handleFiles(e.dataTransfer.files); });
}

function handleFiles(files) {
    const uploadMsg = document.getElementById('upload_msg');
    const tbody = fileListTable.querySelector('tbody') || fileListTable.appendChild(document.createElement('tbody'));
    if (files.length > 0) { uploadMsg.style.display = "none"; fileListTable.style.display = "table"; }

    for (const file of files) {
        if (uploadedFiles.length >= 5) { alert("최대 5개의 파일까지만 업로드 할 수 있습니다."); break; }
        uploadedFiles.push(file);

        const row = document.createElement('tr');
        const delCell = document.createElement('td'); delCell.textContent = 'X';
        delCell.addEventListener('click', () => {
            row.remove();
            uploadedFiles = uploadedFiles.filter(f => f !== file);
            updateCapacityInfo();
            if (tbody.children.length === 0) { fileListTable.style.display = "none"; uploadMsg.style.display = "block"; }
        });
        const nameCell = document.createElement("td"); nameCell.textContent = file.name;
        const sizeCell = document.createElement("td"); sizeCell.textContent = `${(file.size / 1024).toFixed(1)} KB`;
        row.append(delCell, nameCell, sizeCell);
        tbody.appendChild(row);
    }
    updateCapacityInfo();
}

function updateCapacityInfo() {
    const totalSizeKB = uploadedFiles.reduce((sum, f) => sum + f.size / 1024, 0);
    capacityMsg.querySelector("p").textContent = `총 ${(totalSizeKB / 1024).toFixed(2)} MB / 최대 2.00GB x 5개`;
}

//---------------------------------------------------------------
// -----------------------------
// 공지사항 제출 처리
// -----------------------------
noticeForm.addEventListener("submit", function(e) {
    noticeDetailInput.value = editor.innerHTML;
    const existingInputs = document.querySelectorAll('input[name="uploadFiles"]')
    existingInputs.forEach(input => input.remove());
    uploadedFiles.forEach(file => {
        const hiddenInput = document.createElement('input');
        hiddenInput.type = 'file';
        hiddenInput.name = 'uploadFiles';
        hiddenInput.files = createFileList(file);
        hiddenInput.style.display = 'none';
        noticeForm.appendChild(hiddenInput);
    });
});

function createFileList(file) {
    const dataTransfer = new DataTransfer();
    dataTransfer.items.add(file);
    return dataTransfer.files;
}
