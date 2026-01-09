# WebSocket API Documentation

실시간 채팅을 위한 WebSocket(STOMP) 프로토콜 문서입니다.

## 연결 정보

| 항목 | 값 |
|------|-----|
| 엔드포인트 | `/ws` |
| 프로토콜 | STOMP over WebSocket |
| SockJS 지원 | O (Fallback용) |

## 사전 요구사항

WebSocket 연결 전 반드시 HTTP `/login` API로 로그인하여 세션을 생성해야 합니다.
WebSocket 핸드셰이크 시 HTTP 세션의 `userId`가 WebSocket 세션으로 자동 복사됩니다.

---

## 연결 방법

### JavaScript (SockJS + STOMP)

```javascript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

// 1. SockJS로 WebSocket 연결 생성
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

// 2. STOMP 연결
stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);

    // 3. 채팅방 구독
    const chatRoomId = 1;
    stompClient.subscribe(`/topic/chatroom/${chatRoomId}`, function(message) {
        const chatMessage = JSON.parse(message.body);
        console.log('Received:', chatMessage);
    });
});
```

### JavaScript (순수 WebSocket + STOMP)

```javascript
import { Client } from '@stomp/stompjs';

const client = new Client({
    brokerURL: 'ws://localhost:8080/ws',
    onConnect: () => {
        console.log('Connected');

        // 채팅방 구독
        client.subscribe('/topic/chatroom/1', (message) => {
            const chatMessage = JSON.parse(message.body);
            console.log('Received:', chatMessage);
        });
    },
});

client.activate();
```

---

## 메시지 전송 (Client -> Server)

### Destination

```
/app/chatroom/{chatRoomId}
```

### Request Body

```json
{
    "content": "안녕하세요!"
}
```

### 필드 설명

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `content` | String | O | 메시지 내용 |

### 예제 (JavaScript)

```javascript
const chatRoomId = 1;
const message = {
    content: "안녕하세요!"
};

stompClient.send(`/app/chatroom/${chatRoomId}`, {}, JSON.stringify(message));
```

### 권한

- **PARTICIPANT**: 메시지 전송 가능
- **OBSERVER**: 메시지 전송 불가 (에러 발생)

---

## 메시지 수신 (Server -> Client)

### Subscribe Destination

```
/topic/chatroom/{chatRoomId}
```

해당 채팅방에 새 메시지가 전송되면 구독 중인 모든 클라이언트에게 브로드캐스트됩니다.

### Response Body

```json
{
    "messageId": 1,
    "senderId": 1,
    "senderNickName": "홍길동",
    "content": "안녕하세요!",
    "createdAt": "2026-01-08T12:00:00",
    "type": "OTHER"
}
```

### 필드 설명

| 필드 | 타입 | 설명 |
|------|------|------|
| `messageId` | Long | 메시지 고유 ID |
| `senderId` | Long | 발신자 사용자 ID |
| `senderNickName` | String | 발신자 닉네임 |
| `content` | String | 메시지 내용 |
| `createdAt` | String (ISO 8601) | 메시지 생성 시간 |
| `type` | String | 메시지 타입 (`ME` 또는 `OTHER`) |

### MessageType

| 값 | 설명 |
|----|------|
| `ME` | 내가 보낸 메시지 |
| `OTHER` | 다른 사람이 보낸 메시지 |

> **Note**: WebSocket 브로드캐스트 시에는 항상 `type: "OTHER"`로 전송됩니다.
> 클라이언트에서 `senderId`와 현재 로그인한 사용자 ID를 비교하여 내 메시지 여부를 판단하세요.

---

## 전체 흐름 예제

```javascript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

const API_BASE = 'http://localhost:8080';
let stompClient = null;
let currentUserId = null;

// 1. 로그인 (세션 생성)
async function login(nickname, password) {
    const response = await fetch(`${API_BASE}/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include', // 세션 쿠키 포함
        body: JSON.stringify({ nickname, password })
    });
    return response.json();
}

// 2. WebSocket 연결
function connectWebSocket(chatRoomId, onMessageReceived) {
    const socket = new SockJS(`${API_BASE}/ws`);
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function(frame) {
        console.log('WebSocket Connected');

        // 채팅방 구독
        stompClient.subscribe(`/topic/chatroom/${chatRoomId}`, function(message) {
            const chatMessage = JSON.parse(message.body);

            // 내 메시지인지 판단
            const isMyMessage = chatMessage.senderId === currentUserId;
            chatMessage.type = isMyMessage ? 'ME' : 'OTHER';

            onMessageReceived(chatMessage);
        });
    });
}

