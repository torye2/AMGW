# AMGW — 실시간 협업/근태/결재/캘린더 통합 그룹웨어
[![Backend](https://img.shields.io/badge/Backend-Spring%20Boot-green)]()
[![DB](https://img.shields.io/badge/DB-MySQL-blue)]()
[![Lang](https://img.shields.io/badge/Lang-Java-yellow)]()

## 🎯 Overview
조직 내에서 발생하는 **협업/커뮤니케이션/업무관리 기능을 한 곳에 통합**한 그룹웨어 플랫폼입니다.

- **출퇴근 기록 / 휴가 신청 / 근무시간 요약**
- **전자 결재 문서 생성 및 승인 프로세스**
- **조직 캘린더 일정 공유**
- **실시간 채팅 및 읽음 표시**
- **WebRTC 기반 화상 회의**
- **개인 Todo 업무 관리**

프론트는 팀원과 협업하였으며, 저는 **백엔드 전반 설계 및 서비스 로직 구현**을 담당했습니다.

---

## ✨ Key Features (담당 영역 중심)

### 🕒 출결 관리 (Attendance)
- 출근/퇴근 기록 저장 (`AttendancePunchService`)
- 휴가/연장근무 신청 처리 (`AttendanceRequest`)
- 근무 시간 요약 제공 (`AttendanceSummaryService`)
- **트랜잭션 단위로 Punch/Request/Summary 역할을 명확히 분리**하여 유지보수성 확보

### 📄 전자 결재 (Approval)
- 결재 문서 열람/결재/반려 상태 기록 (`ApprovalService`)
- 사용자 + 문서 조합으로 **열람 상태를 복합키**로 관리 (`ApprovalDoc`)
- 문서별 읽음/미읽음 현황 실시간 반영

### 🗓 조직 캘린더 (Calendar)
- 개인/조직 단위 일정 생성 및 조회
- 시간 범위 / 유저 / 카테고리 조건 필터링 (`CalendarService`)
- 반복 일정 / 그룹 일정 공통 모델링

### 💬 실시간 채팅 (Chat)
- 채팅방 / 참여자 / 메시지 / 읽음 상태 **도메인 분리 설계**
- WebSocket 기반 실시간 메시지 전송 (`ChatWsController`)
- 클라이언트가 메시지를 읽을 때 **읽음 처리 및 대화 목록 최신화**

### 🎥 WebRTC 화상 회의 (Meeting)
- `WebRtcSignalController`에서 Offer/Answer/ICE Candidate 교환 처리
- 다대다 회의 구성이 가능하도록 **신호 서버 역할만 수행**

### ✅ Todo 업무 관리
- 개인 Todo CRUD + 완료 상태 관리 (`TodoService`)
- 비즈니스 로직을 Controller가 아닌 Service에서 처리하여 **역할 분리 유지**

---

## 🧩 Architecture (요약)
| 계층 | 역할 |
|---|---|
| **Controller** | 요청 라우팅 / DTO 변환 / 인증 체크 |
| **Service** | 트랜잭션, 유효성 검사, 도메인 규칙 처리 |
| **Repository** | JPA 기반 데이터 액세스 |
| **Entity** | 비즈니스 상태 및 관계 모델링 |

- **실시간**: WebSocket(STOMP), WebRTC Signaling
- **DB**: MySQL + JPA (지연 로딩 최적화 + 관계 정규화)
- **패키지 분리**: `attendance`, `approval`, `calendar`, `chat`, `meeting`, `todo` 단위

---

## 🧭 설계 포인트 (중요)

### 1) 실시간 채팅 읽음 상태 모델링
| 테이블 | 설명 |
|---|---|
| `chat_room` | 채팅방 정보 |
| `chat_room_member` | 유저-채팅방 참여 관계 |
| `chat_message` | 메시지 본문 |
| `chat_message_read` | **유저별 메시지 읽음 상태** |

→ 읽음 여부를 **메시지에 플래그로 두지 않고**,  
**유저 × 메시지** 조합으로 설계 → **N명 읽음 상태 개별 반영 가능**

### 2) 전자 결재 열람 기록
- 결재 문서를 누가 언제 봤는지 추적하기 위해  
  **`ApprovalDoc(messageId + userId)`** 복합키 엔티티 사용
- 프론트는 호출만, **열람 처리 로직은 서비스에서 자동 처리** → 요구사항 변경 시 확장 용이

### 3) 출결 기록 트랜잭션 안정성
- 출근/퇴근 Punch 처리 중 오류 발생 시 Summary 업데이트까지 **롤백 처리**
- 근무시간 계산은 DB 트리거나 CRON이 아니라 **Service 계층 내 정책 기반 계산**

---

## 🧪 문제 해결 & 개선 사항

| 문제 | 원인 | 해결 |
|---|---|---|
| 채팅 읽음 처리 시 방 전체 알림이 발생함 | 메시지 단위 업데이트 처리 미흡 | 읽음 상태를 개별 사용자 기준 테이블로 분리 |
| 결재 문서 열람 기록 누락 | 컨트롤러에서 열람 로직 분기됨 | **Service에 열람 처리 단일 책임화** |
| 근무시간 요약이 페이지마다 다르게 계산됨 | View 렌더 시점마다 계산 | Summary 계산을 별도 Service에서 단일 로직으로 통일 |

---

## 🔧 사용 기술
- **Backend**: Spring Boot, JPA, WebSocket, WebRTC Signaling
- **DB**: MySQL (정규화 + 인덱싱)
- **Infra**: Docker, REST API 설계
- **Frontend 협업**: Endpoints/DTO 명세 문서화

---

## 📌 개선 여지 (Roadmap)
- 대화 목록 미리보기 개선 (최근 메시지 / 읽음 수 표시)
- 화상회의 화면 공유 / 채팅 통합
- 결재 문서 템플릿화 + 멀티 결재선 구성
- 출결 통계 시각화(월/연 단위)

---

## 📎 Repository
https://github.com/torye2/AMGW
