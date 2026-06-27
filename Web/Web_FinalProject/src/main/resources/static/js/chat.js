// Cần có /webjars/sockjs-client/sockjs.min.js và /webjars/stomp-websocket/stomp.min.js
let stompClient = null;

function connectChat(onMessage) {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, () => {
        console.log('✅ Connected to WebSocket');
        stompClient.subscribe('/topic/public', (msg) => {
            onMessage(JSON.parse(msg.body));
        });
        stompClient.subscribe('/user/queue/messages', (msg) => {
            onMessage(JSON.parse(msg.body));
        });
    });
}

function sendMessage(content, to) {
    if (!stompClient) return;
    const payload = { content, to };
    stompClient.send('/app/chat.send', {}, JSON.stringify(payload));
}

// Export cho module khác hoặc dùng trực tiếp
window.connectChat = connectChat;
window.sendMessage = sendMessage;