// 3. 메시지 전송
function sendMessage(chatRoomId, content) {
    if (stompClient && stompClient.connected) {
        stompClient.send(`/app/chatroom/${chatRoomId}`, {}, JSON.stringify({
            content: content
        }));
    }
}

// 4. 연결 해제
function disconnect() {
    if (stompClient) {
        stompClient.disconnect();
    }
}

// 사용 예시
async function main() {
    // 로그인
    await login('홍길동', 'password123');
    currentUserId = 1; // 실제로는 로그인 응답에서 받아야 함

    // WebSocket 연결 및 채팅방 구독
    connectWebSocket(1, (message) => {
        if (message.type === 'ME') {
            console.log(`[나] ${message.content}`);
        } else {
            console.log(`[${message.senderNickName}] ${message.content}`);
        }
    });

    // 메시지 전송
    sendMessage(1, '안녕하세요!');
}
```

---

## 종료(판결) 알림 수신 (Server -> Client)

### Subscribe Destination

```
/topic/chatroom/{chatRoomId}/exit
```

채팅방 종료(판결) 요청/수락/거절 시 해당 채팅방을 구독 중인 모든 클라이언트에게 알림이 전송됩니다.

### Response Body

```json
{
    "type": "EXIT_REQUEST",
    "requesterNickname": "홍길동",
    "message": "지금까지의 대화를 바탕으로 판결을 요청하시겠습니까?"
}
```

### 필드 설명

| 필드 | 타입 | 설명 |
|------|------|------|
| `type` | String | 알림 타입 (`EXIT_REQUEST`, `EXIT_APPROVED`, `EXIT_REJECTED`) |
| `requesterNickname` | String | 요청자/결정자 닉네임 |
| `message` | String | 알림 메시지 |

### 알림 타입

| 값 | 설명 |
|----|------|
| `EXIT_REQUEST` | 판결 요청됨 (상대방이 판결을 요청함) |
| `EXIT_APPROVED` | 판결 수락됨 (채팅방 종료) |
| `EXIT_REJECTED` | 판결 거절됨 (채팅방 계속 진행) |

### 예제 (JavaScript)

```javascript
// 종료 알림 구독
stompClient.subscribe(`/topic/chatroom/${chatRoomId}/exit`, function(message) {
    const notification = JSON.parse(message.body);

    switch (notification.type) {
        case 'EXIT_REQUEST':
            // 판결 요청 UI 표시 (수락/거절 버튼)
            showExitRequestDialog(notification.requesterNickname, notification.message);
            break;
        case 'EXIT_APPROVED':
            // 채팅방 종료 처리
            closeChatRoom(notification.message);
            break;
        case 'EXIT_REJECTED':
            // 판결 거절 알림
            showNotification(notification.message);
            break;
    }
});
```

---

## 최종 판결 알림 (AI 분석) (Server -> Client)

### Subscribe Destination

```
/topic/chatroom/{chatRoomId}/exit
```

판결이 승인되면 AI가 비동기로 분석을 수행하고 결과를 브로드캐스트합니다.

**타이밍**: EXIT_APPROVED 알림이 먼저 전송되고, 수 초 후 AI 분석 완료 시 FINAL_JUDGMENT 또는 JUDGMENT_ERROR가 전송됩니다.

### Response Body (성공)

```json
{
    "type": "FINAL_JUDGMENT",
    "winner": "홍길동",
    "plaintiff": "홍길동",
    "defendant": "김철수",
    "winnerLogicScore": 85,
    "winnerEmpathyScore": 72,
    "judgmentComment": "원고가 구체적인 근거를 제시하며 논리적으로 주장을 펼쳤습니다.",
    "winnerReason": "구체적 사례와 논리적 근거 제시",
    "loserReason": "감정적 대응으로 일관"
}
```

### Response Body (실패)

```json
{
    "type": "JUDGMENT_ERROR",
    "errorMessage": "AI 분석 중 오류가 발생했습니다."
}
```

### 필드 설명

#### FINAL_JUDGMENT (성공 시)

| 필드 | 타입 | 설명 |
|------|------|------|
| `type` | String | 알림 타입 (`FINAL_JUDGMENT`) |
| `winner` | String | 승자 닉네임 |
| `plaintiff` | String | 원고 닉네임 (채팅방 생성자) |
| `defendant` | String | 피고 닉네임 (상대방) |
| `winnerLogicScore` | Integer | 승자의 논리력 점수 (0-100) |
| `winnerEmpathyScore` | Integer | 승자의 공감력 점수 (0-100) |
| `judgmentComment` | String | 심판 코멘트 |
| `winnerReason` | String | 승자가 가산점을 받은 이유 |
| `loserReason` | String | 패자가 감점된 이유 |

#### JUDGMENT_ERROR (실패 시)

| 필드 | 타입 | 설명 |
|------|------|------|
| `type` | String | 알림 타입 (`JUDGMENT_ERROR`) |
| `errorMessage` | String | 에러 메시지 |

### 예제 (JavaScript)

```javascript
// 종료 및 판결 알림 구독 (동일 토픽)
stompClient.subscribe(`/topic/chatroom/${chatRoomId}/exit`, function(message) {
    const notification = JSON.parse(message.body);

    switch (notification.type) {
        case 'EXIT_REQUEST':
            // 판결 요청 UI 표시
            showExitRequestDialog(notification.requesterNickname, notification.message);
            break;
        case 'EXIT_APPROVED':
            // 채팅방 종료 알림 (AI 분석 대기 중...)
            showExitApproved(notification.message);
            break;
        case 'EXIT_REJECTED':
            // 판결 거절 알림
            showNotification(notification.message);
            break;
        case 'FINAL_JUDGMENT':
            // 최종 판결 결과 표시
            showJudgmentResult({
                winner: notification.winner,
                plaintiff: notification.plaintiff,
                defendant: notification.defendant,
                logicScore: notification.winnerLogicScore,
                empathyScore: notification.winnerEmpathyScore,
                comment: notification.judgmentComment,
                winnerReason: notification.winnerReason,
                loserReason: notification.loserReason
            });
            break;
        case 'JUDGMENT_ERROR':
            // AI 분석 실패 알림
            showError(notification.errorMessage);
            break;
    }
});
```

---

## 에러 처리

### 인증 실패

WebSocket 연결 시 로그인된 세션이 없으면 메시지 전송 시 에러가 발생합니다.

```javascript
stompClient.connect({},
    function(frame) {
        // 연결 성공
    },
    function(error) {
        // 연결 실패 또는 에러
        console.error('STOMP error:', error);
    }
);
```

### 권한 에러

OBSERVER가 메시지를 전송하려고 하면 서버에서 예외가 발생합니다.

---

## CORS 설정

WebSocket 연결 시 모든 origin이 허용됩니다 (`setAllowedOriginPatterns("*")`).
단, 세션 쿠키 전송을 위해 `credentials: 'include'`를 설정해야 합니다.

---

## 관련 REST API

WebSocket과 함께 사용할 수 있는 REST API입니다.

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/chat/room/create` | 채팅방 생성 |
| POST | `/chat/room/join` | 채팅방 입장 (초대 코드) |
| POST | `/chat/room/{chatRoomId}/message` | 메시지 전송 (REST) |
| GET | `/chat/room/{chatRoomId}/messages` | 메시지 목록 조회 |
| POST | `/chat/room/{chatRoomId}/exit/request` | 판결(종료) 요청 |
| POST | `/chat/room/{chatRoomId}/exit/decide` | 판결 요청 수락/거절 |

> REST API 상세 스펙은 Swagger UI (`/swagger-ui.html`)에서 확인하세요.

---
## 개발 예정
- 실시간 승률 응답

닉네임 : 승률 쌍의 JSON 응답
```json
{
  "type": "DEBATE_STATUS",
  "score": {
    "A": 57.3,
    "B": 42.7
  }
}
```

- 최종 판결문 응답

```json
{
  "type": "FINAL_JUDGEMENT",
  "winner": "A",
  "plaintiff": "A",
  "defendant": "B",
  "winnerLogicScore": {
    "A": 57.3,
    "B": 42.7
  },
  "winnerEmpathyScore": {
    "A": 57.3,
    "B": 42.7
  },
  "judgementComment": "논리는 대등했으나, 법정 모독(비속어 사용)으로 원고 승소!",
  "winnerReason": "원고는 구체적 사유를 들어 지각을 소명함",
  "loserReason": "피고는 논리적 반박 대신 감정적 비난으로 일관함"
}
```