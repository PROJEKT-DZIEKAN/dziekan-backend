import { useEffect, useState, useRef } from 'react';
import { CompatClient, Stomp } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import '../assets/ChatPage.css';

interface MessageDTO {
  chatId: number;
  senderId: number;
  content: string;
  sentAt: string;
}

type ChatPageProps = {
  userId: string;
  chatId?: string;
  otherUserId?: string;
};

export default function ChatPage({ userId, chatId, otherUserId }: ChatPageProps) {
  const [activeChatId, setActiveChatId] = useState<number | null>(null);
  const [messages, setMessages] = useState<MessageDTO[]>([]);
  const [input, setInput] = useState('');
  const clientRef = useRef<CompatClient | null>(null);
  const chatWindowRef = useRef<HTMLDivElement | null>(null);

  const API_BASE = import.meta.env.VITE_API_BASE_URL || '';

  const jwt = useRef(localStorage.getItem('token') ?? '');

  useEffect(() => {
    chatWindowRef.current?.scrollTo({
      top: chatWindowRef.current.scrollHeight,
      behavior: 'smooth'
    });
  }, [messages]);

  useEffect(() => {
    return () => {
      clientRef.current?.disconnect(() => console.log('[STOMP] disconnected'));
    };
  }, []);


  const initStomp = (numericChatId: number) => {
    setActiveChatId(numericChatId);

    const socket = new SockJS(`${API_BASE}/ws-chat?token=${encodeURIComponent(jwt.current)}`);
    const stompClient = Stomp.over(() => socket);
    clientRef.current = stompClient;

    stompClient.connect(
      {},
      () => {
        console.log('[STOMP] connected ✅');

        stompClient.subscribe('/user/queue/history', msg => {
          console.log('[STOMP] HISTORY RAW:', msg.body);
          setMessages(JSON.parse(msg.body));
        });

        stompClient.send('/app/chat.history', {}, JSON.stringify({ id: numericChatId }));
        console.log('[STOMP] history request sent');

        stompClient.subscribe('/user/queue/messages', msg => {
          console.log('[STOMP] MSG INCOMING:', msg.body);
          setMessages(prev => [...prev, JSON.parse(msg.body)]);
        });
      },
      err => console.error('[STOMP] connection error:', err)
    );
  };


  useEffect(() => {
    if (chatId) {
      const num = Number(chatId);
      if (!isNaN(num)) initStomp(num);
      return;
    }
    if (!otherUserId) return;

    const otherNum = Number(otherUserId);
    if (isNaN(otherNum)) return;

    (async () => {
      try {
        const res = await fetch(`${API_BASE}/api/chats/get-or-create`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'include',
          body: JSON.stringify({ userAId: Number(userId), userBId: otherNum })
        });

        if (res.ok) {
          const { id } = await res.json();
          initStomp(id);
        } else {
          console.error('[REST] get-or-create failed', res.status);
        }
      } catch (e) {
        console.error('[REST] get-or-create error', e);
      }
    })();
  }, [userId, chatId, otherUserId]);

  const sendMessage = () => {
    if (!input.trim() || !clientRef.current || activeChatId == null) return;

    const payload = { chatId: activeChatId, senderId: Number(userId), content: input };
    clientRef.current.send('/app/chat.send', {}, JSON.stringify(payload));
    console.log('[STOMP] MSG OUTGOING:', payload);
    setInput('');
  };


  return (
    <div className="chat-page">
      <div className="chat-window" ref={chatWindowRef}>
        {messages.length === 0 ? (
          <div className="no-messages">Brak wiadomości</div>
        ) : (
          messages.map((m, i) => (
            <div key={i} className={m.senderId === Number(userId) ? 'msg-outgoing' : 'msg-incoming'}>
              <span className="msg-meta">
                {new Date(m.sentAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
              </span>
              <div className="msg-content">{m.content}</div>
            </div>
          ))
        )}
      </div>

      <div className="chat-input">
        <input
          type="text"
          value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && sendMessage()}
          placeholder="Napisz wiadomość..."
        />
        <button onClick={sendMessage}>Wyślij</button>
      </div>
    </div>
  );
}
