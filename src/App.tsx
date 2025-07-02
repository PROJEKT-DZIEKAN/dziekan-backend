import { useState } from 'react';
import {
  BrowserRouter,
  Routes,
  Route,
  Navigate,
  useParams,
} from 'react-router-dom';
import AuthPage from './components/AuthPage';
import NavBar from './components/Navbar';
import ProfilePage from './components/ProfilePage';
import ChatListPage from './components/ChatListPage';
import ChatPage from './components/ChatPage';
import './assets/App.css';

function getUserIdFromToken(): string | null {
  const token = localStorage.getItem('token');
  if (!token) return null;
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.sub || null;
  } catch {
    return null;
  }
}

function ChatPageWrapper() {
  const { chatId } = useParams<{ chatId: string }>();
  const userId = getUserIdFromToken();
  if (!userId || !chatId) return <Navigate to="/" replace />;
  return <ChatPage userId={userId} chatId={chatId} />;
}

export default function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(!!localStorage.getItem('token'));
  const API = import.meta.env.VITE_API_BASE_URL;

  const handleRegister = async ({ firstName, surname }: { firstName: string; surname: string }) => {
    const res = await fetch(`${API}/api/auth/register-by-name`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ firstName, surname }),
    });
    if (!res.ok) {
      const text = await res.text();
      throw new Error(JSON.parse(text)?.message || text);
    }
    const user = await res.json();
    return user.id as number;
  };

  const handleLogin = async ({ userId }: { userId: string }) => {
    const res = await fetch(`${API}/api/auth/login-by-id`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ userId: Number(userId) }),
    });
    if (!res.ok) {
      const text = await res.text();
      throw new Error(JSON.parse(text)?.message || text);
    }
    const { accessToken, refreshToken } = await res.json();
    localStorage.setItem('token', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
    setIsAuthenticated(true);
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    setIsAuthenticated(false);
  };

  return (
    <BrowserRouter>
      {isAuthenticated && <NavBar onLogout={handleLogout} />}
      <Routes>
        {!isAuthenticated && (
          <Route
            path="*"
            element={<AuthPage onLogin={handleLogin} onRegister={handleRegister} />}
          />
        )}
        {isAuthenticated && (
          <>
            <Route path="/" element={<ProfilePage onLogout={handleLogout} />} />
            <Route path="/chats" element={<ChatListPage userId={getUserIdFromToken()!} />} />
            <Route path="/chats/:chatId" element={<ChatPageWrapper />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </>
        )}
      </Routes>
    </BrowserRouter>
  );
}
