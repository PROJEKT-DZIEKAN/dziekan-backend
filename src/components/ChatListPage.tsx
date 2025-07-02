import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

interface UserDTO {
  id: number;
  firstName: string;
  surname: string;
}

interface ChatDTO {
  id: number;
  userAId: number;
  userBId: number;
}

type Props = {
  userId: string;
};

export default function ChatListPage({ userId }: Props) {
  const [users, setUsers] = useState<UserDTO[]>([]);
  const navigate = useNavigate();

  useEffect(() => {
    (async () => {
      try {
        const token = localStorage.getItem('token');
        const res = await fetch(
          `${import.meta.env.VITE_API_BASE_URL}/api/users`,
          {
            headers: {
              'Content-Type': 'application/json',
              ...(token ? { Authorization: `Bearer ${token}` } : {}),
            },
            credentials: 'include',
          }
        );
        if (!res.ok) {
          console.error('fetch users error', res.status, await res.text());
          return;
        }
        const data: UserDTO[] = await res.json();
        // exclude current user
        setUsers(data.filter(u => u.id.toString() !== userId));
      } catch (error) {
        console.error('Error fetching users:', error);
      }
    })();
  }, [userId]);

  const startChat = async (otherId: number) => {
    try {
      const token = localStorage.getItem('token');
      const res = await fetch(
        `${import.meta.env.VITE_API_BASE_URL}/api/chats/get-or-create`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
          },
          credentials: 'include',
          body: JSON.stringify({ userAId: Number(userId), userBId: otherId }),
        }
      );
      if (!res.ok) {
        console.error('get-or-create failed', res.status, await res.text());
        return;
      }
      const { id } = await res.json() as ChatDTO;
      navigate(`/chats/${id}`);
    } catch (error) {
      console.error('Error creating/fetching chat:', error);
    }
  };

  return (
    <div className="chat-list-page">
      <h2>Wybierz użytkownika</h2>
      <ul className="chat-list">
        {users.map(user => (
          <li
            key={user.id}
            className="chat-list-item"
            style={{ cursor: 'pointer' }}
            onClick={() => startChat(user.id)}
          >
            {user.firstName} {user.surname} (#{user.id})
          </li>
        ))}
      </ul>
    </div>
  );
}
